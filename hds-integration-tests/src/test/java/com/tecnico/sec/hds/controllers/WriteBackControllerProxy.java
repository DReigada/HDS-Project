package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.server.controllers.WriteBackController;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.WriteBackApi;
import io.swagger.client.api.DefaultApi;
import io.swagger.model.WriteBackRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;


@Controller
public class WriteBackControllerProxy implements WriteBackApi {

  private WriteBackController writeBackController;

  private ServerTypeWrapper serverTypeWrapper;

  public WriteBackControllerProxy(ReliableBroadcastHelper reliableBroadcastHelper, QueryHelpers queryHelpers,
                                  ServersWrapper serversWrapper, ServerTypeWrapper serverTypeWrapper) {
    this.writeBackController = new WriteBackController(reliableBroadcastHelper, serversWrapper, queryHelpers);
    this.serverTypeWrapper = serverTypeWrapper;
  }

  @Override
  public ResponseEntity<Void> writeBack(@Valid WriteBackRequest body) {
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
