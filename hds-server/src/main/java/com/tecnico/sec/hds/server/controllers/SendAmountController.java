package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.SendAmountRules;
import com.tecnico.sec.hds.server.domain.Transaction;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.SendAmountApi;
import io.swagger.api.SendAmountApiController;
import io.swagger.model.Hash;
import io.swagger.model.SendAmountRequest;
import io.swagger.model.SendAmountResponse;
import io.swagger.model.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Optional;

@Controller
public class SendAmountController implements SendAmountApi {


  private static final Logger log = LoggerFactory.getLogger(SendAmountApiController.class);

  private CryptoAgent cryptoAgent;

  private SendAmountRules sendAmountRules;

  public SendAmountController() throws UnsupportedEncodingException, InvalidParameterSpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
    cryptoAgent = new CryptoAgent("bank", "strongPassword");
    sendAmountRules = new SendAmountRules();
  }

  @Override
  public ResponseEntity<SendAmountResponse> sendAmount(@Valid @RequestBody SendAmountRequest body) {
    String sourceKey = body.getSourceKey().getValue();
    String destKey = body.getDestKey().getValue();
    long amount = body.getAmount().longValue();
    String lastHash = body.getLastHash().getValue();
    String clientSignature = body.getSignature().getValue();


    SendAmountResponse response = new SendAmountResponse();
    String message;
    Hash newHash = new Hash();
    Signature signature = new Signature();

    try {
      if (cryptoAgent.verifySignature(sourceKey + destKey + String.valueOf(amount)
        + lastHash, clientSignature, sourceKey)) {
        Optional<Transaction> result = sendAmount(sourceKey, destKey, amount, clientSignature, lastHash);
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

    } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | InvalidKeySpecException e) {
      e.printStackTrace();
    }

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  public Optional<Transaction> sendAmount(String sourceKey, String destKey,
                                          long amount, String signature, String lastHash) {
    try {
      return sendAmountRules.sendAmount(sourceKey, destKey, amount, signature, lastHash);
    } catch (DBException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }
}

