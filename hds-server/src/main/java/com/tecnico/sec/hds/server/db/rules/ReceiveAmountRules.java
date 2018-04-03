package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.domain.Transaction;
import com.tecnico.sec.hds.util.crypto.ChainHelper;

import java.util.Optional;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withTransaction;

public class ReceiveAmountRules {

  public Optional<Transaction> receiveAmount(String transHash, String signature) throws DBException {
    return withTransaction(conn -> {

      AccountQueries accountQueries = new AccountQueries(conn);
      TransactionQueries transferQueries = new TransactionQueries(conn);

      Optional<Transaction> transaction = transferQueries.getTransactionByHash(transHash);

      if (transaction.isPresent() && transaction.get().pending) {
        float amount = transaction.get().amount;
        String sourceKey = transaction.get().sourceKey;
        String destKey = transaction.get().destKey;

        Optional<Transaction> destLastTransfer = transferQueries.getLastTransaction(destKey);
        Optional<String> destLastTransferHash = destLastTransfer.map(t -> t.hash);

        String newHash = new ChainHelper().generateTransactionHash(
            destLastTransferHash,
            sourceKey,
            destKey,
            amount,
            ChainHelper.TransactionType.ACCEPT,
            signature);

        float balance = accountQueries.getBalance(destKey);

        transferQueries.updateTransactionPendingState(transHash, false);
        transferQueries.insertNewTransaction(sourceKey, destKey, amount, false, true, signature, newHash);
        accountQueries.updateAccount(destKey, balance + amount);

        return transferQueries.getLastInsertedTransaction();

      } else {
        return Optional.empty();
      }
    });
  }

}
