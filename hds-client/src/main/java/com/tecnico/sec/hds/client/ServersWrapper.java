package com.tecnico.sec.hds.client;

import com.tecnico.sec.hds.client.commands.util.QuorumHelper;
import com.tecnico.sec.hds.client.commands.util.SecurityHelper;
import com.tecnico.sec.hds.client.commands.util.TransactionGetter;
import com.tecnico.sec.hds.util.Tuple;
import domain.Transaction;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;
import io.swagger.client.model.Signature;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServersWrapper {
  private final Map<String, DefaultApi> servers;
  private final SecurityHelper securityHelper;

  public ServersWrapper(String user, String pass) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, OperatorCreationException {
    securityHelper = new SecurityHelper(user, pass);
    servers = new HashMap<>();
    initializeServers(getServersConfig());
  }

  private Stream<String> getServersConfig() {
    return Optional.ofNullable(System.getProperty("servers.urls.file"))
        .map(this::getServersConfigFromFile)
        .orElseGet(this::getServersConfigFromResource)
        .filter(s -> !s.isEmpty());
  }

  private Stream<String> getServersConfigFromFile(String filename) {
    try {
      return Files.lines(Paths.get(filename));
    } catch (NoSuchFileException e) {
      System.err.println("Could not find config file with servers urls \"servers.conf\". Using the default config.");
      return Stream.of("http://localhost:8080");
    } catch (IOException e) {
      System.err.println("Error getting the config file: " + e.getMessage());
      System.err.println("Using the default config.");
      return Stream.of("http://localhost:8080");
    }
  }

  private Stream<String> getServersConfigFromResource() {
    return new BufferedReader(new InputStreamReader((getClass().getResourceAsStream("/conf/servers.conf")))).lines();
  }

  private void initializeServers(Stream<String> urls) {
    urls.forEach(url -> {
      ApiClient client = new ApiClient().setBasePath(url);
      DefaultApi server = new DefaultApi(client);
      servers.put(url, server);
    });
  }

  private Boolean verifyAuditResponse(Tuple<DefaultApi, AuditResponse> serverWithReponse) {
    String serverUrl = serverWithReponse.first.getApiClient().getBasePath();
    AuditResponse response = serverWithReponse.second;
    return verifyTransactions(serverUrl, response);
  }

  private Boolean verifyTransactions(String serverUrl, AuditResponse response) {
    List<TransactionInformation> transactionList = response.getList();
    if (transactionList != null) {
      String transactionListMessage = TransactionGetter.getTransactionListMessage(transactionList);
      List<Transaction> transactions = TransactionGetter.InformationToTransaction(transactionList);
      Collections.reverse(transactions);

      try {
        String signature = response.getSignature().getValue();
        return securityHelper.verifySignature(transactionListMessage, signature, serverUrl)
            && securityHelper.verifySignatures(transactions, serverUrl);
      } catch (GeneralSecurityException | IOException e) {
        System.err.println("Failed to verify response from server: " + serverUrl);
        e.printStackTrace();
        return false;
      }
    } else {
      return false;
    }
  }

  public Optional<AuditResponse> audit(AuditRequest body) {
    Tuple<Tuple<DefaultApi, AuditResponse>, List<Tuple<DefaultApi, AuditResponse>>> serversWithResponsesQuorum =
        serverReadWithQuorums(server -> server.audit(body), AuditResponse::getList, this::verifyAuditResponse);

    //TODO implement writeBack
    //serversWithResponsesQuorum.stream()
    //    .skip(1)  // skip the quorum response
    //    .forEach(writeBack);

    AuditResponse auditResponse = serversWithResponsesQuorum.first.second;
    if (auditResponse.getList() != null) {
      if (body.getPublicKey().equals(securityHelper.key)) {
        securityHelper.setLastHash(auditResponse.getList().get(0).getSendHash());
      }
      return Optional.of(auditResponse);
    }
    return Optional.empty();
  }

  public String checkAccount(CheckAccountRequest body) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException {
    body.setPublicKey(securityHelper.key);

    Tuple<Tuple<DefaultApi, CheckAccountResponse>, List<Tuple<DefaultApi, CheckAccountResponse>>> serversWithResponsesQuorum =
        serverReadWithQuorums(server -> server.checkAccount(body), CheckAccountResponse::getList, null); // TODO change this

    CheckAccountResponse checkAmountResponse = serversWithResponsesQuorum.first.second;
    String port = serversWithResponsesQuorum.first.first.getApiClient().getBasePath();

    StringBuilder response = new StringBuilder(
        "Public Key: "
            + securityHelper.key.getValue()
            + "\n"
            + "Balance: "
            + checkAmountResponse.getAmount() + "\n");
    Signature signature = checkAmountResponse.getSignature();

    if (checkAmountResponse.getList() != null) {
      response.append(TransactionGetter.getTransactionListMessage(checkAmountResponse.getList()));
    }

    if (securityHelper.verifySignature(response.toString(), signature.getValue(), port)) {
      return response.toString();
    }

    //TODO implement writeBack
    //serversWithResponsesQuorum.stream()
    //    .skip(1)  // skip the quorum response
    //    .forEach(writeBack);

    return "Unexpected error from server. \n Try Again Later.";
  }

  public String register() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException,
      UnrecoverableKeyException, CertificateException, InvalidKeySpecException, KeyStoreException, IOException {

    RegisterRequest body = new RegisterRequest().publicKey(securityHelper.key);
    securityHelper.signMessage(securityHelper.key.getValue(), body::setSignature);

    Tuple<DefaultApi, RegisterResponse> response = forEachServer(server -> server.register(body))
        .collect(Collectors.toList())
        .get(0);

    RegisterResponse registerResponse = response.second;
    String message = registerResponse.getMessage() + registerResponse.getHash().getValue();

    if (securityHelper.verifySignature(message, registerResponse.getSignature().getValue(), response.first.getApiClient().getBasePath())) {

      securityHelper.setLastHash(registerResponse.getHash());
      return registerResponse.getMessage();
    }

    return "Unexpected error from server. \n Try Again Later.";
  }

  public boolean receiveAmount(ReceiveAmountRequest body) throws NoSuchAlgorithmException,
      InvalidKeyException, SignatureException, UnrecoverableKeyException, CertificateException, InvalidKeySpecException,
      KeyStoreException, IOException {

    body.setDestKey(securityHelper.key);
    body.setLastHash(securityHelper.getLastHash());

    securityHelper.signMessage(
        body.getSourceKey().getValue()
            + securityHelper.key.getValue()
            + body.getAmount()
            + securityHelper.getLastHash().getValue()
            + body.getTransHash().getValue(),
        body::setSignature);

    Tuple<DefaultApi, ReceiveAmountResponse> response = forEachServer(server -> server.receiveAmount(body))
        .collect(Collectors.toList())
        .get(0);

    ReceiveAmountResponse receiveAmountResponse = response.second;
    String message = receiveAmountResponse.getNewHash().getValue() + receiveAmountResponse.getMessage();
    String signature = receiveAmountResponse.getSignature().getValue();

    return securityHelper.verifySignature(message, signature, response.first.getApiClient().getBasePath())
        && receiveAmountResponse.isSuccess();
  }

  public String sendAmount(SendAmountRequest body) throws NoSuchAlgorithmException, InvalidKeyException,
      SignatureException, UnrecoverableKeyException, CertificateException, InvalidKeySpecException,
      KeyStoreException, IOException {

    body.setLastHash(securityHelper.getLastHash());
    body.sourceKey(securityHelper.key);

    String message = securityHelper.key.getValue()
        + body.getDestKey().getValue()
        + body.getAmount().toString()
        + securityHelper.getLastHash().getValue();

    securityHelper.signMessage(message, body::setSignature);

    System.out.println(message);
    System.out.println(body.getSignature().getValue());

    Tuple<DefaultApi, SendAmountResponse> response = forEachServer(server -> server.sendAmount(body))
        .collect(Collectors.toList())
        .get(0);

    SendAmountResponse sendAmountResponse = response.second;
    message = sendAmountResponse.getNewHash().getValue() + sendAmountResponse.getMessage();

    if (securityHelper.verifySignature(message, sendAmountResponse.getSignature().getValue(), response.first.getApiClient().getBasePath())
        && sendAmountResponse.isSuccess()) {

      securityHelper.setLastHash(sendAmountResponse.getNewHash());
      return sendAmountResponse.getMessage();
    }

    return "Unexpected error from server. \n Try Again Later.";
  }

  public GetTransactionResponse getTransaction(GetTransactionRequest body) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, InvalidKeyException, InvalidKeySpecException {
    //TODO remove this
    Tuple<DefaultApi, GetTransactionResponse> response = forEachServer(server -> server.getTransaction(body))
        .collect(Collectors.toList())
        .get(0);

    GetTransactionResponse getTransactionResponse = (GetTransactionResponse) response.second;
    if (securityHelper.verifySignature(
        TransactionGetter.getTransactionListMessage(getTransactionResponse.getTransaction()),
        getTransactionResponse.getSignature().getValue(),
        response.first.getApiClient().getBasePath())) {
      return getTransactionResponse;
    }
    return null;
  }

  /**
   * @param serverCall     the read call to be done to the servers
   * @param responseToList a function to transform a server response to a list of transactions
   * @param <RES>          he server response type
   * @return list tuple that contains the quorum response and the servers that are missing transactions (to be used in the write back)
   */
  private <RES> Tuple<Tuple<DefaultApi, RES>, List<Tuple<DefaultApi, RES>>> serverReadWithQuorums(ServerCall<RES> serverCall,
                                                                                                  Function<RES, List<TransactionInformation>> responseToList,
                                                                                                  Predicate<Tuple<DefaultApi, RES>> transactionVerifier
  ) {
    List<Tuple<DefaultApi, RES>> serversWithResponses =
        forEachServer(serverCall)
            .filter(transactionVerifier)
            .collect(Collectors.toList()); // TODO should we call all servers??

    List<Tuple<DefaultApi, RES>> bla = QuorumHelper.getTransactionsQuorum(serversWithResponses, a -> responseToList.apply(a.second), getServersThreshold())
        .collect(Collectors.toList());

    Tuple<DefaultApi, RES> quorumResponse = bla.get(0);

    List<Tuple<DefaultApi, RES>> serversWithMissingTransactions = bla.stream().skip(1).collect(Collectors.toList());

    return new Tuple<>(quorumResponse, serversWithMissingTransactions);
  }

  private <A> Stream<Tuple<DefaultApi, A>> forEachServer(ServerCall<A> serverCall) {
    return servers.entrySet().stream().parallel()
        .flatMap(entry -> {
          try {
            DefaultApi server = entry.getValue();
            return Stream.of(new Tuple<>(server, serverCall.apply(server)));
          } catch (ApiException e) {
            System.out.println("Failed to call server: " + entry.getKey());
            return Stream.empty();
          }
        });
  }

  private int getServersThreshold() {
    return (int) ((servers.size() + getNumberOfFaults(servers.size())) / 2.0);
  }

  private int getNumberOfFaults(int serversNumber) {
    return 1; // TODO change this
  }

  @FunctionalInterface
  private interface ServerCall<R> {
    R apply(DefaultApi t) throws ApiException;
  }
}
