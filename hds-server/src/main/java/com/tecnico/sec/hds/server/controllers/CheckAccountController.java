package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.controllers.util.TransactionFormatter;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.CheckAccountRules;
import com.tecnico.sec.hds.server.domain.Transaction;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.annotations.ApiParam;
import io.swagger.api.CheckAccountApi;
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


@Controller
public class CheckAccountController implements CheckAccountApi {

    private CryptoAgent cryptoAgent;
    private CheckAccountRules checkAccountRules;
    private TransactionFormatter transactionFormatter;

    public CheckAccountController() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        cryptoAgent = new CryptoAgent("bank");
        checkAccountRules = new CheckAccountRules();
        transactionFormatter = new TransactionFormatter();
    }

    @Override
    public ResponseEntity<CheckAccountResponse> checkAccount(@ApiParam(required = true) @RequestBody @Valid CheckAccountRequest body) {
        String publicKey = body.getPublicKey().getValue();
        CheckAccountResponse checkAccountResponse = new CheckAccountResponse();
        StringBuilder response = new StringBuilder();
        try {
            long amount = checkAccountRules.getBalance(publicKey);
            checkAccountResponse.setAmount("" + amount);
            response = new StringBuilder("Public Key: " + publicKey + "\n" + "Balance: " + amount + "\n");
            List<Transaction> transactionList = checkAccountRules.getPendingTransactions(publicKey);
            for(Transaction transaction : transactionList){
                checkAccountResponse.addListItem(transactionFormatter.getTransactionInformation(transaction));
                response.append(transactionFormatter.getTransactionListMessage(transaction) + "\n");
            }

            Signature signature = new Signature().value(cryptoAgent.generateSignature(response.toString()));
            checkAccountResponse.setSignature(signature);
        } catch (DBException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }


        return new ResponseEntity<>(checkAccountResponse , HttpStatus.OK);
    }
}