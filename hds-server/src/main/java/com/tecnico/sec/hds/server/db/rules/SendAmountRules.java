package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import domain.Transaction;

import java.util.Optional;

public class SendAmountRules {
  private final QueryHelpers queryHelpers;

  public SendAmountRules(QueryHelpers queryHelpers) {
    this.queryHelpers = queryHelpers;
  }

  public boolean verifySendAmount(String sourceKey, String destKey,
                                  long amount, String newHash) {
    try {
      return queryHelpers.withTransaction(conn -> {

        TransactionQueries transferQueries = new TransactionQueries(conn);

        Optional<Transaction> sourceLastTransfer = transferQueries.getLastTransaction(sourceKey);
        Optional<String> sourceLastTransferHash = sourceLastTransfer.map(t -> t.hash);

        String realNewHash = new ChainHelper().generateTransactionHash(
            sourceLastTransferHash,
            Optional.empty(),
            sourceKey,
            destKey,
            amount,
            ChainHelper.TransactionType.SEND_AMOUNT);

        long sourceBalance = transferQueries.getBalance(sourceKey);

        return realNewHash.equals(newHash) && amount <= sourceBalance && amount > 0;
      });
    } catch (DBException e) {
      System.err.println("Failed to verify send amount: ");
      e.printStackTrace();
      return false;
    }
  }

  public Optional<Transaction> sendAmount(String sourceKey, String destKey,
                                          long amount, String signature, String newHash) throws DBException {

    if (verifySendAmount(sourceKey, destKey, amount, newHash)) {
      return queryHelpers.withTransaction(conn -> {
        TransactionQueries transferQueries = new TransactionQueries(conn);

        transferQueries.insertNewTransaction(sourceKey, destKey, amount, true, false, signature, newHash, Optional.empty());

        return transferQueries.getLastInsertedTransaction();
      });
    } else {
      return Optional.empty();
    }

  }

}



