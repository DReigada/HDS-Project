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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;

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
  public ResponseEntity<SendAmountResponse> sendAmount(@Valid SendAmountRequest body) {
    return sendAmountController.sendAmount(body);
  }
}
