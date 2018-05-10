package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import domain.Transaction;

import java.util.Optional;

public class ReceiveAmountRules {
  private final QueryHelpers queryHelpers;

  public ReceiveAmountRules(QueryHelpers queryHelpers) {
    this.queryHelpers = queryHelpers;
  }


  public boolean verifyReceiveAmount(String transHash, String sourceKey, String destKey,
                                     long amount, String newHash) {
    try {
      return queryHelpers.withConnection(conn -> {
        TransactionQueries transferQueries = new TransactionQueries(conn);
        Optional<Transaction> transactionToReceive = transferQueries.getTransactionByHash(transHash);

        if (transactionToReceive.isPresent() && transactionToReceive.get().pending) {
          long transAmount = transactionToReceive.get().amount;
          String transSourceKey = transactionToReceive.get().sourceKey;
          String transDestKey = transactionToReceive.get().destKey;

          Optional<Transaction> destLastTransfer = transferQueries.getLastTransaction(destKey);
          Optional<String> destLastTransferHash = destLastTransfer.map(t -> t.hash);
          Optional<String> receiveHash = Optional.of(transHash);

          String realNewHash = new ChainHelper().generateTransactionHash(
              destLastTransferHash,
              receiveHash,
              sourceKey,
              destKey,
              amount,
              ChainHelper.TransactionType.ACCEPT);

          return transSourceKey.equals(sourceKey) &&
              transDestKey.equals(destKey) &&
              transAmount == amount &&
              realNewHash.equals(newHash);
        } else {
          return false;
        }
      });
    } catch (DBException e) {
      System.err.println("Failed to verify receive amount: ");
      e.printStackTrace();
      return false;
    }
  }

  public Optional<Transaction> receiveAmount(String transHash, String sourceKey, String destKey,
                                             long amount, String newHash, String signature) throws DBException {
    return queryHelpers.withTransaction(conn -> {
      TransactionQueries transferQueries = new TransactionQueries(conn);
      Optional<String> receiveHash = Optional.of(transHash);

      transferQueries.updateTransactionPendingState(transHash, false);
      transferQueries.insertNewTransaction(sourceKey, destKey, amount, false, true, signature, newHash, receiveHash);

      return transferQueries.getLastInsertedTransaction();
    });
  }

}
