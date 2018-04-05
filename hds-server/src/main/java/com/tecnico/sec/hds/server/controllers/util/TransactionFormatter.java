package com.tecnico.sec.hds.server.controllers.util;

import com.tecnico.sec.hds.server.domain.Transaction;
import io.swagger.model.Hash;
import io.swagger.model.Signature;
import io.swagger.model.TransactionInformation;

public class TransactionFormatter {

  public TransactionInformation getTransactionInformation(Transaction transaction){
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

  public String getTransactionListMessage(Transaction transaction){
    String transactionListMessage = "";
    transactionListMessage += "Transaction ID: " + transaction.transID + "\n";
    transactionListMessage += "Source Key: " + transaction.sourceKey + "\n";
    transactionListMessage += "Destination Key: " + transaction.destKey + "\n";
    transactionListMessage +=  "Amount: " + transaction.amount + "\n";
    transactionListMessage += "Pending: " + transaction.pending + "\n";
    transactionListMessage += "Received: " + transaction.receive + "\n";
    transactionListMessage += "Signature: " + transaction.signature + "\n";
    transactionListMessage += "Hash: " + transaction.hash + "\n";
    return transactionListMessage;
  }

}
