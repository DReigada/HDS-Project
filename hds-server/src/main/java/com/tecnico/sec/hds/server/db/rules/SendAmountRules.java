package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.domain.Transaction;
import com.tecnico.sec.hds.util.crypto.ChainHelper;

import java.util.Optional;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withTransaction;

public class SendAmountRules {

  public Optional<Transaction> sendAmount(String sourceKey, String destKey, float amount, String signature) throws DBException {
    return withTransaction(conn -> {

      AccountQueries accountQueries = new AccountQueries(conn);
      TransactionQueries transferQueries = new TransactionQueries(conn);

      Optional<Transaction> sourceLastTransfer = transferQueries.getLastTransaction(sourceKey);
      Optional<String> sourceLastTransferHash = sourceLastTransfer.map(Transaction::getHash);

      String newHash = new ChainHelper().generateTransactionHash(
          sourceLastTransferHash,
          sourceKey,
          destKey,
          amount,
          ChainHelper.TransactionType.SEND_AMOUNT,
          signature);

      float sourceBalance = accountQueries.getBalance(sourceKey);

      if (amount <= sourceBalance && amount > 0) {
        int accountUpdated = accountQueries.updateAccount(sourceKey, sourceBalance - amount);
        int insertTransaction = transferQueries.insertNewTransaction(sourceKey, destKey, amount, true, signature, newHash);

        return transferQueries.getLastInsertedTransaction();
      }

      return Optional.empty();
    });

  }

}



