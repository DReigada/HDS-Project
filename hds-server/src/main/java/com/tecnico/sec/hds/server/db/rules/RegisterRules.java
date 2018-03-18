package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withConnection;

public class RegisterRules {

  public int register(String publicKey) throws DBException {
    return withConnection(conn -> {
      AccountQueries accountQueries = new AccountQueries(conn);
      return accountQueries.register(publicKey);
    });
  }

}
