package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.controllers.util.TransactionFormatter;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.GetTransactionRules;
import com.tecnico.sec.hds.server.domain.Transaction;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.GetTransactionApi;
import io.swagger.model.GetTransactionRequest;
import io.swagger.model.GetTransactionResponse;
import io.swagger.model.Hash;
import io.swagger.model.Signature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Optional;

@Controller
public class GetTransactionController implements GetTransactionApi {

  private GetTransactionRules getTransactionRules;
  private CryptoAgent cryptoAgent;
  private TransactionFormatter transactionFormatter;

  public GetTransactionController() throws InvalidParameterSpecException, InvalidAlgorithmParameterException,
    NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
    getTransactionRules = new GetTransactionRules();
    cryptoAgent = new CryptoAgent("bank", "strongPassword");
    transactionFormatter = new TransactionFormatter();
  }

  @Override
  public ResponseEntity<GetTransactionResponse> getTransaction(@Valid GetTransactionRequest body) {
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
