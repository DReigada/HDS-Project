package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import domain.Transaction;

import java.util.Optional;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withConnection;


public class GetTransactionRules {

  public Optional<Transaction> getTransaction(String hash) throws DBException {

    return withConnection(conn -> {

      TransactionQueries transferQueries = new TransactionQueries(conn);

      Optional<Transaction> transaction = transferQueries.getTransactionByHash(hash);

      return transaction;

    });

  }

}
