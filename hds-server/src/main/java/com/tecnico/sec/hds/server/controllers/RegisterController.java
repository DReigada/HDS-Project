package com.tecnico.sec.hds.server.controllers;


import io.swagger.api.RegisterApi;
import io.swagger.api.RegisterApiController;
import io.swagger.model.RegisterRequest;
import io.swagger.model.RegisterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class RegisterController implements RegisterApi {

  private static final Logger log = LoggerFactory.getLogger(RegisterApiController.class);

  @Override
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest body) {
    String key = body.getPublicKey().getValue();
    log.info("Got new public key: " + key);



    RegisterResponse response =
        new RegisterResponse().addBlaItem("Hello").addBlaItem("user:").addBlaItem(key);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
