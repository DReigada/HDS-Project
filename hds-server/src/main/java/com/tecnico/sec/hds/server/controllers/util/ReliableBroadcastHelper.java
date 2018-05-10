package com.tecnico.sec.hds.server.controllers.util;

import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.client.model.BroadcastRequest;
import io.swagger.client.model.PubKey;
import io.swagger.client.model.Signature;
import io.swagger.client.model.TransactionInformation;

import java.util.*;

public class ReliableBroadcastHelper {
  // the keys are the hashes
  private final Map<String, ReliableBroadcastSession> reliableBroadcastSessions;
  private final CryptoAgent cryptoAgent;
  private final Set<String> serverKeys;
  private final Set<String> serversUrls;
  private final int servers;

  public ReliableBroadcastHelper(CryptoAgent cryptoAgent, int numberOfServers, Set<String> serversUrls) {
    reliableBroadcastSessions = new HashMap<>();
    serverKeys = new HashSet<>();
    this.cryptoAgent = cryptoAgent;
    this.servers = numberOfServers;
    this.serversUrls = serversUrls;
  }

  /**
   * @param hash
   * @return true if the element existed, false otherwise
   */
  public synchronized ReliableBroadcastSession createIfNotExists(String hash) {
    return reliableBroadcastSessions.computeIfAbsent(hash, s -> new ReliableBroadcastSession(servers));
  }

  public synchronized Optional<ReliableBroadcastSession> get(String hash) {
    return Optional.ofNullable(reliableBroadcastSessions.get(hash));
  }

  public BroadcastRequest createEchoRequest(TransactionInformation transaction) {
    BroadcastRequest broadcastBody = new BroadcastRequest()
        .publicKey(new PubKey().value(cryptoAgent.getStringPublicKey()))
        .isEcho(true)
        .isReady(false)
        .transaction(transaction);

    return broadcastBody.signature(new Signature().value(signBroadcastBody(broadcastBody)));
  }

  public String signBroadcastBody(BroadcastRequest body) {
    return cryptoAgent.generateSignature(getStringToSign(body));
  }

  public String getStringToSign(BroadcastRequest body) {
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

  private void populateServerKeysList(Set<String> urls) {
    urls.forEach(url -> serverKeys.add(cryptoAgent.getBankPublicKey(url)));
  }

  public boolean verifyMessage(String key, String message, String signature) {
    if (serverKeys.isEmpty()) {
      populateServerKeysList(serversUrls);
    }
    return serverKeys.contains(key) && cryptoAgent.verifySignature(message, signature, key);
  }
}
