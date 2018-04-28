package com.tecnico.sec.hds.client;

import com.tecnico.sec.hds.client.commands.*;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.client.model.Hash;
import io.swagger.client.model.PubKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Client {
  public final ServersWrapper server;
  public final CryptoAgent cryptoAgent;
  public final PubKey key;

  private Hash lastHash;

  private Map<String, AbstractCommand> commands;

  public Client(String username, String password) throws InvalidParameterSpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
    server = new ServersWrapper();
    cryptoAgent = new CryptoAgent(username, password);
    key = new PubKey().value(cryptoAgent.getStringPublicKey());
    lastHash = new Hash();
    lastHash.setValue("");
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

  public Hash getLastHash() {
    return lastHash;
  }

  public void setLastHash(Hash lastHash) {
    this.lastHash = lastHash;
  }

}
