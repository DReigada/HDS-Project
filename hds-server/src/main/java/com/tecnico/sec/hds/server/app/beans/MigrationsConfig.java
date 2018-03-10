package com.tecnico.sec.hds.server.app.beans;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationsConfig {
  private static final Logger logger = LoggerFactory.getLogger(MigrationsConfig.class);

  @Bean
  public ApplicationRunner migrations() {
    return args -> {
      Flyway flyway = new Flyway();
      flyway.setDataSource("jdbc:sqlite:HDSDB.db", "", "");
      logger.info("Executing migrations");
      flyway.migrate();
    };
  }
}
