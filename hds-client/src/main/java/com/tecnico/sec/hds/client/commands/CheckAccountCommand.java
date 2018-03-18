package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.CheckAmountRequest;

public class CheckAccountCommand extends AbstractCommand {
  private static final String name = "check_account";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {
    CheckAmountRequest checkAmountRequest = new CheckAmountRequest().publicKey(client.key);
    client.server.checkAmount(checkAmountRequest);
  }

  @Override
  public String getName() {
    return name;
  }
}
