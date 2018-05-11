package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.integrationTests.util.ServerHelper;
import com.tecnico.sec.hds.integrationTests.util.TestHelpers;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.model.*;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class ChangeTransOrderTest {

  private static ServerHelper serverHelper;
  private static List<String> serversUrls;

  @BeforeClass
  public static void start() {
    serverHelper = new ServerHelper();
    serverHelper.writeConfig(10);
    serversUrls = serverHelper.startServers(0, 10, ServerTypeWrapper.ServerType.NORMAL);
  }

  @AfterClass
  public static void afterClass() {
    serverHelper.stopServers();
    serverHelper.deleteConfig();
    ServerTypeWrapper.cleanServers();
    new File("user1KeyStore.jce").delete();
    new File("user2KeyStore.jce").delete();
    new File("user3KeyStore.jce").delete();
    new File("user4KeyStore.jce").delete();
    new File("user5KeyStore.jce").delete();
  }

  @Test
  public void tooMuchoutOfOrderTransactions() throws GeneralSecurityException, IOException, OperatorCreationException {

    ServerTypeWrapper.changeServersType(0, 10, ServerTypeWrapper.ServerType.NORMAL);

    ServersWrapper server = new ServersWrapper("user1", "pass1", serversUrls);

    server.register();

    TestHelpers.verifyAmount(server, 1000L);
    SendAmountRequest send1req = new SendAmountRequest().amount(50).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    TestHelpers.verifyAmount(server, 950L);

    server.sendAmount(send1req);


    TestHelpers.verifyAmount(server, 900L);

    server.sendAmount(send1req);

    TestHelpers.verifyAmount(server, 850L);


    ServerTypeWrapper.changeServersType(6, 10, ServerTypeWrapper.ServerType.BADORDER);

    Optional<Tuple<CheckAccountResponse, Long>> checkAccount = server.checkAccount(new CheckAccountRequest(), false);

    assertFalse(checkAccount.isPresent());
  }



  @Test
  public void allServersOutOfOrder() throws GeneralSecurityException, IOException, OperatorCreationException {

    ServerTypeWrapper.changeServersType(0, 10, ServerTypeWrapper.ServerType.NORMAL);


    ServersWrapper server = new ServersWrapper("user2", "pass2", serversUrls);

    server.register();

    TestHelpers.verifyAmount(server, 1000L);
    SendAmountRequest send1req = new SendAmountRequest().amount(50).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    TestHelpers.verifyAmount(server, 950L);

    server.sendAmount(send1req);


    TestHelpers.verifyAmount(server, 900L);

    server.sendAmount(send1req);

    TestHelpers.verifyAmount(server, 850L);

    ServerTypeWrapper.changeServersType(0, 10, ServerTypeWrapper.ServerType.SAMEBADORDER);

    Optional<Tuple<CheckAccountResponse, Long>> checkAccount = server.checkAccount(new CheckAccountRequest(), false);

    assertFalse(checkAccount.isPresent());
  }

  @Test
  public void limitOutOfOrder3Transactions() throws GeneralSecurityException, IOException, OperatorCreationException {

    ServerTypeWrapper.changeServersType(0, 10, ServerTypeWrapper.ServerType.NORMAL);


    ServersWrapper server = new ServersWrapper("user3", "pass3", serversUrls);

    server.register();

    TestHelpers.verifyAmount(server, 1000L);
    SendAmountRequest send1req = new SendAmountRequest().amount(50).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    TestHelpers.verifyAmount(server, 950L);

    server.sendAmount(send1req);


    TestHelpers.verifyAmount(server, 900L);

    server.sendAmount(send1req);

    TestHelpers.verifyAmount(server, 850L);


    ServerTypeWrapper.changeServersType(7, 10, ServerTypeWrapper.ServerType.BADORDER);

    TestHelpers.verifyAmount(server, 850L);

  }

  @Test
  public void limitOutOfOrderTransactions2() throws GeneralSecurityException, IOException, OperatorCreationException {

    ServerTypeWrapper.changeServersType(0, 10, ServerTypeWrapper.ServerType.NORMAL);

    ServersWrapper server1 = new ServersWrapper("user4", "pass4", serversUrls);
    ServersWrapper server2 = new ServersWrapper("user5", "pass5", serversUrls);
    server1.register();
    server2.register();


    ServerTypeWrapper.changeServersType(7, 10, ServerTypeWrapper.ServerType.BADORDER);

    TestHelpers.verifyAmount(server1, 1000L);
    TestHelpers.verifyAmount(server2, 1000L);
    SendAmountRequest send1req = new SendAmountRequest().amount(50).destKey(server1.securityHelper.key);
    server1.sendAmount(send1req);

    Tuple<CheckAccountResponse, Long> checkAccount = TestHelpers.verifyAmount(server1, 950L);

    String trans = checkAccount.first.getPending().get(0).getSendHash().getValue();

    ReceiveAmountRequest receiveAmountRequest =
        new ReceiveAmountRequest()
            .sourceKey(server1.securityHelper.key)
            .destKey(server1.securityHelper.key)
            .amount(50)
            .transHash(new Hash().value(trans));

    server1.receiveAmount(receiveAmountRequest);

    TestHelpers.verifyAmount(server1, 1000L);

    SendAmountRequest send2req = new SendAmountRequest().amount(100).destKey(server1.securityHelper.key);
    server2.sendAmount(send2req);

    TestHelpers.verifyAmount(server2, 900L);

    checkAccount = TestHelpers.verifyAmount(server1, 1000L);

    trans = checkAccount.first.getPending().get(0).getSendHash().getValue();

    receiveAmountRequest =
        new ReceiveAmountRequest()
            .sourceKey(server2.securityHelper.key)
            .destKey(server1.securityHelper.key)
            .amount(100)
            .transHash(new Hash().value(trans));

    server1.receiveAmount(receiveAmountRequest);

    TestHelpers.verifyAmount(server1, 1100L);
  }

}
