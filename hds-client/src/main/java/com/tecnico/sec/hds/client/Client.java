package com.tecnico.sec.hds.client;

import com.tecnico.sec.hds.client.commands.*;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.client.model.Hash;
import io.swagger.client.model.PubKey;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Client {
  public final ServersWrapper server;
  public final CryptoAgent cryptoAgent;
  public final PubKey key;
  public final ChainHelper chainHelper;

  private Hash lastHash;

  private Map<String, AbstractCommand> commands;

  public Client(String username, String password)
      throws NoSuchAlgorithmException, IOException, UnrecoverableKeyException, CertificateException, KeyStoreException, OperatorCreationException {
    server = new ServersWrapper();
    cryptoAgent = new CryptoAgent(username, password);
    key = new PubKey().value(cryptoAgent.getStringPublicKey());
    lastHash = new Hash();
    lastHash.setValue("");
    chainHelper = new ChainHelper();
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
