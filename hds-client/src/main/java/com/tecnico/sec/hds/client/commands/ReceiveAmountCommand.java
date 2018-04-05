package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.ReceiveAmountRequest;
import io.swagger.client.model.Signature;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class ReceiveAmountCommand extends AbstractCommand {
  private static final String name = "receive_amount";

  @Override
  public void doRun(Client client, String[] args) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, ApiException {
    String message = "";
    ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest();
    receiveAmountRequest.setPublicKey(client.key);
    //receiveAmountRequest.setTransHash(client);

    Signature signature = new Signature().value(client.cryptoAgent.generateSignature(message));
    receiveAmountRequest.signature(signature);
    client.server.receiveAmount(receiveAmountRequest);
  }

  @Override
  public String getName() {
    return name;
  }
}
