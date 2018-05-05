package com.tecnico.sec.hds.server.controllers;


import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.rules.RegisterRules;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.RegisterApi;
import io.swagger.api.RegisterApiController;
import io.swagger.model.Hash;
import io.swagger.model.RegisterRequest;
import io.swagger.model.RegisterResponse;
import io.swagger.model.Signature;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

@Controller
public class RegisterController implements RegisterApi {

  private static final Logger log = LoggerFactory.getLogger(RegisterApiController.class);
  private CryptoAgent cryptoAgent;
  private RegisterRules registerRules;

  public RegisterController() throws NoSuchAlgorithmException, IOException, UnrecoverableKeyException, CertificateException, OperatorCreationException, KeyStoreException {
    String port = System.getProperty("server.port");
    cryptoAgent = new CryptoAgent("bank" + port, "bank"+ port);
    registerRules = new RegisterRules();
  }

  @Override
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest body){
    String key = body.getPublicKey().getValue();
    String signature = body.getSignature().getValue();
    RegisterResponse response =  new RegisterResponse();

    String message;


    try {
      if(cryptoAgent.verifySignature(key,signature,key)) {
        Hash hash = new Hash();
        hash.setValue(registerRules.register(key));
        response.setHash(hash);
        message = "Registration Completed:" + key;
      }
      else {
        message = "Registration Fail: Try Later";
      }
  } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | InvalidKeySpecException | DBException e1) {
      e1.printStackTrace();
      message = "Unexpected Error!";
    }

    try {
      signature = cryptoAgent.generateSignature(message + response.getHash().getValue());
      Signature signed = new Signature().value(signature);
      response.setSignature(signed);
      response.setMessage(message);
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      e.printStackTrace();
    }

    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
