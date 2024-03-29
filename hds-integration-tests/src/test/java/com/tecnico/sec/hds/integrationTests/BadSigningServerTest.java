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
import java.util.List;
import java.util.Optional;

import static junit.framework.Assert.assertFalse;


public class BadSigningServerTest {

  private static ServerHelper serverHelper;
  private static List<String> serversUrls;


  @BeforeClass
  public static void start() {
    serverHelper = new ServerHelper();
    serverHelper.writeConfig(10);
    serversUrls = serverHelper.startServers(0, 7, ServerTypeWrapper.ServerType.NORMAL);
    List<String> badServerUrls = serverHelper.startServers(7, 10, ServerTypeWrapper.ServerType.BADSIGN);

    serversUrls.addAll(badServerUrls);
  }

  @AfterClass
  public static void afterClass() {
    serverHelper.stopServers();
    serverHelper.deleteConfig();
    ServerTypeWrapper.cleanServers();
    new File("user1KeyStore.jce").delete();
    new File("user2KeyStore.jce").delete();
    new File("user3KeyStore.jce").delete();
  }


  @Test
  public void maxBadServers() throws GeneralSecurityException, IOException, OperatorCreationException {

    ServersWrapper server = new ServersWrapper("user1", "pass1", serversUrls);
    ServerTypeWrapper.changeServerType(6, ServerTypeWrapper.ServerType.NORMAL);
    server.register();

    TestHelpers.verifyAmount(server, 1000L);
    SendAmountRequest send1req = new SendAmountRequest().amount(200).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    Tuple<CheckAccountResponse, Long> checkAccount = TestHelpers.verifyAmount(server, 800L);

    String trans = checkAccount.first.getPending().get(0).getSendHash().getValue();

    ReceiveAmountRequest receiveAmountRequest =
        new ReceiveAmountRequest()
            .sourceKey(server.securityHelper.key)
            .destKey(server.securityHelper.key)
            .amount(200)
            .transHash(new Hash().value(trans));

    server.receiveAmount(receiveAmountRequest);

    TestHelpers.verifyAmount(server, 1000L);
  }

  @Test
  public void toMuchBadServers() throws GeneralSecurityException, IOException, OperatorCreationException {

    ServerTypeWrapper.changeServerType(6, ServerTypeWrapper.ServerType.BADSIGN);
    ServersWrapper server = new ServersWrapper("user2", "pass2", serversUrls);

    server.register();

    SendAmountRequest send1req = new SendAmountRequest().amount(200).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    Optional<Tuple<CheckAccountResponse, Long>> checkAccount2 = server.checkAccount(new CheckAccountRequest(), false);

    assertFalse(checkAccount2.isPresent());

  }

  @Test
  public void scenario() throws GeneralSecurityException, IOException, OperatorCreationException {

    ServerTypeWrapper.changeServerType(6, ServerTypeWrapper.ServerType.NORMAL);
    ServersWrapper server = new ServersWrapper("user3", "pass3", serversUrls);

    server.register();

    TestHelpers.verifyAmount(server, 1000L);

    SendAmountRequest send1req = new SendAmountRequest().amount(200).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    TestHelpers.verifyAmount(server, 800L);

    Optional<Tuple<CheckAccountResponse, Long>> checkAccount2 = server.checkAccount(new CheckAccountRequest(), false);

    ServerTypeWrapper.changeServerType(6, ServerTypeWrapper.ServerType.BADSIGN);

  }

}
