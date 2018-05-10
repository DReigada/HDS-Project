package com.tecnico.sec.hds.integrationTests.byzantineApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.tecnico.sec.hds.server.controllers", "com.tecnico.sec.hds.integrationTests.byzantineApp"})
public class ByzantineApplication {
  public static void main(String[] args) {
    runApplication();
  }

  public static ConfigurableApplicationContext runApplication() {
    return SpringApplication.run(ByzantineApplication.class);
  }

}