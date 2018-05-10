package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.server.controllers.AuditController;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.AuditApi;
import io.swagger.model.AuditRequest;
import io.swagger.model.AuditResponse;
import io.swagger.model.Signature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class AuditControllerProxy implements AuditApi {

  private final AuditController auditController;

  ServerTypeWrapper serverTypeWrapper;

  public AuditControllerProxy(CryptoAgent cryptoAgent, QueryHelpers queryHelpers, ServerTypeWrapper serverTypeWrapper) {
    this.serverTypeWrapper = serverTypeWrapper;
    auditController = new AuditController(cryptoAgent, queryHelpers);
  }

  @Override
  public ResponseEntity<AuditResponse> audit(@RequestBody @Valid AuditRequest body) {

    ResponseEntity<AuditResponse> response;

    switch (serverTypeWrapper.getType()){
      case NORMAL:
        return auditController.audit(body);
      case BYZANTINE:
        return null;
      case BADSIGN:
        response = auditController.audit(body);
        response.getBody().setSignature(new Signature().value("Fake Signature"));
        return response;
      default:
        throw new RuntimeException("This should never happen");
    }
  }
}
