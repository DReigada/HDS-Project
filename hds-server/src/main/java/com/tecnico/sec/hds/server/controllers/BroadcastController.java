package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.util.SecurityHelper;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.annotations.ApiParam;
import io.swagger.api.BroadcastApi;
import io.swagger.model.BroadcastRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class BroadcastController implements BroadcastApi {

  private final ServersWrapper serversWrapper;



  public BroadcastController(ServersWrapper serversWrapper){
    this.serversWrapper = serversWrapper;
  }

  @Override
  public ResponseEntity<Void> broadcast(@ApiParam(required = true) @RequestBody @Valid BroadcastRequest body) {
    return null;
  }
}
