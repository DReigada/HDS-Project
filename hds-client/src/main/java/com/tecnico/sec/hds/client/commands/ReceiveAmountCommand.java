package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.Hash;
import io.swagger.client.model.ReceiveAmountRequest;
import io.swagger.client.model.ReceiveAmountResponse;
import io.swagger.client.model.Signature;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class ReceiveAmountCommand extends AbstractCommand {
  private static final String name = "receive_amount";

  @Override
  public void doRun(Client client, String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, ApiException {

    Hash hash = new Hash();
    hash.setValue(args[0]);
    System.out.println(hash.getValue());

    Signature signature = new Signature();

    signature.setValue(client.cryptoAgent.generateSignature(client.key + hash.getValue()));

    ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest();
    receiveAmountRequest.publicKey(client.key);
    receiveAmountRequest.setTransHash(hash);
    receiveAmountRequest.signature(signature);

    ReceiveAmountResponse receiveAmountResponse = client.server.receiveAmount(receiveAmountRequest);

    hash = receiveAmountResponse.getNewHash();

    if (client.cryptoAgent.verifyBankSignature(hash.getValue() + receiveAmountResponse.getMessage(),
      receiveAmountResponse.getSignature().getValue())) {
      client.setLastHash(hash);
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
