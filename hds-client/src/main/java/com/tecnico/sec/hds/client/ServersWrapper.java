package com.tecnico.sec.hds.client;

import com.tecnico.sec.hds.client.commands.util.QuorumHelper;
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
    Stream<AuditResponse> responses = forEachServer(server -> server.audit(body));

    // TODO verify signatures

    List<AuditResponse> responsesList = responses.collect(Collectors.toList());

    List<AuditResponse> naoSeiOquelhechamar =
        QuorumHelper.getTransactionsQuorum(responsesList, AuditResponse::getList, getServersThreshold())
            .collect(Collectors.toList());

    AuditResponse quorumResponse = naoSeiOquelhechamar.get(0);

    //TODO implement writeBack
    //naoSeiOquelhechamar.stream()
    //    .skip(1)  // skip the quorum response
    //    .forEach(writeBack);

    return quorumResponse;
  }

  public CheckAccountResponse checkAccount(CheckAccountRequest body) {
    return forEachServer(server -> server.checkAccount(body))
        .findFirst()
        .get();
  }

  public RegisterResponse register(RegisterRequest body) {
    return forEachServer(server -> server.register(body))
        .collect(Collectors.toList())
        .get(0);
  }

  public ReceiveAmountResponse receiveAmount(ReceiveAmountRequest body) {
    return forEachServer(server -> server.receiveAmount(body))
        .collect(Collectors.toList())
        .get(0);
  }

  public SendAmountResponse sendAmount(SendAmountRequest body) {
    return forEachServer(server -> server.sendAmount(body))
        .collect(Collectors.toList())
        .get(0);
  }

  public GetTransactionResponse getTransaction(GetTransactionRequest body) {
    return forEachServer(server -> server.getTransaction(body))
        .collect(Collectors.toList())
        .get(0);
  }

  private int getServersThreshold() {
    return (int) ((servers.size() + getNumberOfFaults(servers.size())) / 2.0);
  }

  private int getNumberOfFaults(int serversNumber) {
    return 1; // TODO change this
  }

  private <A> Stream<A> forEachServer(ServerCall<A> serverCall) {
    return servers.entrySet().stream().parallel()
        .flatMap(entry -> {
          try {
            return Stream.of(serverCall.apply(entry.getValue()));
          } catch (ApiException e) {
            System.out.println("Failed to call server: " + entry.getKey());
            return Stream.empty();
          }
        });
  }

  @FunctionalInterface
  private interface ServerCall<R> {
    R apply(DefaultApi t) throws ApiException;
  }
}
