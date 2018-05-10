package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.server.controllers.RegisterController;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.RegisterApi;
import io.swagger.model.RegisterRequest;
import io.swagger.model.RegisterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;

@Controller
public class RegisterControllerProxy implements RegisterApi {

  private RegisterController registerController;

  private ServerTypeWrapper serverTypeWrapper;

  public RegisterControllerProxy(CryptoAgent cryptoAgent, QueryHelpers queryHelpers, ServerTypeWrapper serverTypeWrapper) {
    this.registerController = new RegisterController(cryptoAgent, queryHelpers);
    this.serverTypeWrapper = serverTypeWrapper;
  }

  @Override
  public ResponseEntity<RegisterResponse> register(@Valid RegisterRequest body) {
    return registerController.register(body);
  }
}
