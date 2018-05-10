package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.controllers.converters.RequestConverter;
import com.tecnico.sec.hds.controllers.converters.ResponseConverter;
import io.swagger.api.AuditApi;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.model.AuditRequest;
import io.swagger.model.AuditResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class AuditControllerProxy implements AuditApi {

  DefaultApi server;

  String type;

  public AuditControllerProxy(DefaultApi server) {
    this.server = server;
    type = System.getProperty("type");
  }

  @Override
  public ResponseEntity<AuditResponse> audit(@RequestBody @Valid AuditRequest body) {

    io.swagger.client.model.AuditResponse auditResponse;
    try {
        auditResponse = server.audit(RequestConverter.auditRequestServerToClient(body));
      return new ResponseEntity<>(ResponseConverter.auditResponseClientToServer(auditResponse), HttpStatus.OK);
    } catch (ApiException e){
      e.printStackTrace();
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
