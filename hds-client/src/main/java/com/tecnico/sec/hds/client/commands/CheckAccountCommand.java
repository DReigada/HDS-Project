package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import com.tecnico.sec.hds.util.TransactionGetter;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.model.CheckAccountRequest;
import io.swagger.client.model.CheckAccountResponse;

import java.util.Optional;

public class CheckAccountCommand extends AbstractCommand {
  private static final String name = "check_account";

  @Override
  public void doRun(Client client, String[] args) {

    CheckAccountRequest checkAccountRequest = new CheckAccountRequest();

    Optional<Tuple<CheckAccountResponse, Long>> response = client.server.checkAccount(checkAccountRequest);

    if (response.isPresent()) {
//        System.out.println("Public Key: " + response.get().key);
      String pendingString = TransactionGetter.getTransactionListMessage(response.get().first.getPending());

      System.out.println("Balance: " + response.get().second + "\n");
      System.out.println(pendingString);
    } else {
      System.out.println("Unexpected error from server");
    }
  }

  @Override
  public String getName() {
    return name;
  }
}

