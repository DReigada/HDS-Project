package com.tecnico.sec.hds.server.controllers;

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

/**
 * Created by jp_s on 3/6/2018.
 */
@Controller
public class CheckAccountController implements CheckAccountApi {

    private CryptoAgent cryptoAgent;
    private CheckAccountRules checkAccountRules;

    public CheckAccountController() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        cryptoAgent = new CryptoAgent("bank");
        checkAccountRules = new CheckAccountRules();
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
                checkAccountResponse.addListItem(getTransactionInformation(transaction));
                response.append(getTransactionListMessage(transaction) + "\n");
            }

            Signature signature = new Signature().value(cryptoAgent.generateSignature(response.toString()));
            checkAccountResponse.setSignature(signature);
        } catch (DBException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        checkAccountResponse.setMessage(response.toString());

        return new ResponseEntity<>(checkAccountResponse , HttpStatus.OK);
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
        transactionListMessage += "Transaction ID: " + transaction.transID + "\n";
        transactionListMessage += "Source Key: " + transaction.sourceKey + "\n";
        transactionListMessage += "Destination Key: " + transaction.destKey + "\n";
        transactionListMessage +=  "Amount: " + transaction.amount + "\n";
        transactionListMessage += "Pending: " + transaction.pending + "\n";
        transactionListMessage += "Received: " + transaction.receive + "\n";
        transactionListMessage += "Signature: " + transaction.signature + "\n";
        transactionListMessage += "Hash: " + transaction.hash + "\n";
        return transactionListMessage;
    }
}
