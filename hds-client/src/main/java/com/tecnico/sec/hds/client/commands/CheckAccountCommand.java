package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.CheckAmountRequest;
import io.swagger.client.model.CheckAmountResponse;

public class CheckAccountCommand extends AbstractCommand {
  private static final String name = "check_account";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {
    CheckAmountRequest checkAmountRequest = new CheckAmountRequest().publicKey(client.key);
    CheckAmountResponse checkAmountResponse = client.server.checkAmount(checkAmountRequest);

    System.out.println(checkAmountResponse);
  }

  @Override
  public String getName() {
    return name;
  }
}
