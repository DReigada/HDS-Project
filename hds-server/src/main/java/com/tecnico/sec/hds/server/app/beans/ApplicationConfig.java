package com.tecnico.sec.hds.server.app.beans;

import com.tecnico.sec.hds.server.db.commands.util.Migrations;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetAddress;

@Configuration
public class ApplicationConfig {
  private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

  @Bean
  public ApplicationRunner migrations(QueryHelpers queryHelpers) {
    return args -> Migrations.migrate(queryHelpers);
  }

  @Bean
  public CryptoAgent cryptoAgent() {
    return createCryptoAgent();
  }

  @Bean
  public QueryHelpers queryHelpers() {
    return new QueryHelpers();
  }

  private CryptoAgent createCryptoAgent() {
    try {
      String ip = InetAddress.getLocalHost().getCanonicalHostName().replace(".", "_");
      String port = System.getProperty("server.port", "8080");
      String fileName = "bank" + ip + "_" + port;

      logger.info("Creating keystore on file: " + fileName);

      return new CryptoAgent(fileName, fileName);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
      return null;
    }
  }
}
