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
public class ReceiveAmountController implements ReceiveAmountApi {

  private static final Logger log = LoggerFactory.getLogger(ReceiveAmountController.class);

  private CryptoAgent cryptoAgent;

  private ReceiveAmountRules receiveAmountRules;

  public ReceiveAmountController() throws InvalidParameterSpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
    cryptoAgent = new CryptoAgent("bank", "strongPassword");
    receiveAmountRules = new ReceiveAmountRules();
  }

  @Override
  public ResponseEntity<ReceiveAmountResponse> receiveAmount(@ApiParam(required = true) @RequestBody @Valid ReceiveAmountRequest body) {
    String sourceKey = body.getSourceKey().getValue();
    String destKey = body.getDestKey().getValue();
    long amount = body.getAmount();
    String lastHash = body.getLastHash().getValue();
    String transSignature = body.getTransSignature().getValue();
    String transHash = body.getTransHash().getValue();
    String clientSignature = body.getSignature().getValue();

    ReceiveAmountResponse response = new ReceiveAmountResponse();
    String message;
    Hash newHash = new Hash();
    Signature signature = new Signature();
    try {
      if (cryptoAgent.verifySignature(sourceKey + destKey + amount + lastHash, transSignature, destKey)
        && cryptoAgent.verifySignature(transSignature + transHash, clientSignature, destKey)) {
        Optional<Transaction> result = receiveAmount(sourceKey, destKey, amount, lastHash, transSignature, transHash);
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

  public Optional<Transaction> receiveAmount(String sourceKey, String destKey, long amount, String lastHash,
                                             String signature, String transHash) {
    try {
      return receiveAmountRules.receiveAmount(transHash, sourceKey, destKey, amount, lastHash, signature);
    } catch (DBException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }
}
