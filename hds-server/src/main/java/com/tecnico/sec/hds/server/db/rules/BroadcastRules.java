package com.tecnico.sec.hds.server.db.rules;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.server.controllers.converters.Converters;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastSession;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.client.model.PubKey;
import io.swagger.client.model.Signature;
import io.swagger.model.BroadcastRequest;
import io.swagger.model.TransactionInformation;

public class BroadcastRules {
  private final CryptoAgent cryptoAgent;
  private final ReliableBroadcastHelper reliableBroadcastHelper;
  private final ServersWrapper serversWrapper;
  private final ReceiveAmountRules receiveAmountRules;
  private final SendAmountRules sendAmountRules;

  public BroadcastRules(CryptoAgent cryptoAgent, ReliableBroadcastHelper reliableBroadcastHelper,
                        ServersWrapper serversWrapper, ReceiveAmountRules receiveAmountRules, SendAmountRules sendAmountRules) {
    this.cryptoAgent = cryptoAgent;
    this.reliableBroadcastHelper = reliableBroadcastHelper;
    this.serversWrapper = serversWrapper;
    this.receiveAmountRules = receiveAmountRules;
    this.sendAmountRules = sendAmountRules;
  }

  public void echoRule(BroadcastRequest body) {
    String serverPublicKey = body.getPublicKey().getValue();
    String broadcastSignature = body.getSignature().getValue();
    String echoMessage = reliableBroadcastHelper.getStringToSign(Converters.serverBroadcastRequestToClient(body));

    if (reliableBroadcastHelper.verifyMessage(serverPublicKey, echoMessage, broadcastSignature)) {
      ReliableBroadcastSession session = reliableBroadcastHelper.createIfNotExists(body.getTransaction().getSendHash().getValue());
      session.putEcho(serverPublicKey);

      if (session.canBroadcastReadyAfterEcho()) {
        startThread(() -> serversWrapper.broadcast(getReadySignedRequest(body)));
      }
    }
  }

  public void readyRule(BroadcastRequest body) {
    String serverPublicKey = body.getPublicKey().getValue();
    String broadcastSignature = body.getSignature().getValue();
    String readyMessage = reliableBroadcastHelper.getStringToSign(Converters.serverBroadcastRequestToClient(body));
    if (reliableBroadcastHelper.verifyMessage(serverPublicKey, readyMessage, broadcastSignature)) {
      ReliableBroadcastSession session = reliableBroadcastHelper.createIfNotExists(body.getTransaction().getSendHash().getValue());

      session.putReady(serverPublicKey);

      if (session.canBroadcastReadyAfterReady()) {
        startThread(() -> serversWrapper.broadcast(getReadySignedRequest(body)));
      }

      if (session.canDeliver()) {
        startThread(() -> {
          TransactionInformation trans = body.getTransaction();
          String hash = trans.getSendHash().getValue();
          String sourceKey = trans.getSourceKey();
          String destKey = trans.getDestKey();
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

          session.monitorNotify();
        });
      }
    }
  }

  private void startThread(Runnable r) {
    new Thread(r).start();
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
