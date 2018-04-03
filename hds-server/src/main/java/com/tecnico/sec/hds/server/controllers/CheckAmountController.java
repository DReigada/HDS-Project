package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.CheckAmountRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.annotations.ApiParam;
import io.swagger.api.CheckAmountApi;
import io.swagger.model.CheckAmountRequest;
import io.swagger.model.CheckAmountResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by jp_s on 3/6/2018.
 */
public class CheckAmountController implements CheckAmountApi {

    private CryptoAgent cryptoAgent;
    private CheckAmountRules checkAmountRules;

    public CheckAmountController() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        cryptoAgent = new CryptoAgent("bank");
        checkAmountRules = new CheckAmountRules();
    }

    @Override
    public ResponseEntity<CheckAmountResponse> checkAmount(@ApiParam(required = true) @RequestBody @Valid CheckAmountRequest body) {
        String publicKey = body.getPublicKey().getValue();

        CheckAmountResponse checkAmountResponse = new CheckAmountResponse();
        String response = "";
        try {
            float amount = checkAmountRules.getBalance(publicKey);
            response = "Public Key: " + publicKey + "\n" + "Balance: " + amount;
        } catch (DBException e) {
            response = "Invalid Public Key!";
        }

        //cryptoAgent.generateSignature(response);
        checkAmountResponse.addMessageItem(response);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
