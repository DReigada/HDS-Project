package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.domain.Transaction;

import java.util.List;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withConnection;


public class CheckAmountRules {

  public float getBalance(String publicKey) throws DBException {
    return withConnection(conn -> {
      AccountQueries accountQueries = new AccountQueries(conn);

      return accountQueries.getBalance(publicKey);

    });
  }

  public List<Transaction> getPendingTransactions(String publicKey) throws DBException {
    return withConnection(conn -> {
      TransactionQueries transferQueries = new TransactionQueries(conn);

      return transferQueries.getPendingTransactions(publicKey);
    });
  }
}
