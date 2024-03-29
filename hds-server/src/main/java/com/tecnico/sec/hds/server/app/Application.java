package com.tecnico.sec.hds.server.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.tecnico.sec.hds.server.controllers", "com.tecnico.sec.hds.server.app.beans"})
public class Application {
  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    runApplication();
  }

  public static ConfigurableApplicationContext runApplication() {
    return SpringApplication.run(Application.class);
  }

}