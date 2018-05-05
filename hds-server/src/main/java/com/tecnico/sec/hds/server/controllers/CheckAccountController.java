package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.app.Application;
import com.tecnico.sec.hds.server.controllers.util.TransactionFormatter;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.AuditRules;
import com.tecnico.sec.hds.server.db.rules.CheckAccountRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import domain.Transaction;
import io.swagger.annotations.ApiParam;
import io.swagger.api.CheckAccountApi;
import io.swagger.model.CheckAccountRequest;
import io.swagger.model.CheckAccountResponse;
import io.swagger.model.Signature;
import io.swagger.model.TransactionInformation;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;


@Controller
public class CheckAccountController implements CheckAccountApi {

  private CryptoAgent cryptoAgent = Application.cryptoAgent;
  private AuditRules auditRules;
  private CheckAccountRules checkAccountRules;

  public CheckAccountController() throws NoSuchAlgorithmException, IOException, UnrecoverableKeyException, CertificateException, OperatorCreationException, KeyStoreException {
    checkAccountRules = new CheckAccountRules();
    auditRules =  new AuditRules();
  }

  @Override
  public ResponseEntity<CheckAccountResponse> checkAccount(@ApiParam(required = true) @RequestBody @Valid CheckAccountRequest body) {
    String publicKey = body.getPublicKey().getValue();
    CheckAccountResponse checkAccountResponse = new CheckAccountResponse();
    StringBuilder stringToSign;
    try {
      stringToSign = new StringBuilder();

      List<Transaction> history = auditRules.audit(publicKey);

      checkAccountResponse.setHistory(new ArrayList<>());

      for (Transaction transaction : history) {
        checkAccountResponse.addHistoryItem(TransactionFormatter.getTransactionInformation(transaction));
      }

      if (history.size() > 0) {
        stringToSign.append(TransactionFormatter.convertTransactionsToString(checkAccountResponse.getHistory()));
      }

      List<Transaction> transactionList = checkAccountRules.getPendingTransactions(publicKey);

      checkAccountResponse.setPending(new ArrayList<>());
      for (Transaction transaction : transactionList) {
        checkAccountResponse.addPendingItem(TransactionFormatter.getTransactionInformation(transaction));

      }
      if (checkAccountResponse.getPending().size() > 0) {
        stringToSign.append(TransactionFormatter.convertTransactionsToString(checkAccountResponse.getPending()));
      }

      Signature signature = new Signature().value(cryptoAgent.generateSignature(stringToSign.toString()));
      checkAccountResponse.setSignature(signature);
    } catch (DBException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      e.printStackTrace();
    }

    return new ResponseEntity<>(checkAccountResponse, HttpStatus.OK);
  }
}