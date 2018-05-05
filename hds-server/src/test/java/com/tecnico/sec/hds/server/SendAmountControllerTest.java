package com.tecnico.sec.hds.server;

import com.tecnico.sec.hds.server.controllers.CheckAccountController;
import com.tecnico.sec.hds.server.controllers.RegisterController;
import com.tecnico.sec.hds.server.controllers.SendAmountController;
import com.tecnico.sec.hds.server.db.commands.util.Migrations;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.model.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SendAmountControllerTest {
  private static List<SendAmountRequest> transactions;
  private static CryptoAgent agent1;
  private static CryptoAgent agent2;
  private static SendAmountController sendAmountController;

  @BeforeClass
  public static void populate() throws Exception {
    Migrations.migrate();
    agent1 = new CryptoAgent("user12345", "pass");
    agent2 = new CryptoAgent("user23456", "otherpass");
    RegisterController registerController = new RegisterController();
    RegisterRequest registerRequest1 = new RegisterRequest().publicKey(new PubKey().value(agent1.getStringPublicKey()));
    registerRequest1.setSignature(new Signature().value(agent1.generateSignature(agent1.getStringPublicKey())));
    RegisterRequest registerRequest2 = new RegisterRequest().publicKey(new PubKey().value(agent2.getStringPublicKey()));
    registerRequest2.setSignature(new Signature().value(agent2.generateSignature(agent2.getStringPublicKey())));
    ResponseEntity<RegisterResponse> responseEntity1 = registerController.register(registerRequest1);
    registerController.register(registerRequest2);
    RegisterResponse registerResponse1 = responseEntity1.getBody();
    transactions = new ArrayList<>();
    Signature signature = new Signature().value(agent1.generateSignature(agent1.getStringPublicKey() +
        agent2.getStringPublicKey() + 100 + registerResponse1.getHash().getValue()));
    SendAmountRequest sendAmountRequest = new SendAmountRequest();
    sendAmountRequest.setAmount(100);
    sendAmountRequest.setLastHash(registerResponse1.getHash());
    sendAmountRequest.setDestKey(new PubKey().value(agent2.getStringPublicKey()));
    sendAmountRequest.setSourceKey(new PubKey().value(agent1.getStringPublicKey()));
    sendAmountRequest.setSignature(signature);
    sendAmountController = new SendAmountController();
    sendAmountController.sendAmount(sendAmountRequest);
    transactions.add(sendAmountRequest);
  }

  @AfterClass
  public static void clean() {
    File file = new File(QueryHelpers.getDBFilePath() + ".mv.db");
    file.delete();
  }

  @Test
  @Ignore
  public void ReplayAttack() throws Exception {
    for (SendAmountRequest request : transactions) {
      for (int i = 0; i < 10; i++) {
        sendAmountController.sendAmount(request);
      }
    }
    CheckAccountController checkAccountController = new CheckAccountController();
    CheckAccountRequest checkAccountRequest = new CheckAccountRequest().publicKey(new PubKey().value(agent1.getStringPublicKey()));
    CheckAccountResponse response = checkAccountController.checkAccount(checkAccountRequest).getBody();
    //assertEquals("900", response.getAmount()); TODO: FIX THIS SHIT
  }

}
