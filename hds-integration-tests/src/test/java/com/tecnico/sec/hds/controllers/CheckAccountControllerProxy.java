package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.integrationTests.util.ServerHelper;
import com.tecnico.sec.hds.integrationTests.util.TestHelpers;
import com.tecnico.sec.hds.server.controllers.CheckAccountController;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.CheckAccountApi;
import io.swagger.model.CheckAccountRequest;
import io.swagger.model.CheckAccountResponse;
import io.swagger.model.Signature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Base64;
import java.util.Collections;

@Controller
public class CheckAccountControllerProxy implements CheckAccountApi {

  private CheckAccountController checkAccountController;

  private ServerTypeWrapper serverTypeWrapper;

  public CheckAccountControllerProxy(CryptoAgent cryptoAgent, QueryHelpers queryHelpers, ServerTypeWrapper serverTypeWrapper) {
    this.checkAccountController = new CheckAccountController(cryptoAgent, queryHelpers);
    this.serverTypeWrapper = serverTypeWrapper;
  }

  @Override
  public ResponseEntity<CheckAccountResponse> checkAccount(@RequestBody @Valid CheckAccountRequest body) {

    ResponseEntity<CheckAccountResponse> response;

    switch (serverTypeWrapper.getType()) {
      case NORMAL:
        return checkAccountController.checkAccount(body);
      case BYZANTINE:
        return null;
      case BADSIGN:
        response = checkAccountController.checkAccount(body);
        response.getBody().setSignature(new Signature().value(Base64.getEncoder().encodeToString("FakeSignature".getBytes())));
        return response;
      case BADORDER:
        response = checkAccountController.checkAccount(body);
        TestHelpers.shuffleToDifferent(response.getBody().getHistory());
        return response;
      case SAMEBADORDER:
        response = checkAccountController.checkAccount(body);
        response.getBody().getHistory().remove(1);
        return response;
      case ECHOS10:
        return checkAccountController.checkAccount(body);
      case NOECHOES:
        return checkAccountController.checkAccount(body);
      case IGNORE:
        Thread.currentThread().stop();
      default:
        throw new RuntimeException("This should never happen");
    }
  }
}
