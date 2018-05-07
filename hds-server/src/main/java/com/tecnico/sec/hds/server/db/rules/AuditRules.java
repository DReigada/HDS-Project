package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import domain.Transaction;

import java.util.List;
import java.util.Optional;


public class AuditRules {
  private final QueryHelpers queryHelpers;

  public AuditRules(QueryHelpers queryHelpers) {
    this.queryHelpers = queryHelpers;
  }

  public List<Transaction> audit(String publicKey) throws DBException {
    return queryHelpers.withConnection(conn -> {

      TransactionQueries transferQueries = new TransactionQueries(conn);

      return transferQueries.getHistory(publicKey, Optional.empty());

    });
  }

}

