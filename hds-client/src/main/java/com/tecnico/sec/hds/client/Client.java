package com.tecnico.sec.hds.client;

import com.tecnico.sec.hds.client.commands.*;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.client.ApiClient;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.PubKey;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Client {
  private final ApiClient client;
  public final DefaultApi server;
  public final CryptoAgent cryptoAgent;
  public final PubKey key;

  private Map<String, AbstractCommand> commands;

  public Client(String username) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    client = new ApiClient().setBasePath("http://localhost:8080");
    server = new DefaultApi(client);
    cryptoAgent = new CryptoAgent(username);

    key = new PubKey().value(cryptoAgent.getStringPublicKey());
  }

  public Map<String, AbstractCommand> getCommands() {
    createCommands();
    return commands;
  }

  private synchronized void createCommands() {
    if (commands == null) {
      AbstractCommand[] commandsArr = {
          new AuditCommand(),
          new CheckAccountCommand(),
          new ReceiveAmountCommand(),
          new RegisterCommand(),
          new SendAmountCommand()
      };

      commands = Arrays.stream(commandsArr)
          .collect(Collectors.toMap(AbstractCommand::getName, Function.identity()));
    }
  }
}
