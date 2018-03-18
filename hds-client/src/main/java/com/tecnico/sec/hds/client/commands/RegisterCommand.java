package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.RegisterRequest;

public class RegisterCommand extends AbstractCommand {
  private static final String name = "register";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {
    RegisterRequest request = new RegisterRequest().publicKey(client.key);
    client.server.register(request);
  }

  @Override
  public String getName() {
    return name;
  }
}
