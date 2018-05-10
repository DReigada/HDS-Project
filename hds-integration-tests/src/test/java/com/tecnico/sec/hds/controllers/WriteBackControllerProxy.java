package com.tecnico.sec.hds.controllers;

import io.swagger.api.WriteBackApi;
import io.swagger.client.api.DefaultApi;
import io.swagger.model.WriteBackRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;


@Controller
public class WriteBackControllerProxy implements WriteBackApi {

  DefaultApi server;

  String type;

  public WriteBackControllerProxy(DefaultApi server) {
    this.server = server;
    type = System.getProperty("type");
  }

  @Override
  public ResponseEntity<Void> writeBack(@Valid WriteBackRequest body) {
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
