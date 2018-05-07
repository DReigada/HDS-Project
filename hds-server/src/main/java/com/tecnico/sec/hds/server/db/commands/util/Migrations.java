package com.tecnico.sec.hds.server.db.commands.util;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Migrations {
  private static final Logger logger = LoggerFactory.getLogger(Migrations.class);

  public static void migrate(QueryHelpers queryHelpers) {
    Flyway flyway = new Flyway();
    flyway.setDataSource(queryHelpers.dbURL, "", "");
    logger.info("Executing migrations");
    flyway.migrate();
  }
}
