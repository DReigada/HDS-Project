package com.tecnico.sec.hds.server.util;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;

import java.util.UUID;

public class TestHelper {
  public static String createRandomAccount() throws DBException {
    return QueryHelpers.withConnection(conn -> {
      AccountQueries acc = new AccountQueries(conn);
      String key = randomPublicKey();
      acc.register(key);
      return key;
    });
  }

  // TODO change this for valid key
  private static String randomPublicKey() {
    return UUID.randomUUID().toString();
  }
}
