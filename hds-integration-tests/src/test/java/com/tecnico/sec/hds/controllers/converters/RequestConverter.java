package com.tecnico.sec.hds.controllers.converters;

import io.swagger.client.model.*;

public class RequestConverter {

  public static AuditRequest auditRequestServerToClient(io.swagger.model.AuditRequest auditRequest){
    return new AuditRequest().publicKey(new PubKey().value(auditRequest.getPublicKey().getValue()));
  }

  public static CheckAccountRequest checkAccountServerToClient(io.swagger.model.CheckAccountRequest checkAccountRequest){
    return null;
  }

  public static ReceiveAmountRequest receiveAmountServerToClient(io.swagger.model.ReceiveAmountRequest receiveAmount){
    return null;
  }

  public static RegisterRequest registerRequestServerToClient(io.swagger.model.RegisterRequest registerRequest){
    return null;
  }

  public static SendAmountRequest sendAmountServerToClient(io.swagger.model.SendAmountRequest sendAmountRequest){
    return null;
  }

  public static WriteBackRequest writeBackRequestServerToClient(io.swagger.model.WriteBackRequest writeBackRequest){
    return null;
  }

  public static BroadcastRequest broadcastRequestServerToClient(io.swagger.model.BroadcastRequest broadcastRequest){
    return null;
  }


}
