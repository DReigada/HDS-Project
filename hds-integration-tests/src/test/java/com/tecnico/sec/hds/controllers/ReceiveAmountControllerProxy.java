package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.server.controllers.ReceiveAmountController;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.ReceiveAmountApi;
import io.swagger.model.ReceiveAmountRequest;
import io.swagger.model.ReceiveAmountResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class ReceiveAmountControllerProxy implements ReceiveAmountApi {

  private ReceiveAmountController receiveAmountController;

  private ServerTypeWrapper serverTypeWrapper;

  public ReceiveAmountControllerProxy(CryptoAgent cryptoAgent, QueryHelpers queryHelpers, ServersWrapper serversWrapper,
                                      ReliableBroadcastHelper reliableBroadcastHelper, ServerTypeWrapper serverTypeWrapper) {
    this.receiveAmountController = new ReceiveAmountController(cryptoAgent, queryHelpers, serversWrapper, reliableBroadcastHelper);
    this.serverTypeWrapper = serverTypeWrapper;
  }

  @Override
  public ResponseEntity<ReceiveAmountResponse> receiveAmount(@RequestBody @Valid ReceiveAmountRequest body) {
   return receiveAmountController.receiveAmount(body);
  }
}
