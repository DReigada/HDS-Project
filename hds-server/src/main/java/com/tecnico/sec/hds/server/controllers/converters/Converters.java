package com.tecnico.sec.hds.server.controllers.converters;

import io.swagger.client.model.*;

public class Converters {

  public static BroadcastRequest serverBroadcastRequestToClient(io.swagger.model.BroadcastRequest body) {
    return
        new BroadcastRequest()
            .publicKey(new PubKey().value(body.getPublicKey().getValue()))
            .isEcho(body.isIsEcho())
            .isReady(body.isIsReady())
            .transaction(serverTransactionToClient(body.getTransaction()));
  }

  public static TransactionInformation serverTransactionToClient(io.swagger.model.TransactionInformation trans) {
    return new TransactionInformation()
        .sourceKey(trans.getSourceKey())
        .destKey(trans.getDestKey())
        .amount(trans.getAmount())
        .pending(trans.isPending())
        .receive(trans.isReceive())
        .signature(new Signature().value(trans.getSignature().getValue()))
        .sendHash(new Hash().value(trans.getSendHash().getValue()))
        .receiveHash(new Hash().value(trans.getReceiveHash().getValue()));
  }

  public static TransactionInformation createTransaction(String sourceKey, String destKey, long amount, boolean pending,
                                                         boolean receive, String hash, String receiveHash, String transSignature) {
    return new TransactionInformation()
        .sourceKey(sourceKey)
        .destKey(destKey)
        .amount(Long.toString(amount))
        .pending(pending)
        .receive(receive)
        .sendHash(new Hash().value(hash))
        .receiveHash(new Hash().value(receiveHash))
        .signature(new Signature().value(transSignature));
  }
}
