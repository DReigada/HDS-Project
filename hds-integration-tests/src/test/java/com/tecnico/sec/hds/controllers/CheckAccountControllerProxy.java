package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.server.controllers.CheckAccountController;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.CheckAccountApi;
import io.swagger.model.CheckAccountRequest;
import io.swagger.model.CheckAccountResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

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

    return checkAccountController.checkAccount(body);
  }
}
