package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import domain.Transaction;
import com.tecnico.sec.hds.util.crypto.ChainHelper;

import java.util.Optional;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withTransaction;

public class ReceiveAmountRules {

  public Optional<Transaction> receiveAmount(String receiveHashTrans, String sourceKey, String destKey,
                                             long amount, String newHashTrans, String signature) throws DBException {
    return withTransaction(conn -> {

      TransactionQueries transferQueries = new TransactionQueries(conn);


      Optional<Transaction> receiveTrans = transferQueries.getTransactionByHash(receiveHashTrans);


      Optional<Transaction> transaction = transferQueries.getLastTransaction(destKey);


      if (receiveTrans.isPresent() && receiveTrans.get().pending && transaction.isPresent()) {
        long transAmount = receiveTrans.get().amount;
        String transSourceKey = receiveTrans.get().sourceKey;
        String transDestKey = receiveTrans.get().destKey;
        Optional<String> receiveHash = Optional.of(receiveTrans.get().hash);
        if (transSourceKey.equals(sourceKey) && transDestKey.equals(destKey) && transAmount == amount
            && receiveHashTrans.equals(receiveHash.get())) {
            String newHash = new ChainHelper().generateTransactionHash(
            transaction.map(t -> t.hash),
              transaction.map(t -> t.receiveHash),
              transaction.get().sourceKey,
              transaction.get().destKey,
              transaction.get().amount,
              transaction.get().receive ? ChainHelper.TransactionType.ACCEPT : ChainHelper.TransactionType.SEND_AMOUNT,
              transaction.get().signature);


            if(newHash.equals(newHashTrans)){
              transferQueries.updateTransactionPendingState(receiveHashTrans, false);
              transferQueries.insertNewTransaction(sourceKey, destKey, amount, false, true, signature, newHashTrans, receiveHash);

              return transferQueries.getLastInsertedTransaction();
            }
        }
      }
      return Optional.empty();
    });
  }

}
