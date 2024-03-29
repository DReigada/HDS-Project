package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.controllers.util.TransactionFormatter;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.db.rules.GetTransactionRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import domain.Transaction;
import io.swagger.annotations.ApiParam;
import io.swagger.api.GetTransactionApi;
import io.swagger.model.GetTransactionRequest;
import io.swagger.model.GetTransactionResponse;
import io.swagger.model.Signature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Optional;

@Controller
public class GetTransactionController implements GetTransactionApi {
  private final CryptoAgent cryptoAgent;
  private GetTransactionRules getTransactionRules;


  public GetTransactionController(CryptoAgent cryptoAgent, QueryHelpers queryHelpers) {
    this.cryptoAgent = cryptoAgent;
    getTransactionRules = new GetTransactionRules(queryHelpers);
  }

  @Override
  public ResponseEntity<GetTransactionResponse> getTransaction(@ApiParam(required = true) @RequestBody @Valid GetTransactionRequest body) {
    String hash = body.getHash().getValue();

    GetTransactionResponse getTransactionResponse = new GetTransactionResponse();

    try {
      Optional<Transaction> transaction = getTransactionRules.getTransaction(hash);

      if (transaction.isPresent()) {
        getTransactionResponse.setTransaction(TransactionFormatter.getTransactionInformation(transaction.get()));
        Signature signature = new Signature();
        signature.setValue(cryptoAgent.generateSignature(TransactionFormatter.getTransactionInformation(transaction.get()).toString()));
        getTransactionResponse.setSignature(signature);
      }

    } catch (DBException e) {
      e.printStackTrace();
    }
    return new ResponseEntity<>(getTransactionResponse, HttpStatus.OK);
  }
}
