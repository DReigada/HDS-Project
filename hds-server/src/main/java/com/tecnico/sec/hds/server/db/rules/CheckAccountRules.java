package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import domain.Transaction;

import java.util.List;


public class CheckAccountRules {
  private final QueryHelpers queryHelpers;

  public CheckAccountRules(QueryHelpers queryHelpers) {
    this.queryHelpers = queryHelpers;
  }

  public long getBalance(String publicKey) throws DBException {
    return queryHelpers.withConnection(conn -> {
      TransactionQueries transactionQueries = new TransactionQueries(conn);

      return transactionQueries.getBalance(publicKey);

    });
  }

  public List<Transaction> getPendingTransactions(String publicKey) throws DBException {
    return queryHelpers.withConnection(conn -> {
      TransactionQueries transferQueries = new TransactionQueries(conn);

      return transferQueries.getPendingTransactions(publicKey);
    });
  }
}
