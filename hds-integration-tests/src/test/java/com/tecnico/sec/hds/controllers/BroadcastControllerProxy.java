package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.server.controllers.BroadcastController;
import com.tecnico.sec.hds.server.controllers.converters.Converters;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.BroadcastApi;
import io.swagger.client.model.TransactionInformation;
import io.swagger.model.BroadcastRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class BroadcastControllerProxy implements BroadcastApi {

  private final BroadcastController broadcastController;

  private final ServersWrapper serversWrapper;

  private final ReliableBroadcastHelper reliableBroadcastHelper;

  private ServerTypeWrapper serverTypeWrapper;

  public BroadcastControllerProxy(ServersWrapper serversWrapper, CryptoAgent cryptoAgent, QueryHelpers queryHelpers,
                                  ReliableBroadcastHelper reliableBroadcastHelper, ServerTypeWrapper serverTypeWrapper) {
    this.serversWrapper = serversWrapper;
    this.reliableBroadcastHelper = reliableBroadcastHelper;
    this.serverTypeWrapper = serverTypeWrapper;
    this.broadcastController = new BroadcastController(cryptoAgent, queryHelpers, serversWrapper, reliableBroadcastHelper);
  }

  @Override
  public ResponseEntity<Void> broadcast(@RequestBody @Valid BroadcastRequest body) {
    switch (serverTypeWrapper.getType()) {
      case NORMAL:
        return broadcastController.broadcast(body);
      case BYZANTINE:
        return null;
      case NOECHOES:
        return new ResponseEntity<>(HttpStatus.OK);
      case NOREADIES:
        return new ResponseEntity<>(HttpStatus.OK);
      case ECHOS10:
        return broadcastController.broadcast(body);
      case BADSIGN:
        return broadcastController.broadcast(body);
      default:
        return broadcastController.broadcast(body);
        //throw new RuntimeException("This should never happen");
    }


  }
}
