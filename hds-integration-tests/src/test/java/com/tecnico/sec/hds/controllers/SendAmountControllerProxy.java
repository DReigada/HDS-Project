package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.server.controllers.SendAmountController;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.SendAmountApi;
import io.swagger.model.SendAmountRequest;
import io.swagger.model.SendAmountResponse;
import io.swagger.model.Signature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Base64;

@Controller
public class SendAmountControllerProxy implements SendAmountApi {

  private SendAmountController sendAmountController;

  private ServerTypeWrapper serverTypeWrapper;

  public SendAmountControllerProxy(CryptoAgent cryptoAgent, QueryHelpers queryHelpers, ServersWrapper serversWrapper,
                                   ReliableBroadcastHelper reliableBroadcastHelper, ServerTypeWrapper serverTypeWrapper) {
    this.sendAmountController = new SendAmountController(cryptoAgent, queryHelpers, serversWrapper, reliableBroadcastHelper);
    this.serverTypeWrapper = serverTypeWrapper;
  }

  @Override
  public ResponseEntity<SendAmountResponse> sendAmount(@RequestBody @Valid SendAmountRequest body) {

    ResponseEntity<SendAmountResponse> response;

    switch (serverTypeWrapper.getType()){
      case NORMAL:
      case BADORDER:
        return sendAmountController.sendAmount(body);
      case BYZANTINE:
        return null;
      case BADSIGN:
        response = sendAmountController.sendAmount(body);
        response.getBody().setSignature(new Signature().value(Base64.getEncoder().encodeToString("FakeSignature".getBytes())));
        return response;
      case ECHOS10:
        return new ResponseEntity<>(HttpStatus.OK);
       default:
         throw new RuntimeException("This should never happen");
    }
  }
}
