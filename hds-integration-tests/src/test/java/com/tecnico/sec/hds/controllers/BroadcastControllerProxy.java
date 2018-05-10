package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.controllers.converters.RequestConverter;
import com.tecnico.sec.hds.controllers.converters.ResponseConverter;
import io.swagger.api.ApiException;
import io.swagger.api.BroadcastApi;
import io.swagger.client.api.DefaultApi;
import io.swagger.model.BroadcastRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class BroadcastControllerProxy implements BroadcastApi {

  DefaultApi server;

  String type;

  public BroadcastControllerProxy(DefaultApi server) {
    this.server = server;
    type = System.getProperty("type");
  }

  @Override
  public ResponseEntity<Void> broadcast(@RequestBody @Valid BroadcastRequest body) {
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
