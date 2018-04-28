package com.tecnico.sec.hds.client.commands.util;

import domain.Transaction;
import io.swagger.client.model.TransactionInformation;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionGetter {

  public static String getTransactionListMessage(List<TransactionInformation> transactions){
    return transactions.stream().parallel().map(s -> s.toString()).collect(Collectors.joining("\n"));
  }

  public static String getTransactionListMessage(TransactionInformation transaction){
    return transaction.toString();
  }

  public static List<Transaction> InformationToTransaction(List<TransactionInformation> transactions){
    return transactions.stream().parallel().map(s ->
    new Transaction(s.getTransID(),
        s.getSourceKey(),
        s.getDestKey(),
        Long.valueOf(s.getAmount()),
        s.isPending(),
        s.isReceive(),
        s.getSignature().getValue(),
        s.getHash().getValue())
    ).collect(Collectors.toList());
  }
}
