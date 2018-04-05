package com.tecnico.sec.hds.client.commands.util;

import io.swagger.client.model.TransactionInformation;

public class TransactionGetter {

  public String getTransactionListMessage(TransactionInformation transaction){
    String transactionListMessage = "";
    transactionListMessage += "Transaction ID: " + transaction.getTransID() + "\n";
    transactionListMessage += "Source Key: " + transaction.getSourceKey() + "\n";
    transactionListMessage += "Destination Key: " + transaction.getDestKey() + "\n";
    transactionListMessage += "Amount: " + transaction.getAmount() + "\n";
    transactionListMessage += "Pending: " + transaction.isPending() + "\n";
    transactionListMessage += "Received: " + transaction.isReceive() + "\n";
    transactionListMessage += "Signature: " + transaction.getSignature().getValue() + "\n";
    transactionListMessage += "Hash: " + transaction.getHash().getValue() + "\n";
    return transactionListMessage;
  }
}
