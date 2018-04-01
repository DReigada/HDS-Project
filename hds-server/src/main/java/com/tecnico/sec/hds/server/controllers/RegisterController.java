package com.tecnico.sec.hds.server.controllers;


import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.RegisterRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.RegisterApi;
import io.swagger.api.RegisterApiController;
import io.swagger.model.RegisterRequest;
import io.swagger.model.RegisterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Controller
public class RegisterController implements RegisterApi {

  private static final Logger log = LoggerFactory.getLogger(RegisterApiController.class);
  private CryptoAgent cryptoAgent;
  private RegisterRules registerRules;

  public RegisterController() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    cryptoAgent = new CryptoAgent("bank");
    registerRules = new RegisterRules();
  }

  @Override
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest body) {
    String key = body.getPublicKey().getValue();
    String signature = body.getSignature().getValue();
    log.info("Public key: " + key);
    log.info("1n Signature: " + signature);

    RegisterResponse response =  new RegisterResponse();

    try {
      cryptoAgent.verifySignature(key,signature);
      registerRules.register(key);
      response.addBlaItem("Registration Completed:").addBlaItem(key);
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      response.addBlaItem("Registration Fail: Invalid Keys");
    } catch (DBException e) {
      response.addBlaItem("Registration Fail: Invalid Database, Try Later");
    }

    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
