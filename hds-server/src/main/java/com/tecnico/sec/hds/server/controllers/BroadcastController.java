package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.db.rules.BroadcastRules;
import com.tecnico.sec.hds.server.db.rules.ReceiveAmountRules;
import com.tecnico.sec.hds.server.db.rules.SendAmountRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.BroadcastApi;
import io.swagger.model.BroadcastRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;


@Controller
public class BroadcastController implements BroadcastApi {
  private final BroadcastRules broadcastRules;

  public BroadcastController(CryptoAgent cryptoAgent, QueryHelpers queryHelpers,
                             ServersWrapper serversWrapper, ReliableBroadcastHelper reliableBroadcastHelper) {
    ReceiveAmountRules receiveAmountRules = new ReceiveAmountRules(queryHelpers);
    SendAmountRules sendAmountRules = new SendAmountRules(queryHelpers);
    broadcastRules = new BroadcastRules(cryptoAgent, reliableBroadcastHelper, serversWrapper, receiveAmountRules, sendAmountRules);
  }

  @Override
  public ResponseEntity<Void> broadcast(@Valid @RequestBody BroadcastRequest body) {
    if (body.isIsEcho()) {
      broadcastRules.echoRule(body);
    } else if (body.isIsReady()) {
      broadcastRules.readyRule(body);
    } else {
      System.err.println("This should never happen™");
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
