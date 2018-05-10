package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.controllers.converters.RequestConverter;
import com.tecnico.sec.hds.controllers.converters.ResponseConverter;
import io.swagger.api.RegisterApi;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.model.RegisterRequest;
import io.swagger.model.RegisterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;

@Controller
public class RegisterControllerProxy implements RegisterApi {

  DefaultApi server;

  String type;

  public RegisterControllerProxy(DefaultApi server) {
    this.server = server;
    type = System.getProperty("type");
  }

  @Override
  public ResponseEntity<RegisterResponse> register(@Valid RegisterRequest body) {
    io.swagger.client.model.RegisterResponse registerResponse;
    try {
      registerResponse = server.register(RequestConverter.registerRequestServerToClient(body));

      return new ResponseEntity<>(ResponseConverter.registerResponseClientToServer(registerResponse), HttpStatus.OK);
    } catch (ApiException e) {
      e.printStackTrace();
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
