package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.controllers.util.TransactionFormatter;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.db.rules.AuditRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import domain.Transaction;
import io.swagger.annotations.ApiParam;
import io.swagger.api.AuditApi;
import io.swagger.model.AuditRequest;
import io.swagger.model.AuditResponse;
import io.swagger.model.Signature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@Controller
public class AuditController implements AuditApi {
  private final CryptoAgent cryptoAgent;
  private AuditRules auditRules;

  public AuditController(CryptoAgent cryptoAgent, QueryHelpers queryHelpers) {
    auditRules = new AuditRules(queryHelpers);
    this.cryptoAgent = cryptoAgent;
  }

  @Override
  public ResponseEntity<AuditResponse> audit(@ApiParam(required = true) @RequestBody @Valid AuditRequest body) {
    String pubKey = body.getPublicKey().getValue();
    AuditResponse auditResponse = new AuditResponse();
    try {
      auditResponse.setList(new ArrayList<>());
      List<Transaction> history = auditRules.audit(pubKey);
      for (Transaction transaction : history) {
        auditResponse.addListItem(TransactionFormatter.getTransactionInformation(transaction));
      }
      Signature sign;
      if (auditResponse.getList() != null) {
        sign = new Signature().value(cryptoAgent.generateSignature(
            TransactionFormatter.convertTransactionsToString(auditResponse.getList())));
      } else {
        sign = new Signature().value(cryptoAgent.generateSignature(""));
      }
      auditResponse.setSignature(sign);
    } catch (DBException e) {
      e.printStackTrace();
    }

    return new ResponseEntity<>(auditResponse, HttpStatus.OK);
  }
}
