package com.tecnico.sec.hds.server.app.beans;

import com.tecnico.sec.hds.server.db.commands.util.Migrations;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationsConfig {
  @Bean
  public ApplicationRunner migrations() {
    return args -> Migrations.migrate();
  }
}
