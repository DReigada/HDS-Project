package com.tecnico.sec.hds;

import com.tecnico.sec.hds.util.QuorumHelper;
import com.tecnico.sec.hds.util.SecurityHelper;
import com.tecnico.sec.hds.util.TransactionGetter;
import com.tecnico.sec.hds.util.Tuple;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import domain.Transaction;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServersWrapper {
  public final SecurityHelper securityHelper;
  private final Map<String, DefaultApi> servers;
  private final QuorumHelper quorumHelper;

  public ServersWrapper(String user, String pass) throws IOException, OperatorCreationException, GeneralSecurityException {
    this(user, pass, getServersConfig());
  }

  public ServersWrapper(String user, String pass, List<String> serversUrls) throws IOException, GeneralSecurityException, OperatorCreationException {
    securityHelper = new SecurityHelper(user, pass);
    servers = new HashMap<>();
    initializeServers(serversUrls.stream());
    quorumHelper = new QuorumHelper(servers.size());
  }

  private static List<String> getServersConfig() {
    return Optional.ofNullable(System.getProperty("servers.urls.file"))
        .map(ServersWrapper::getServersConfigFromFile)
        .orElseGet(ServersWrapper::getServersConfigFromResource)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  private static Stream<String> getServersConfigFromFile(String filename) {
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

  private static Stream<String> getServersConfigFromResource() {
    return new BufferedReader(new InputStreamReader((ServersWrapper.class.getResourceAsStream("/conf/servers.conf")))).lines();
  }

  private void initializeServers(Stream<String> urls) {
    urls.forEach(url -> {
      ApiClient client = new ApiClient().setBasePath(url);
      DefaultApi server = new DefaultApi(client);
      servers.put(url, server);
    });
  }

  public void broadcast(BroadcastRequest body) {
    forEachServer(s -> {
      s.broadcast(body);
      return true;
    });
  }

  public Optional<AuditResponse> audit(AuditRequest body) {
    Optional<Tuple<Tuple<DefaultApi, AuditResponse>, List<Tuple<DefaultApi, AuditResponse>>>> serversWithResponsesQuorum =
        serverReadWithQuorums(server -> server.audit(body), AuditResponse::getList, this::verifyAuditResponse);

    //TODO implement writeBack
    //serversWithResponsesQuorum.stream()
    //    .skip(1)  // skip the quorum response
    //    .forEach(writeBack);

    return serversWithResponsesQuorum.map(tuple -> {
      AuditResponse auditResponse = tuple.first.second;

      if (body.getPublicKey().equals(securityHelper.key)) {
        securityHelper.setLastHash(auditResponse.getList().get(0).getSendHash());
      }

      return auditResponse;
    });
  }

  private boolean verifyAuditResponse(Tuple<DefaultApi, AuditResponse> serverWithResponse) {
    String serverUrl = serverWithResponse.first.getApiClient().getBasePath();
    AuditResponse response = serverWithResponse.second;
    List<TransactionInformation> transactionList = response.getList();
    if (transactionList != null) {
      String transactionListMessage = TransactionGetter.getTransactionListMessage(transactionList);
      List<Transaction> transactions = TransactionGetter.InformationToTransaction(transactionList);
      Collections.reverse(transactions);

      try {
        String signature = response.getSignature().getValue();
        return securityHelper.verifySignature(transactionListMessage, signature, serverUrl)
            && securityHelper.verifyTransactionsSignaturesAndChain(transactions);
      } catch (GeneralSecurityException | IOException e) {
        System.err.println("Failed to verify response from server: " + serverUrl);
        e.printStackTrace();
        return false;
      }
    } else {
      return false;
    }
  }

  public Optional<Tuple<CheckAccountResponse, Long>> checkAccount(CheckAccountRequest body) {
    body.setPublicKey(securityHelper.key);

    Optional<Tuple<Tuple<DefaultApi, CheckAccountResponse>, List<Tuple<DefaultApi, CheckAccountResponse>>>> serversWithResponsesQuorumOpt =
        serverReadWithQuorums(server -> server.checkAccount(body), CheckAccountResponse::getHistory, this::verifyCheckAccount); // TODO change this

    return serversWithResponsesQuorumOpt.map(serversWithResponsesQuorum -> {
      CheckAccountResponse checkAmountResponse = serversWithResponsesQuorum.first.second;

      return new Tuple<>(serversWithResponsesQuorum.first.second, getBalanceFromTransactions(checkAmountResponse.getHistory()));
    });
  }

  private boolean verifyCheckAccount(Tuple<DefaultApi, CheckAccountResponse> serverWithResponse) {
    String serverUrl = serverWithResponse.first.getApiClient().getBasePath();
    CheckAccountResponse response = serverWithResponse.second;

    List<Transaction> transactionsHistory =
        TransactionGetter.InformationToTransaction(response.getHistory());

    String serverMessage =
        TransactionGetter.getTransactionListMessage(response.getHistory()) +
            TransactionGetter.getTransactionListMessage(response.getPending());

    try {
      Collections.reverse(transactionsHistory);
      return
          securityHelper.verifyTransactionsSignaturesAndChain(transactionsHistory) &&
              securityHelper.verifySignature(serverMessage, response.getSignature().getValue(), serverUrl);
    } catch (GeneralSecurityException | IOException e) {
      System.err.println("Failed to verify response from server: " + serverUrl);
      e.printStackTrace();
      return false;
    }
  }

  private long getBalanceFromTransactions(List<TransactionInformation> transactions) {
    return transactions.stream()
        .mapToLong(t -> t.isReceive() ? Long.valueOf(t.getAmount()) : -Long.valueOf(t.getAmount()))
        .sum();
  }

  public String register() throws GeneralSecurityException, IOException {

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

  public boolean receiveAmount(ReceiveAmountRequest body) throws GeneralSecurityException, IOException {

    body.setDestKey(securityHelper.key);

    body.setLastHash(securityHelper.createHash(
        Optional.of(securityHelper.getLastHash().getValue()),
        Optional.of(body.getTransHash().getValue()),
        body.getSourceKey().getValue(),
        body.getDestKey().getValue(),
        body.getAmount(),
        ChainHelper.TransactionType.ACCEPT));

    securityHelper.signMessage(
        body.getSourceKey().getValue()
            + securityHelper.key.getValue()
            + body.getAmount()
            + body.getLastHash().getValue()
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

  public String sendAmount(SendAmountRequest body) throws GeneralSecurityException, IOException {

    body.setLastHash(securityHelper.createHash(
        Optional.of(securityHelper.getLastHash().getValue()),
        Optional.empty(),
        securityHelper.key.getValue(),
        body.getDestKey().getValue(), body.getAmount(),
        ChainHelper.TransactionType.SEND_AMOUNT));

    body.sourceKey(securityHelper.key);

    String message = securityHelper.key.getValue()
        + body.getDestKey().getValue()
        + body.getAmount().toString()
        + body.getLastHash().getValue();

    securityHelper.signMessage(message, body::setSignature);

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

  public GetTransactionResponse getTransaction(GetTransactionRequest body) throws GeneralSecurityException, IOException {
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
  private <RES> Optional<Tuple<Tuple<DefaultApi, RES>, List<Tuple<DefaultApi, RES>>>> serverReadWithQuorums(
      ServerCall<RES> serverCall,
      Function<RES, List<TransactionInformation>> responseToList,
      Predicate<Tuple<DefaultApi, RES>> responseVerifier
  ) {
    return quorumHelper.getReadQuorumFromResponses(forEachServer(serverCall), responseToList, responseVerifier);
  }

  <A> Stream<Tuple<DefaultApi, A>> forEachServer(ServerCall<A> serverCall) {
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

  @FunctionalInterface
  interface ServerCall<R> {
    R apply(DefaultApi t) throws ApiException;
  }
}