package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.server.controllers.converters.Converters;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastSession;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.db.rules.ReceiveAmountRules;
import com.tecnico.sec.hds.server.db.rules.SendAmountRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.BroadcastApi;
import io.swagger.client.model.PubKey;
import io.swagger.client.model.Signature;
import io.swagger.model.BroadcastRequest;
import io.swagger.model.TransactionInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;


@Controller
public class BroadcastController implements BroadcastApi {
  private final CryptoAgent cryptoAgent;
  private final ReliableBroadcastHelper reliableBroadcastHelper;
  private final ServersWrapper serversWrapper;
  private final ReceiveAmountRules receiveAmountRules;
  private final SendAmountRules sendAmountRules;

  public BroadcastController(CryptoAgent cryptoAgent, QueryHelpers queryHelpers,
                             ServersWrapper serversWrapper, ReliableBroadcastHelper reliableBroadcastHelper) {
    this.cryptoAgent = cryptoAgent;
    this.serversWrapper = serversWrapper;
    this.reliableBroadcastHelper = reliableBroadcastHelper;
    receiveAmountRules = new ReceiveAmountRules(queryHelpers);
    sendAmountRules = new SendAmountRules(queryHelpers);
  }

  @Override
  public ResponseEntity<Void> broadcast(@Valid @RequestBody BroadcastRequest body) {
    // TODO: verify signature
    // verify if the public key exists on the server list

    if (body.isIsEcho()) {
      echoController(body);
    } else if (body.isIsReady()) {
      readyController(body);
    } else {
      System.err.println("This should never happen™");
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }

  private void echoController(BroadcastRequest body) {
    ReliableBroadcastSession session = reliableBroadcastHelper.createIfNotExists(body.getPublicKey().getValue());

    String serverPublicKey = body.getPublicKey().getValue();
    session.putEcho(serverPublicKey);

    if (session.canBroadcastReadyAfterEcho()) {
      runThread(() -> serversWrapper.broadcast(getReadySignedRequest(body)));
    }
  }

  private void readyController(BroadcastRequest body) {
    ReliableBroadcastSession session = reliableBroadcastHelper.createIfNotExists(body.getPublicKey().getValue());

    String serverPublicKey = body.getPublicKey().getValue();
    session.putReady(serverPublicKey);

    if (session.canBroadcastReadyAfterReady()) {
      runThread(() -> serversWrapper.broadcast(getReadySignedRequest(body)));
    }

    if (session.canDeliver()) {
      runThread(() -> {
        TransactionInformation trans = body.getTransaction();
        String hash = trans.getSendHash().getValue();
        String sourceKey = trans.getSourceKey();
        String destKey = trans.getSourceKey();
        int amount = Integer.valueOf(trans.getAmount());
        String signature = trans.getSignature().getValue();

        try {
          if (body.getTransaction().isReceive()) {
            String receiveHash = trans.getReceiveHash().getValue();
            receiveAmountRules.receiveAmount(receiveHash, sourceKey, destKey, amount, hash, signature);
          } else {
            sendAmountRules.sendAmount(sourceKey, destKey, amount, signature, hash);
          }
        } catch (DBException e) {
          System.err.println("Failed to store transaction: " + hash + " isReceive: " + trans.isReceive());
        }
        session.notifyAll();
      });
    }
  }

  private void runThread(Runnable r) {
    new Thread(r).run();
  }

  private io.swagger.client.model.BroadcastRequest getReadySignedRequest(BroadcastRequest body) {
    io.swagger.client.model.BroadcastRequest newBody =
        Converters.serverBroadcastRequestToClient(body)
            .publicKey(new PubKey().value(cryptoAgent.getStringPublicKey()))
            .isReady(true)
            .isEcho(false);

    return newBody
        .signature(new Signature().value(reliableBroadcastHelper.signBroadcastBody(newBody)));
  }
}
