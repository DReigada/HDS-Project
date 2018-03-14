package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.DBException;

import static com.tecnico.sec.hds.server.db.commands.QueryHelpers.withConnection;

public class RegisterRules {

  public int register(String publicKey) throws DBException {
    return withConnection(conn -> {
      AccountQueries accountQueries = new AccountQueries(conn);
      return accountQueries.register(publicKey);
    });
  }

}
