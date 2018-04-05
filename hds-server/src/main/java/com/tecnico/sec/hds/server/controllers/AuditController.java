package com.tecnico.sec.hds.server.controllers;

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

import javax.validation.Valid;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

/**
 * Created by jp_s on 3/12/2018.
 */
@Controller
public class AuditController implements AuditApi{
  private AuditRules auditRules;
  private CryptoAgent cryptoAgent;


  public AuditController() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    cryptoAgent = new CryptoAgent("bank");
    auditRules = new AuditRules();
  }

  @Override
  public ResponseEntity<AuditResponse> audit(@ApiParam(required = true) @RequestBody @Valid AuditRequest body) {
    String pubKey = body.getPublicKey().getValue();
    AuditResponse auditResponse = new AuditResponse();
    try {
      List<Transaction> history = auditRules.audit(pubKey);
      Signature sign = new Signature();
      StringBuilder transactionListMessage = new StringBuilder();
      for (Transaction transaction : history){
        auditResponse.addListItem(getTransactionInformation(transaction));
        transactionListMessage.append(getTransactionListMessage(transaction));
      }

      sign.value(cryptoAgent.generateSignature(transactionListMessage.toString()));
      auditResponse.setSignature(sign);

    } catch (DBException | NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
      e.printStackTrace();
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }

  private TransactionInformation getTransactionInformation(Transaction transaction){
    Hash hash = new Hash().value(transaction.hash);
    Signature signature = new Signature().value(transaction.signature);
    TransactionInformation transactionInformation = new TransactionInformation();
    transactionInformation.setTransID(transaction.transID);
    transactionInformation.setSourceKey(transaction.sourceKey);
    transactionInformation.setDestKey(transaction.destKey);
    transactionInformation.setAmount("" + transaction.amount);
    transactionInformation.setPending(transaction.pending);
    transactionInformation.setReceive(transaction.receive);
    transactionInformation.setSignature(signature);
    transactionInformation.setHash(hash);
    return transactionInformation;
  }

  private String getTransactionListMessage(Transaction transaction){
    String transactionListMessage = "";
    transactionListMessage += transaction.transID + "\n";
    transactionListMessage += transaction.sourceKey + "\n";
    transactionListMessage += transaction.destKey + "\n";
    transactionListMessage += transaction.amount + "\n";
    transactionListMessage += transaction.pending + "\n";
    transactionListMessage += transaction.receive + "\n";
    transactionListMessage += transaction.signature + "\n";
    transactionListMessage += transaction.hash + "\n";
    return transactionListMessage;
  }
}
