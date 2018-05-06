package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import domain.Transaction;

import java.util.Optional;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withTransaction;

public class SendAmountRules {

  public Optional<Transaction> sendAmount(String sourceKey, String destKey, long amount, String signature, String newHashTrans) throws DBException {

    return withTransaction(conn -> {
      TransactionQueries transferQueries = new TransactionQueries(conn);

      Optional<Transaction> transaction = transferQueries.getLastTransaction(sourceKey);

      String newHash = new ChainHelper().generateTransactionHash(
          transaction.map(t -> t.hash),
          transaction.map(t -> t.receiveHash),
          transaction.get().sourceKey,
          transaction.get().destKey,
          transaction.get().amount,
          transaction.get().receive ? ChainHelper.TransactionType.ACCEPT : ChainHelper.TransactionType.SEND_AMOUNT,
          transaction.get().signature);

      if(newHash.equals(newHashTrans)) {


        long sourceBalance = transferQueries.getBalance(sourceKey);

        if (amount <= sourceBalance && amount > 0) {
          transferQueries.insertNewTransaction(sourceKey, destKey, amount, true, false, signature, newHashTrans, Optional.empty());

          return transferQueries.getLastInsertedTransaction();
        }
      }
      return Optional.empty();
    });

  }

}



