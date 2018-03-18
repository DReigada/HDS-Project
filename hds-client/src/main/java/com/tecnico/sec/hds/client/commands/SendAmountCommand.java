package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.SendAmountRequest;

public class SendAmountCommand extends AbstractCommand {
  private static final String name = "send_amount";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {
    SendAmountRequest sendAmount = new SendAmountRequest().destKey(client.key).sourceKey(client.key).amount(1);
    client.server.sendAmount(sendAmount);
  }


  @Override
  public String getName() {
    return name;
  }
}
