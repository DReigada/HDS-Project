package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.CheckAccountRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.annotations.ApiParam;
import io.swagger.api.CheckAccountApi;
import io.swagger.model.CheckAccountRequest;
import io.swagger.model.CheckAccountResponse;
import io.swagger.model.Signature;
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
        String response = "";
        try {
            long amount = checkAccountRules.getBalance(publicKey);
            response = "Public Key: " + publicKey + "\n" + "Balance: " + amount;
            Signature signature = new Signature().value(cryptoAgent.generateSignature(response));
            checkAccountResponse.setSignature(signature);
        } catch (DBException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        checkAccountResponse.setMessage(response);

        return new ResponseEntity<>(checkAccountResponse , HttpStatus.OK);
    }
}
