package com.tecnico.sec.hds.server.controllers;

import io.swagger.annotations.ApiParam;
import io.swagger.api.ReceiveAmountApi;
import io.swagger.model.ReceiveAmountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * Created by jp_s on 3/12/2018.
 */

@Controller
public class ReceiveAmountController implements ReceiveAmountApi {

  @Override
  public ResponseEntity<Void> receiveAmount(@ApiParam(value = "", required = true) @RequestBody @Valid ReceiveAmountRequest body) {
    return  new ResponseEntity<>(HttpStatus.OK);
  }
}
