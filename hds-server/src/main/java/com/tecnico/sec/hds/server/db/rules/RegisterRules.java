package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.ChainHelper;

import java.util.Optional;

public class RegisterRules {
  private final QueryHelpers queryHelpers;

  public RegisterRules(QueryHelpers queryHelpers) {
    this.queryHelpers = queryHelpers;
  }

  public String register(String publicKey) throws DBException {
    return queryHelpers.withTransaction(conn -> {
      AccountQueries accountQueries = new AccountQueries(conn);
      TransactionQueries transactionQueries = new TransactionQueries(conn);

      String newHash = new ChainHelper().generateTransactionHash(
          Optional.empty(),
          Optional.empty(),
          "",
          publicKey,
          1000,
          ChainHelper.TransactionType.ACCEPT);

      accountQueries.register(publicKey);

      transactionQueries.insertNewTransaction("0", publicKey, 1000, false, true, "", newHash, Optional.empty());

      return newHash;
    });
  }

}
