package com.tecnico.sec.hds.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.tecnico.sec.hds.controllers", "com.tecnico.sec.hds.app.beans"})
public class ServerProxyApp {
  private static final Logger logger = LoggerFactory.getLogger(com.tecnico.sec.hds.server.app.Application.class);

  public static void main(String[] args) {
    System.setProperty("type", args[0]);
    runApplication();
  }

  public static ConfigurableApplicationContext runApplication() {
    return SpringApplication.run(com.tecnico.sec.hds.server.app.Application.class);
  }

}
