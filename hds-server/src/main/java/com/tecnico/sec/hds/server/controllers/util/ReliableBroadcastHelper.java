package com.tecnico.sec.hds.server.controllers.util;

import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.client.model.BroadcastRequest;
import io.swagger.client.model.PubKey;
import io.swagger.client.model.TransactionInformation;

import java.util.HashMap;
import java.util.Map;

public class ReliableBroadcastHelper {
  // the keys are the hashes
  private final Map<String, ReliableBroadcastSession> reliableBroadcastSessions;
  private final CryptoAgent cryptoAgent;
  private final int servers;

  public ReliableBroadcastHelper(CryptoAgent cryptoAgent, int numberOfServers) {
    reliableBroadcastSessions = new HashMap<>();
    this.cryptoAgent = cryptoAgent;
    this.servers = numberOfServers;
  }

  /**
   * @param hash
   * @return true if the element existed, false otherwise
   */
  public synchronized ReliableBroadcastSession createIfNotExists(String hash) {
    return reliableBroadcastSessions.computeIfAbsent(hash, s -> new ReliableBroadcastSession(servers));
  }

  public synchronized ReliableBroadcastSession get(String hash) {
    return reliableBroadcastSessions.get(hash);
  }

  public BroadcastRequest createEchoRequest(TransactionInformation transaction) {
    return new BroadcastRequest()
        .publicKey(new PubKey().value(cryptoAgent.getStringPublicKey()))
        .isEcho(true)
        .isReady(false)
        .transaction(transaction);
  }

  public String signBroadcastBody(BroadcastRequest body) {
    return cryptoAgent.generateSignature(getStringToSign(body));
  }

  private String getStringToSign(BroadcastRequest body) {
    TransactionInformation trans = body.getTransaction();
    return
        body.getPublicKey().getValue() +
            body.isIsEcho() +
            body.isIsReady() +
            trans.getSourceKey() + trans.getDestKey() +
            trans.getAmount() +
            trans.getSendHash() +
            trans.getReceiveHash();
  }
}
