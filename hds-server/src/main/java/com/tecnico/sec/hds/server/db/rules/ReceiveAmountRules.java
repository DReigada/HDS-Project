package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.TransactionQueries;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withTransaction;

public class ReceiveAmountRules {

  public int receiveAmount(int transID, String publicKey) throws DBException {
    return withTransaction(conn -> {

      AccountQueries accountQueries = new AccountQueries(conn);
      TransactionQueries transferQueries = new TransactionQueries(conn);

      float balance = accountQueries.getBalance(publicKey);
      float amount = transferQueries.getTransAmount(transID, publicKey);


      int updateTrasnfer = transferQueries.updateTransaction(transID);
      int updateAccount = accountQueries.updateAccount(publicKey, balance + amount);

      if ((updateTrasnfer == 1) && (updateAccount == 1)) {
        return 1;
      }
      return 0;
    });
  }

}
