package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import domain.Transaction;

import java.util.Optional;

import static com.tecnico.sec.hds.server.db.commands.util.QueryHelpers.withTransaction;

public class SendAmountRules {

  public Optional<Transaction> sendAmount(String sourceKey, String destKey, long amount, String signature, String lastHash) throws DBException {

    return withTransaction(conn -> {

      AccountQueries accountQueries = new AccountQueries(conn);
      TransactionQueries transferQueries = new TransactionQueries(conn);

      Optional<Transaction> sourceLastTransfer = transferQueries.getLastTransaction(sourceKey);
      Optional<String> sourceLastTransferHash = sourceLastTransfer.map(t -> t.hash);

      String lastTransferHash = sourceLastTransferHash.orElse("");

      if(lastTransferHash.equals(lastHash)) {


        String newHash = new ChainHelper().generateTransactionHash(
          sourceLastTransferHash,
          Optional.empty(),
          sourceKey,
          destKey,
          amount,
          ChainHelper.TransactionType.SEND_AMOUNT,
          signature);

        long sourceBalance = transferQueries.getBalance(sourceKey);

        if (amount <= sourceBalance && amount > 0) {
          transferQueries.insertNewTransaction(sourceKey, destKey, amount, true, false, signature, newHash, Optional.empty());

          return transferQueries.getLastInsertedTransaction();
        }
      }
      return Optional.empty();
    });

  }

}



