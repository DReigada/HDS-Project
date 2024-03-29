package com.tecnico.sec.hds.server.controllers;


import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.db.rules.RegisterRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.RegisterApi;
import io.swagger.model.Hash;
import io.swagger.model.RegisterRequest;
import io.swagger.model.RegisterResponse;
import io.swagger.model.Signature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class RegisterController implements RegisterApi {
  private final CryptoAgent cryptoAgent;

  private RegisterRules registerRules;

  public RegisterController(CryptoAgent cryptoAgent, QueryHelpers queryHelpers) {
    this.cryptoAgent = cryptoAgent;
    registerRules = new RegisterRules(queryHelpers);
  }

  @Override
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest body) {
    String key = body.getPublicKey().getValue();
    String signature = body.getSignature().getValue();
    RegisterResponse response = new RegisterResponse();
    String message;
    System.err.println("SERVER: " + cryptoAgent.username);

    try {
      if (cryptoAgent.verifySignature(key, signature, key)) {
        Hash hash = new Hash();
        hash.setValue(registerRules.register(key));
        response.setHash(hash);
        message = "Registration Completed:" + key;
      } else {
        message = "Registration Fail: Try Later";
      }

      signature = cryptoAgent.generateSignature(message + response.getHash().getValue());
      Signature signed = new Signature().value(signature);
      response.setSignature(signed);
      response.setMessage(message);
    } catch (DBException e1) {
      System.err.println("ERROR: " + cryptoAgent.username);
      e1.printStackTrace();
    }

    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
