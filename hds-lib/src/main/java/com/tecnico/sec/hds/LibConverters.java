package com.tecnico.sec.hds;

import io.swagger.client.model.PubKey;
import io.swagger.client.model.ReceiveAmountRequest;
import io.swagger.client.model.SendAmountRequest;
import io.swagger.client.model.TransactionInformation;

public class LibConverters {

  public static SendAmountRequest transactionInformationToSendAmountRequest(
      TransactionInformation trans, boolean isWriteBack) {
    return new SendAmountRequest()
        .sourceKey(new PubKey().value(trans.getSourceKey()))
        .destKey(new PubKey().value(trans.getDestKey()))
        .amount(Integer.valueOf(trans.getAmount()))
        .hash(trans.getSendHash())
        .signature(trans.getSignature())
        .writeBack(isWriteBack);
  }

  public static ReceiveAmountRequest transactionInformationToReceiveAmountRequest(
      TransactionInformation trans, boolean isWriteBack) {
    return new ReceiveAmountRequest()
        .sourceKey(new PubKey().value(trans.getSourceKey()))
        .destKey(new PubKey().value(trans.getDestKey()))
        .amount(Integer.valueOf(trans.getAmount()))
        .hash(trans.getSendHash())
        .transHash(trans.getReceiveHash())
        .signature(trans.getSignature())
        .writeBack(isWriteBack);
  }
}
