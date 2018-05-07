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

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}