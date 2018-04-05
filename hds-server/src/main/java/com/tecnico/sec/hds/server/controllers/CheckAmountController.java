package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.CheckAmountRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.annotations.ApiParam;
import io.swagger.api.CheckAmountApi;
import io.swagger.model.CheckAmountRequest;
import io.swagger.model.CheckAmountResponse;
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
            long amount = checkAmountRules.getBalance(publicKey);
            response = "Public Key: " + publicKey + "\n" + "Balance: " + amount;
            Signature signature = new Signature().value(cryptoAgent.generateSignature(response));
            checkAmountResponse.setSignature(signature);
        } catch (DBException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        checkAmountResponse.setMessage(response);

        return new ResponseEntity<>(checkAmountResponse , HttpStatus.OK);
    }
}
