package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.TransferQueries;
import com.tecnico.sec.hds.server.domain.Transfer;

import java.util.List;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withConnection;


public class CheckAmountRules {

  public float getBalance(String publicKey) throws DBException{
    return withConnection(conn -> {
      AccountQueries accountQueries = new AccountQueries(conn);

      return accountQueries.getBalance(publicKey);

    });
  }

  public List<Transfer> getPendingTransfers(String publicKey) throws  DBException{
    return withConnection(conn -> {
      TransferQueries transferQueries = new TransferQueries(conn);

      return transferQueries.getPendingTransfers(publicKey);
    });
  }
}
