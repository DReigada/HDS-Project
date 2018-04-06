package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.controllers.util.TransactionFormatter;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.AuditRules;
import com.tecnico.sec.hds.server.domain.Transaction;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.annotations.ApiParam;
import io.swagger.api.AuditApi;
import io.swagger.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.List;



@Controller
public class AuditController implements AuditApi{
  private AuditRules auditRules;
  private CryptoAgent cryptoAgent;
  private TransactionFormatter transactionFormatter;

  public AuditController() throws InvalidParameterSpecException, InvalidAlgorithmParameterException,
    NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
    cryptoAgent = new CryptoAgent("bank", "strongPassword");
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
