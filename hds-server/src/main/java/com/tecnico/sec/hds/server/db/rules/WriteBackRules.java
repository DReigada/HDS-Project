package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import domain.Transaction;
import io.swagger.client.model.AuditRequest;
import io.swagger.client.model.AuditResponse;
import io.swagger.client.model.PubKey;
import io.swagger.client.model.TransactionInformation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WriteBackRules {

  private final ServersWrapper serversWrapper;
  private final AuditRules auditRules;
  private final QueryHelpers queryHelpers;

  public WriteBackRules(ServersWrapper serversWrapper, QueryHelpers queryHelpers) {
    this.serversWrapper = serversWrapper;
    auditRules = new AuditRules(queryHelpers);
    this.queryHelpers = queryHelpers;
  }

  public void doWriteBack(String publicKey) {
    AuditRequest request = new AuditRequest()
        .publicKey(new PubKey().value(publicKey));

    Optional<AuditResponse> response = serversWrapper.audit(request, false);
    List<Transaction> ownTrasactions;

    try {
      ownTrasactions = auditRules.audit(publicKey);
    } catch (DBException e) {
      System.err.println("Failed to get own transactions during writeback");
      e.printStackTrace();
      return;
    }

    if (response.isPresent()) {
      Set<String> ownTrasactionsHashes =
          ownTrasactions.stream()
              .map(a -> a.hash)
              .collect(Collectors.toSet());

      List<TransactionInformation> missingTransactions =
          response.get().getList()
              .stream()
              .filter(trans -> !ownTrasactionsHashes.contains(trans.getSendHash().getValue()))
              .collect(Collectors.toList());

      Collections.reverse(missingTransactions);

      try {
        queryHelpers.withTransaction(conn -> {
          TransactionQueries transactionQueries = new TransactionQueries(conn);

          missingTransactions.forEach(trans -> {
            try {
              transactionQueries.insertNewTransaction(trans.getSourceKey(), trans.getDestKey(),
                  Integer.valueOf(trans.getAmount()), trans.isPending(), trans.isReceive(),
                  trans.getSignature().getValue(), trans.getSendHash().getValue(),
                  trans.isReceive() ? Optional.of(trans.getReceiveHash().getValue()) : Optional.empty());
            } catch (DBException e) {
              System.err.println("Failed to insert transaction in write back");
              throw new RuntimeException(e);
            }
          });
          return true;
        });
      } catch (DBException e) {
        System.err.println("Failed to insert transaction in write back");
        e.printStackTrace();
      }
    } else {
      System.err.println("Could not call audit during WriteBack");
    }
  }

}
