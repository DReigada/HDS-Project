package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.ReceiveAmountRules;
import com.tecnico.sec.hds.server.domain.Transaction;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.annotations.ApiParam;
import io.swagger.api.ReceiveAmountApi;
import io.swagger.model.Hash;
import io.swagger.model.ReceiveAmountRequest;
import io.swagger.model.ReceiveAmountResponse;
import io.swagger.model.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;


@Controller
public class ReceiveAmountController implements ReceiveAmountApi {

  private static final Logger log = LoggerFactory.getLogger(ReceiveAmountController.class);

  private CryptoAgent cryptoAgent;

  private ReceiveAmountRules receiveAmountRules;

  public ReceiveAmountController() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    cryptoAgent = new CryptoAgent("bank");
    receiveAmountRules = new ReceiveAmountRules();
  }

  @Override
  public ResponseEntity<ReceiveAmountResponse> receiveAmount(@ApiParam(required = true) @RequestBody @Valid ReceiveAmountRequest body) {
    String publicKey = body.getPublicKey().getValue();
    String transHash = body.getTransHash().getValue();
    String lastHash = body.getLastHash().getValue();
    String clientSignature = body.getSignature().getValue();

    ReceiveAmountResponse response = new ReceiveAmountResponse();
    String message;
    Hash newHash = new Hash();
    Signature signature = new Signature();

    try {
      if (cryptoAgent.verifySignature(publicKey + transHash, clientSignature, publicKey)) {
        Optional<Transaction> result = receiveAmount(transHash, clientSignature ,lastHash);
        if (result.isPresent()) {  
          newHash.setValue(result.get().hash);
          response.setNewHash(newHash);
          message = "Transaction Successful";
        } else {
          message = "Transaction Failed";
        }
        response.setNewHash(newHash);
      } else {
        message = "Nice try Hacker wanna be";
      }
      signature.setValue(cryptoAgent.generateSignature(newHash.getValue() + message));
      response.setMessage(message);
      response.setSignature(signature);

    } catch (IOException | NoSuchAlgorithmException | SignatureException | InvalidKeyException | InvalidKeySpecException e) {
      e.printStackTrace();
    }

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  public Optional<Transaction> receiveAmount(String transHash, String signature, String lastHash) {
    try {
      return receiveAmountRules.receiveAmount(transHash, signature, lastHash);
    } catch (DBException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }
}
