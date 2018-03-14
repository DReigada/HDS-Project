package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.DBException;
import com.tecnico.sec.hds.server.domain.Transfer;

import java.util.List;

import com.tecnico.sec.hds.server.db.commands.TransferQueries;

import static com.tecnico.sec.hds.server.db.commands.QueryHelpers.withConnection;



public class AuditRules {

  public List<Transfer> audit(String publicKey) throws DBException{
    return withConnection( conn -> {

      TransferQueries transferQueriesQueries = new TransferQueries(conn);

      return transferQueriesQueries.getHistory(publicKey);

    });
  }

}

