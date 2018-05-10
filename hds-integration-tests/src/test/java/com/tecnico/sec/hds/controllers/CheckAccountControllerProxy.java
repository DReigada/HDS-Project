package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.controllers.converters.RequestConverter;
import com.tecnico.sec.hds.controllers.converters.ResponseConverter;
import io.swagger.api.CheckAccountApi;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.model.CheckAccountRequest;
import io.swagger.model.CheckAccountResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class CheckAccountControllerProxy implements CheckAccountApi {

  DefaultApi server;

  String type;

  public CheckAccountControllerProxy(DefaultApi server) {
    this.server = server;
    type = System.getProperty("type");
  }

  @Override
  public ResponseEntity<CheckAccountResponse> checkAccount(@RequestBody @Valid CheckAccountRequest body) {
    io.swagger.client.model.CheckAccountResponse checkAccountResponse;
    try {
      checkAccountResponse = server.checkAccount(RequestConverter.checkAccountServerToClient(body));

      return new ResponseEntity<>(ResponseConverter.checkAccountResponseClientToServer(checkAccountResponse), HttpStatus.OK);
    } catch (ApiException e){
      e.printStackTrace();
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
