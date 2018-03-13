package com.tecnico.sec.hds.server.rules;

import com.tecnico.sec.hds.server.db.commands.AccountTableQueries;

import java.sql.Connection;

import static com.tecnico.sec.hds.server.db.commands.QueryHelpers.withTransaction;

public class AccountRules {

//  public boolean send(String sourceKey, String destKey, float ammount) {
//
//    return withTransaction(conn -> {
//      sendAmount sendAmountQueries = new sendAmount();
//      amountCommand amountCommandQueries =
//
//      float sourceBalance = sendAmountQueries.getBalance(sourceKey);
//      if (amount <= sourceBalance){
//        boolean accountUpdated = updateAccount(sourceKey, sourceBalance - amount);
//        boolean transferInserted = insertNewTransfer(sourceKey, destKey, amount);
//        return (accountUpdated && transferInserted);
//      }
//      return false;
//    });
//  }


  public String insert(String name) {
    return withTransaction(conn -> {
      AccountTableQueries accountQueries = new AccountTableQueries(conn);
      AccountTableQueries transfersQueries = new AccountTableQueries(conn);

      transfersQueries.insert(name);
      return accountQueries.insert(name);
    });
  }

  public String bla(Connection conn) {
    return "";
  }
}
