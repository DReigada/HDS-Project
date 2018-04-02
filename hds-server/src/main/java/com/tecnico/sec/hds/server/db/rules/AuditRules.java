package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.domain.Transaction;

import java.util.List;

import com.tecnico.sec.hds.server.db.commands.TransactionQueries;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withConnection;


public class AuditRules {

  public List<Transaction> audit(String publicKey) throws DBException {
    return withConnection(conn -> {

      TransactionQueries transferQueriesQueries = new TransactionQueries(conn);

      return transferQueriesQueries.getHistory(publicKey);

    });
  }

}

