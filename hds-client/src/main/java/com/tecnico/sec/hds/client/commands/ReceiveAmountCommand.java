package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import com.tecnico.sec.hds.client.commands.util.TransactionGetter;
import io.swagger.client.ApiException;
import io.swagger.client.model.*;
import io.swagger.client.model.Signature;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class ReceiveAmountCommand extends AbstractCommand {
  private static final String name = "receive_amount";

  @Override
  public void doRun(Client client, String[] args)
      throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, CertificateException, KeyStoreException, UnrecoverableKeyException {
    TransactionGetter transactionGetter = new TransactionGetter();
    Hash hash = new Hash();
    hash.setValue(args[0]);

    GetTransactionRequest getTransactionRequest = new GetTransactionRequest();

    getTransactionRequest.setHash(hash);

    GetTransactionResponse getTransactionResponse = client.server.getTransaction(getTransactionRequest);

    if(!(getTransactionResponse.getTransaction() != null &&
      client.cryptoAgent.verifyBankSignature(transactionGetter.getTransactionListMessage(getTransactionResponse.getTransaction()),
        getTransactionResponse.getSignature().getValue()))){

      System.out.println("Transaction does not exist");

    }

    TransactionInformation transaction = getTransactionResponse.getTransaction();

    ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest();

    PubKey sourceKey = new PubKey();

    sourceKey.setValue(transaction.getSourceKey());

    receiveAmountRequest.setSourceKey(sourceKey);

    receiveAmountRequest.setDestKey(client.key);

    receiveAmountRequest.amount(Integer.valueOf(transaction.getAmount()));

    receiveAmountRequest.setLastHash(client.getLastHash());

    Signature transSignature =  new Signature();

    transSignature.setValue(client.cryptoAgent.generateSignature(sourceKey.getValue() + client.key.getValue()
      + transaction.getAmount() + client.getLastHash().getValue()));

    receiveAmountRequest.setTransSignature(transSignature);

    //transactionGetter.getTransactionListMessage(getTransactionResponse.getTransaction());

    receiveAmountRequest.setTransHash(hash);

    Signature signature = new Signature();

    signature.setValue(client.cryptoAgent.generateSignature(transSignature.getValue() + hash.getValue()));

    receiveAmountRequest.signature(signature);

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

  @Override
  public String getName() {
    return name;
  }
}
