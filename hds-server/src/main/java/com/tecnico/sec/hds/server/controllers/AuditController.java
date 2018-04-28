package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.controllers.util.TransactionFormatter;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.AuditRules;
import com.tecnico.sec.hds.server.domain.Transaction;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.annotations.ApiParam;
import io.swagger.api.AuditApi;
import io.swagger.model.AuditRequest;
import io.swagger.model.AuditResponse;
import io.swagger.model.Signature;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;



@Controller
public class AuditController implements AuditApi{
  private AuditRules auditRules;
  private CryptoAgent cryptoAgent;
  private TransactionFormatter transactionFormatter;

  public AuditController() throws NoSuchAlgorithmException, IOException, UnrecoverableKeyException, CertificateException, OperatorCreationException, KeyStoreException {
    cryptoAgent = new CryptoAgent("bank", "bank");
    auditRules = new AuditRules();
    transactionFormatter = new TransactionFormatter();
  }

  @Override
  public ResponseEntity<AuditResponse> audit(@ApiParam(required = true) @RequestBody @Valid AuditRequest body) {
    String pubKey = body.getPublicKey().getValue();
    AuditResponse auditResponse = new AuditResponse();
    try {
      List<Transaction> history = auditRules.audit(pubKey);
      StringBuilder transactionListMessage = new StringBuilder();
      for (Transaction transaction : history){
        auditResponse.addListItem(transactionFormatter.getTransactionInformation(transaction));
        transactionListMessage.append(transactionFormatter.getTransactionListMessage(transaction) + "\n");
      }

      Signature sign = new Signature().value(cryptoAgent.generateSignature(transactionListMessage.toString()));
      auditResponse.setSignature(sign);

    } catch (DBException | NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
      e.printStackTrace();
    }

    return new ResponseEntity<>(auditResponse, HttpStatus.OK);
  }
}
