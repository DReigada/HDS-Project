package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.domain.Transaction;

import java.util.List;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withConnection;


public class CheckAccountRules {

  public long getBalance(String publicKey) throws DBException {
    return withConnection(conn -> {
      TransactionQueries transactionQueries = new TransactionQueries(conn);

      return transactionQueries.getBalance(publicKey);

    });
  }

  public List<Transaction> getPendingTransactions(String publicKey) throws DBException {
    return withConnection(conn -> {
      TransactionQueries transferQueries = new TransactionQueries(conn);

      return transferQueries.getPendingTransactions(publicKey);
    });
  }
}
