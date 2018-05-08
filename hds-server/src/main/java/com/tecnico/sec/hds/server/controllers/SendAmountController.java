package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastSession;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.db.rules.GetTransactionRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import domain.Transaction;
import io.swagger.api.SendAmountApi;
import io.swagger.model.Hash;
import io.swagger.model.SendAmountRequest;
import io.swagger.model.SendAmountResponse;
import io.swagger.model.Signature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Optional;

@Controller
public class SendAmountController implements SendAmountApi {
  private final CryptoAgent cryptoAgent;
  private final ServersWrapper serversWrapper;
  private final ReliableBroadcastHelper reliableBroadcastHelper;
  private final GetTransactionRules getTransactionRules;

  public SendAmountController(CryptoAgent cryptoAgent, QueryHelpers queryHelpers,
                              ServersWrapper serversWrapper, ReliableBroadcastHelper reliableBroadcastHelper) {
    this.cryptoAgent = cryptoAgent;
    this.serversWrapper = serversWrapper;
    this.reliableBroadcastHelper = reliableBroadcastHelper;
    getTransactionRules = new GetTransactionRules(queryHelpers);
  }

  @Override
  public ResponseEntity<SendAmountResponse> sendAmount(@Valid @RequestBody SendAmountRequest body) {
    String sourceKey = body.getSourceKey().getValue();
    String destKey = body.getDestKey().getValue();
    long amount = body.getAmount().longValue();
    String hash = body.getHash().getValue();
    String clientSignature = body.getSignature().getValue();


    SendAmountResponse response = new SendAmountResponse();
    String message;
    boolean success;
    Hash newHash = new Hash();
    Signature signature = new Signature();

    if (cryptoAgent.verifySignature(sourceKey + destKey + String.valueOf(amount) + hash, clientSignature, sourceKey)) {
      ReliableBroadcastSession session = reliableBroadcastHelper.createIfNotExists(hash);

      if (session.canBroadcastEcho()) {
        serversWrapper.broadcast(); // TODO echo
        session.wait();
      }

      Optional<Transaction> result = getTransactionRules.getTransaction(hash);

      if (result.isPresent()) {
        newHash.setValue(result.get().hash);
        response.setNewHash(newHash);
        success = true;
        message = "Transaction Successful";
      } else {
        success = false;
        message = "Transaction Failed";
      }
      response.setNewHash(newHash);
    } else {
      success = false;
      message = "Nice try Hacker wanna be";
    }

    signature.setValue(cryptoAgent.generateSignature(newHash.getValue() + message));
    response.setSuccess(success);
    response.setMessage(message);
    response.setSignature(signature);

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

}

