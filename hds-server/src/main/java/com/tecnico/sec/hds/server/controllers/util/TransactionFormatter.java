package com.tecnico.sec.hds.server.controllers.util;

import domain.Transaction;
import io.swagger.model.Hash;
import io.swagger.model.Signature;
import io.swagger.model.TransactionInformation;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionFormatter {

  public static TransactionInformation getTransactionInformation(Transaction transaction){
    Hash hash = new Hash().value(transaction.hash);
    Signature signature = new Signature().value(transaction.signature);
    TransactionInformation transactionInformation = new TransactionInformation();
    transactionInformation.setTransID(transaction.transID);
    transactionInformation.setSourceKey(transaction.sourceKey);
    transactionInformation.setDestKey(transaction.destKey);
    transactionInformation.setAmount("" + transaction.amount);
    transactionInformation.setPending(transaction.pending);
    transactionInformation.setReceive(transaction.receive);
    transactionInformation.setSignature(signature);
    transactionInformation.setHash(hash);
    return transactionInformation;
  }

  public static String convertTransactionsToString(List<TransactionInformation> transactions){
    return transactions.stream().parallel().map(t->t.toString()).collect(Collectors.joining("\n")) ;
  }
}
