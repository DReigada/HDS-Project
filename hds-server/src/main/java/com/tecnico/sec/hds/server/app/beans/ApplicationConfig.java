package com.tecnico.sec.hds.server.app.beans;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.db.commands.util.Migrations;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;

@Configuration
public class ApplicationConfig {
  private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

  @Bean
  public ApplicationRunner migrations(QueryHelpers queryHelpers) {
    return args -> Migrations.migrate(queryHelpers);
  }

  @Bean
  public CryptoAgent cryptoAgent(ServersWrapper wrapper) {
    return wrapper.securityHelper.cryptoAgent;
  }

  @Bean
  public ServersWrapper serversWrapper() {
    return createServersWrapper();
  }

  @Bean
  public ReliableBroadcastHelper reliableBroadcastHelper(CryptoAgent cryptoAgent, ServersWrapper serversWrapper) {
    return new ReliableBroadcastHelper(cryptoAgent, serversWrapper.getNumberOfServers());
  }

  @Bean
  public QueryHelpers queryHelpers() {
    return new QueryHelpers();
  }

  private ServersWrapper createServersWrapper() {
    try {
      String ip = InetAddress.getLocalHost().getCanonicalHostName().replace(".", "_");
      String port = System.getProperty("server.port", "8080");
      String fileName = "bank" + ip + "_" + port;

      logger.info("Creating keystore on file: " + fileName);

      return new ServersWrapper(fileName, fileName);
    } catch (GeneralSecurityException | OperatorCreationException | IOException e) {
      e.printStackTrace();
      System.exit(1);
      return null;
    }
  }
}
