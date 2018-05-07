package com.tecnico.sec.hds.server.util;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.db.rules.RegisterRules;

import java.util.UUID;

public class TestHelper {
  public static Tuple<String, String> createRandomAccount() throws DBException {
    RegisterRules registerRules = new RegisterRules(new QueryHelpers());
    String key = randomPublicKey();
    String hash = registerRules.register(key);
    return new Tuple(key,hash);
  }

  // TODO change this for valid key
  private static String randomPublicKey() {
    return UUID.randomUUID().toString();
  }
}
