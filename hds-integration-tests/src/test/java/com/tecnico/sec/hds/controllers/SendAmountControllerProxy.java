package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.controllers.converters.RequestConverter;
import com.tecnico.sec.hds.controllers.converters.ResponseConverter;
import io.swagger.api.SendAmountApi;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.model.SendAmountRequest;
import io.swagger.model.SendAmountResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;

@Controller
public class SendAmountControllerProxy implements SendAmountApi {

  DefaultApi server;

  String type;

  public SendAmountControllerProxy(DefaultApi server) {
    this.server = server;
    type = System.getProperty("type");
  }

  @Override
  public ResponseEntity<SendAmountResponse> sendAmount(@Valid SendAmountRequest body) {
    io.swagger.client.model.SendAmountResponse sendAmountResponse;
    try {
      sendAmountResponse = server.sendAmount(RequestConverter.sendAmountServerToClient(body));

      return new ResponseEntity<>(ResponseConverter.sendAmountResponseClientToServer(sendAmountResponse), HttpStatus.OK);
    } catch (ApiException e) {
      e.printStackTrace();
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
