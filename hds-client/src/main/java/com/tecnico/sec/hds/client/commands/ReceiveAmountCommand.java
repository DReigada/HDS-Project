package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.model.*;

import java.security.GeneralSecurityException;
import java.util.Optional;

public class ReceiveAmountCommand extends AbstractCommand {
  private static final String name = "receive_amount";

  @Override
  public void doRun(Client client, String[] args) {
    Hash hash = new Hash().value(args[0].trim());

    Optional<Tuple<CheckAccountResponse, Long>> checkAccountResponseOpt =
        client.server.checkAccount(new CheckAccountRequest(), true);

    if (checkAccountResponseOpt.isPresent()) {
      Optional<TransactionInformation> transactionOpt =
          checkAccountResponseOpt.get()
              .first.getPending().stream()
              .filter(a -> a.getSendHash().getValue().equals(hash.getValue()))
              .findFirst();

      try {
        if (transactionOpt.isPresent()) {
          TransactionInformation transaction = transactionOpt.get();
          ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest();

          receiveAmountRequest.setSourceKey(new PubKey().value(transaction.getSourceKey()));
          receiveAmountRequest.setDestKey(new PubKey().value(transaction.getDestKey()));
          receiveAmountRequest.amount(Integer.valueOf(transaction.getAmount()));
          receiveAmountRequest.setTransHash(transaction.getSendHash());

          Optional<String> receiveAmountResponse = client.server.receiveAmount(receiveAmountRequest);

          System.out.println(receiveAmountResponse.orElse("Failed to call receive amount"));
        }

      } catch (GeneralSecurityException e) {
        System.out.println("Failed to call receive amount");
        e.printStackTrace();
      }
    } else {
      System.out.println("Failed to call Audit in receive amount");
    }

  }

  @Override
  public String getName() {
    return name;
  }
}
