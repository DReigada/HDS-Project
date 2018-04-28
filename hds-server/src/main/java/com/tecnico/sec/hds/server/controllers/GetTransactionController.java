package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.controllers.util.TransactionFormatter;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.GetTransactionRules;
import com.tecnico.sec.hds.server.domain.Transaction;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.annotations.ApiParam;
import io.swagger.api.GetTransactionApi;
import io.swagger.model.GetTransactionRequest;
import io.swagger.model.GetTransactionResponse;
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
import java.util.Optional;

@Controller
public class GetTransactionController implements GetTransactionApi {

  private GetTransactionRules getTransactionRules;
  private CryptoAgent cryptoAgent;
  private TransactionFormatter transactionFormatter;

  public GetTransactionController() throws
      NoSuchAlgorithmException, IOException, UnrecoverableKeyException, CertificateException, OperatorCreationException, KeyStoreException {
    getTransactionRules = new GetTransactionRules();
    cryptoAgent = new CryptoAgent("bank", "pass");
    transactionFormatter = new TransactionFormatter();
  }

  @Override
  public ResponseEntity<GetTransactionResponse> getTransaction(@ApiParam(required = true) @RequestBody @Valid GetTransactionRequest body) {
    String hash = body.getHash().getValue();

    GetTransactionResponse getTransactionResponse = new GetTransactionResponse();

    try {
      Optional<Transaction> transaction = getTransactionRules.getTransaction(hash);

      if (transaction.isPresent()) {
        getTransactionResponse.setTransaction(transactionFormatter.getTransactionInformation(transaction.get()));
        Signature signature = new Signature();
        signature.setValue(cryptoAgent.generateSignature(transactionFormatter.getTransactionListMessage(transaction.get())));
        getTransactionResponse.setSignature(signature);
      }

    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | DBException e) {
      e.printStackTrace();
    }
    return new ResponseEntity<>(getTransactionResponse, HttpStatus.OK);
  }
}