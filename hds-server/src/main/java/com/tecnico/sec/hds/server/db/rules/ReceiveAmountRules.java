package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
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


  public Optional<Transaction> receiveAmount(String transHash, String sourceKey, String destKey,
                                             long amount, String lastHash, String signature) throws DBException {
    return queryHelpers.withTransaction(conn -> {

      AccountQueries accountQueries = new AccountQueries(conn);
      TransactionQueries transferQueries = new TransactionQueries(conn);

      Optional<Transaction> transaction = transferQueries.getTransactionByHash(transHash);

      if (transaction.isPresent() && transaction.get().pending) {
        long transAmount = transaction.get().amount;
        String transSourceKey = transaction.get().sourceKey;
        String transDestKey = transaction.get().destKey;

        Optional<Transaction> destLastTransfer = transferQueries.getLastTransaction(destKey);
        Optional<String> destLastTransferHash = destLastTransfer.map(t -> t.hash);
        Optional<String> receiveHash = Optional.of(transHash);
        String lastTransferHash = destLastTransferHash.orElse("");

        if (transSourceKey.equals(sourceKey) && transDestKey.equals(destKey) && transAmount == amount
            && lastTransferHash.equals(lastHash)) {
          String newHash = new ChainHelper().generateTransactionHash(
              destLastTransferHash,
              receiveHash,
              sourceKey,
              destKey,
              amount,
              ChainHelper.TransactionType.ACCEPT,
              signature);

          transferQueries.updateTransactionPendingState(transHash, false);
          transferQueries.insertNewTransaction(sourceKey, destKey, amount, false, true, signature, newHash, receiveHash);

          return transferQueries.getLastInsertedTransaction();
        }
      }
      return Optional.empty();
    });
  }

}
