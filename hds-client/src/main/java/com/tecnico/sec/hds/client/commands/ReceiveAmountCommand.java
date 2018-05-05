package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import com.tecnico.sec.hds.client.commands.util.TransactionGetter;
import domain.Transaction;
import io.swagger.client.model.*;
import io.swagger.client.model.Signature;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.List;

public class ReceiveAmountCommand extends AbstractCommand {
  private static final String name = "receive_amount";

  @Override
  public void doRun(Client client, String[] args)
    throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, CertificateException, KeyStoreException, UnrecoverableKeyException {

    PubKey sourceKey = new PubKey();

    sourceKey.setValue(args[0]);

    Hash hash = new Hash();
    hash.setValue(args[1]);

    TransactionInformation transaction = null;

    AuditRequest auditRequest = new AuditRequest();

    auditRequest.setPublicKey(sourceKey);

    AuditResponse auditResponse = client.server.audit(auditRequest);

    String transactionListMessage = TransactionGetter.getTransactionListMessage(auditResponse.getList());
    List<Transaction> transactions = TransactionGetter.InformationToTransaction(auditResponse.getList());
    Collections.reverse(transactions);

    if (/*client.cryptoAgent.verifyBankSignature(transactionListMessage, auditResponse.getSignature().getValue())
      && client.cryptoAgent.verifyTransactionsSignature(transactions)
      && client.chainHelper.verifyTransaction(transactions, sourceKey.getValue())*/ true) {

      for (TransactionInformation transactionInformation : auditResponse.getList()) {

        if (transactionInformation.getSendHash().getValue().equals(hash.getValue())) {
          transaction = transactionInformation;
        }
      }
    }

    if (transaction != null) {

      ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest();

      receiveAmountRequest.setSourceKey(sourceKey);

      receiveAmountRequest.setDestKey(client.key);

      receiveAmountRequest.amount(Integer.valueOf(transaction.getAmount()));

      receiveAmountRequest.setLastHash(client.getLastHash());

      receiveAmountRequest.setTransHash(hash);



      Signature signature = new Signature();
      signature.setValue(client.cryptoAgent.generateSignature(sourceKey.getValue() + client.key.getValue()
        + transaction.getAmount() + client.getLastHash().getValue() + hash.getValue()));


      receiveAmountRequest.setSignature(signature);

      ReceiveAmountResponse receiveAmountResponse = client.server.receiveAmount(receiveAmountRequest);

      hash = receiveAmountResponse.getNewHash();

      if (hash.getValue() != null &&
        client.cryptoAgent.verifyBankSignature(hash.getValue() + receiveAmountResponse.getMessage(),
          receiveAmountResponse.getSignature().getValue())) {
        if (receiveAmountResponse.isSuccess()) {
          client.setLastHash(hash);
        }
        System.out.println(receiveAmountResponse.getMessage());
      } else {
        System.out.println("I caught you fake!!");
      }
    }

  }

  @Override
  public String getName() {
    return name;
  }
}
