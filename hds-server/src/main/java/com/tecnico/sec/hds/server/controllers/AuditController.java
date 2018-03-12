package com.tecnico.sec.hds.server.controllers;

import io.swagger.annotations.ApiParam;
import io.swagger.api.AuditApi;
import io.swagger.model.AuditRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * Created by jp_s on 3/12/2018.
 */
@Controller
public class AuditController implements AuditApi{
  @Override
  public ResponseEntity<Void> audit(@ApiParam(value = "", required = true) @RequestBody @Valid AuditRequest body) {
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
