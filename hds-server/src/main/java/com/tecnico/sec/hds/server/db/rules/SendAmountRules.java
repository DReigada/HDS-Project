package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.DBException;
import com.tecnico.sec.hds.server.db.commands.TransferQueries;

import static com.tecnico.sec.hds.server.db.commands.QueryHelpers.withTransaction;

public class SendAmountRules {

  public int sendAmount(String sourceKey, String destKey, float amount) throws DBException {

    return withTransaction( conn -> {

      AccountQueries accountQueries = new AccountQueries(conn);
      TransferQueries transferQueries = new TransferQueries(conn);

      float sourceBalance = accountQueries.getBalance(sourceKey);

      if (amount <= sourceBalance){
        int accountUpdated = accountQueries.updateAccount(sourceKey, sourceBalance - amount);
        int insertTransfer = transferQueries.insertNewTransfer(sourceKey, destKey, amount);

        if ((accountUpdated == 1) && (insertTransfer == 1)){
          return 1;
        }
      }

      return 0;

    });

  }

}



