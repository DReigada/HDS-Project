package com.tecnico.sec.hds.client;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.client.commands.*;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Client {
  public final ServersWrapper server;

  private Map<String, AbstractCommand> commands;

  public Client(String username, String password) throws IOException, GeneralSecurityException, OperatorCreationException {
    server = new ServersWrapper(username, password);
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
