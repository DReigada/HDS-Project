package com.tecnico.sec.hds.client;

import com.tecnico.sec.hds.client.commands.util.QuorumHelper;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServersWrapper {
  private final Map<String, DefaultApi> servers;

  public ServersWrapper() throws IOException {
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
      servers.put(url, new DefaultApi(client));
    });
  }

  public AuditResponse audit(AuditRequest body) {
    Tuple<AuditResponse, List<Tuple<DefaultApi, AuditResponse>>> serversWithResponsesQuorum =
        serverReadWithQuorums(server -> server.audit(body), AuditResponse::getList);

    //TODO implement writeBack
    //serversWithResponsesQuorum.stream()
    //    .skip(1)  // skip the quorum response
    //    .forEach(writeBack);

    return serversWithResponsesQuorum.first;
  }

  public CheckAccountResponse checkAccount(CheckAccountRequest body) {
    Tuple<CheckAccountResponse, List<Tuple<DefaultApi, CheckAccountResponse>>> serversWithResponsesQuorum =
        serverReadWithQuorums(server -> server.checkAccount(body), CheckAccountResponse::getPending);

    //TODO implement writeBack
    //serversWithResponsesQuorum.stream()
    //    .skip(1)  // skip the quorum response
    //    .forEach(writeBack);

    return serversWithResponsesQuorum.first;
  }

  public RegisterResponse register(RegisterRequest body) {
    return forEachServer(server -> server.register(body))
        .map(t -> t.second)
        .collect(Collectors.toList())
        .get(0);
  }

  public ReceiveAmountResponse receiveAmount(ReceiveAmountRequest body) {
    return forEachServer(server -> server.receiveAmount(body))
        .map(t -> t.second)
        .collect(Collectors.toList())
        .get(0);
  }

  public SendAmountResponse sendAmount(SendAmountRequest body) {
    return forEachServer(server -> server.sendAmount(body))
        .map(t -> t.second)
        .collect(Collectors.toList())
        .get(0);
  }

  public GetTransactionResponse getTransaction(GetTransactionRequest body) {
    return forEachServer(server -> server.getTransaction(body))
        .map(t -> t.second)
        .collect(Collectors.toList())
        .get(0);
  }

  /**
   * @param serverCall     the read call to be done to the servers
   * @param responseToList a function to transform a server response to a list of transactions
   * @param <A>            he server response type
   * @return list tuple that contains the quorum response and the servers that are missing transactions (to be used in the write back)
   */
  private <A> Tuple<A, List<Tuple<DefaultApi, A>>> serverReadWithQuorums(ServerCall<A> serverCall, Function<A, List<TransactionInformation>> responseToList) {
    List<Tuple<DefaultApi, A>> serversWithResponses = forEachServer(serverCall).collect(Collectors.toList()); // TODO should we call all servers??

    List<Tuple<DefaultApi, A>> bla = QuorumHelper.getTransactionsQuorum(serversWithResponses, a -> responseToList.apply(a.second), getServersThreshold())
        .collect(Collectors.toList());

    A quorumResponse = bla.get(0).second;

    List<Tuple<DefaultApi, A>> serversWithMissingTransactions = bla.stream().skip(1).collect(Collectors.toList());

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
