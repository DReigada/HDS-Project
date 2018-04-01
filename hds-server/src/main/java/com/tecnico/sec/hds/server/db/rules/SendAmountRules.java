package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.TransactionQueries;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withTransaction;

public class SendAmountRules {

  public int sendAmount(String sourceKey, String destKey, float amount, String signature, String hash) throws DBException {

    return withTransaction( conn -> {

      AccountQueries accountQueries = new AccountQueries(conn);
      TransactionQueries transferQueries = new TransactionQueries(conn);

      float sourceBalance = accountQueries.getBalance(sourceKey);

      if (amount >= sourceBalance  && amount > 0){
        int accountUpdated = accountQueries.updateAccount(sourceKey, sourceBalance - amount);
        int insertTransaction = transferQueries.insertNewTransaction(sourceKey, destKey, amount, true, signature, hash);

        if ((accountUpdated == 1) && (insertTransaction == 1)){
          return 1;
        }
      }

      return 0;

    });

  }

}



