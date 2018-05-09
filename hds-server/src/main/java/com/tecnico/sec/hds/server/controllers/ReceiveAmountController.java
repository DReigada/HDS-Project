package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.server.controllers.converters.Converters;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastSession;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.db.rules.GetTransactionRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import domain.Transaction;
import io.swagger.annotations.ApiParam;
import io.swagger.api.ReceiveAmountApi;
import io.swagger.client.model.TransactionInformation;
import io.swagger.model.Hash;
import io.swagger.model.ReceiveAmountRequest;
import io.swagger.model.ReceiveAmountResponse;
import io.swagger.model.Signature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Optional;


@Controller
public class ReceiveAmountController implements ReceiveAmountApi {
  private final CryptoAgent cryptoAgent;
  private final ReliableBroadcastHelper reliableBroadcastHelper;
  private final ServersWrapper serversWrapper;
  private final GetTransactionRules getTransactionRules;

  public ReceiveAmountController(CryptoAgent cryptoAgent, QueryHelpers queryHelpers,
                                 ServersWrapper serversWrapper, ReliableBroadcastHelper reliableBroadcastHelper) {
    this.cryptoAgent = cryptoAgent;
    this.serversWrapper = serversWrapper;
    this.reliableBroadcastHelper = reliableBroadcastHelper;
    getTransactionRules = new GetTransactionRules(queryHelpers);
  }

  @Override
  public ResponseEntity<ReceiveAmountResponse> receiveAmount(@ApiParam(required = true) @RequestBody @Valid ReceiveAmountRequest body) {
    String sourceKey = body.getSourceKey().getValue();
    String destKey = body.getDestKey().getValue();
    long amount = body.getAmount();
    String hash = body.getHash().getValue();
    String receiveHash = body.getTransHash().getValue();
    String transSignature = body.getSignature().getValue();

    ReceiveAmountResponse response = new ReceiveAmountResponse();
    String message;
    boolean success;
    Hash newHash = new Hash();
    Signature signature = new Signature();

    if (cryptoAgent.verifySignature(sourceKey + destKey + amount + hash + receiveHash, transSignature, destKey)) {
      ReliableBroadcastSession session = reliableBroadcastHelper.createIfNotExists(hash);

      Optional<Transaction> result = Optional.empty();

      try {
        session.runIfEchoIsPossibleAndWait(() -> {
          TransactionInformation trans = Converters.createTransaction(sourceKey, destKey, amount, true, true, hash, receiveHash, transSignature);
          serversWrapper.broadcast(reliableBroadcastHelper.createEchoRequest(trans));
        });

        result = getTransactionRules.getTransaction(hash);
      } catch (DBException e) {
        System.err.println("Failed to receive amount:");
        e.printStackTrace();
      }

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
    System.out.println(newHash.getValue() + " " + message);
    System.out.println(signature.getValue());
    response.setSuccess(success);
    response.setMessage(message);
    response.setSignature(signature);

    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
