package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.controllers.converters.RequestConverter;
import com.tecnico.sec.hds.controllers.converters.ResponseConverter;
import io.swagger.api.ReceiveAmountApi;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.model.ReceiveAmountRequest;
import io.swagger.model.ReceiveAmountResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class ReceiveAmountControllerProxy implements ReceiveAmountApi {

  DefaultApi server;

  String type;

  public ReceiveAmountControllerProxy(DefaultApi server) {
    this.server = server;
    type = System.getProperty("type");
  }

  @Override
  public ResponseEntity<ReceiveAmountResponse> receiveAmount(@RequestBody @Valid ReceiveAmountRequest body) {
    io.swagger.client.model.ReceiveAmountResponse receiveAmountResponse;
    try {
      receiveAmountResponse = server.receiveAmount(RequestConverter.receiveAmountServerToClient(body));

      return new ResponseEntity<>(ResponseConverter.receiveAmountResponseClientToServer(receiveAmountResponse), HttpStatus.OK);
    } catch (ApiException e) {
      e.printStackTrace();
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
