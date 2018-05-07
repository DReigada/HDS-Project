package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import domain.Transaction;

import java.util.Optional;


public class GetTransactionRules {
  private final QueryHelpers queryHelpers;

  public GetTransactionRules(QueryHelpers queryHelpers) {
    this.queryHelpers = queryHelpers;
  }

  public Optional<Transaction> getTransaction(String hash) throws DBException {

    return queryHelpers.withConnection(conn -> {

      TransactionQueries transferQueries = new TransactionQueries(conn);

      Optional<Transaction> transaction = transferQueries.getTransactionByHash(hash);

      return transaction;

    });

  }

}
