package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.controllers.util.TransactionFormatter;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.CheckAccountRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import domain.Transaction;
import io.swagger.annotations.ApiParam;
import io.swagger.api.CheckAccountApi;
import io.swagger.model.CheckAccountRequest;
import io.swagger.model.CheckAccountResponse;
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
import java.util.ArrayList;
import java.util.List;


@Controller
public class CheckAccountController implements CheckAccountApi {

  private CryptoAgent cryptoAgent;
  private CheckAccountRules checkAccountRules;

  public CheckAccountController() throws NoSuchAlgorithmException, IOException, UnrecoverableKeyException, CertificateException, OperatorCreationException, KeyStoreException {
    cryptoAgent = new CryptoAgent("bank", "bank");
    checkAccountRules = new CheckAccountRules();
  }

  @Override
  public ResponseEntity<CheckAccountResponse> checkAccount(@ApiParam(required = true) @RequestBody @Valid CheckAccountRequest body) {
    String publicKey = body.getPublicKey().getValue();
    CheckAccountResponse checkAccountResponse = new CheckAccountResponse();
    StringBuilder response;
    try {
      long amount = checkAccountRules.getBalance(publicKey);
      checkAccountResponse.setAmount("" + amount);
      response = new StringBuilder("Public Key: " + publicKey + "\n" + "Balance: " + amount + "\n");
      List<Transaction> transactionList = checkAccountRules.getPendingTransactions(publicKey);

      checkAccountResponse.setList(new ArrayList<>());
      for (Transaction transaction : transactionList) {
        checkAccountResponse.addListItem(TransactionFormatter.getTransactionInformation(transaction));
      }
      if (checkAccountResponse.getList().size() > 0) {
        response.append(TransactionFormatter.convertTransactionsToString(checkAccountResponse.getList())); //MIGHT FAIL HERE
      }

      Signature signature = new Signature().value(cryptoAgent.generateSignature(response.toString()));
      checkAccountResponse.setSignature(signature);
    } catch (DBException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      e.printStackTrace();
    }

    return new ResponseEntity<>(checkAccountResponse, HttpStatus.OK);
  }
}