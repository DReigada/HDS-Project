package com.tecnico.sec.hds.server.app;

import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;

@SpringBootApplication
@ComponentScan(basePackages = {"com.tecnico.sec.hds.server.controllers", "com.tecnico.sec.hds.server.app.beans"})
public class Application {
  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static CryptoAgent cryptoAgent = createCryptoAgent();

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  private static CryptoAgent createCryptoAgent() {
    try {
      String ip = InetAddress.getLocalHost().getHostAddress().replace(".", "_");
      String port = System.getProperty("server.port");
      String fileName = "bank" + ip + "_" + port;

      logger.info("Creating keystore on file: " + fileName);

      return new CryptoAgent(fileName, fileName);
    } catch (GeneralSecurityException | IOException | OperatorCreationException e) {
      e.printStackTrace();
      System.exit(1);
      return null;
    }
  }
}