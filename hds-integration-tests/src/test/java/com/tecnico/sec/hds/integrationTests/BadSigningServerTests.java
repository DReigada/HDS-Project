package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
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

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class BadSigningServerTests {

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
    new File("user1KeyStore.jce").delete();
    new File("user2KeyStore.jce").delete();
  }

  public static Tuple<CheckAccountResponse, Long> verifyAmount(ServersWrapper server, long expected) {
    Optional<Tuple<CheckAccountResponse, Long>> checkAccount2 = server.checkAccount(new CheckAccountRequest(), false);

    assertTrue(checkAccount2.isPresent());
    assertEquals(expected, (long) checkAccount2.get().second);
    return checkAccount2.get();
  }

  @Test
  public void maxBadServers() throws GeneralSecurityException, IOException, OperatorCreationException {

    ServersWrapper server = new ServersWrapper("user1", "pass1", serversUrls);

    server.register();

    verifyAmount(server, 1000L);
    SendAmountRequest send1req = new SendAmountRequest().amount(200).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    Tuple<CheckAccountResponse, Long> checkAccount = verifyAmount(server, 800L);

    String trans = checkAccount.first.getPending().get(0).getSendHash().getValue();

    ReceiveAmountRequest receiveAmountRequest =
        new ReceiveAmountRequest()
            .sourceKey(server.securityHelper.key)
            .destKey(server.securityHelper.key)
            .amount(200)
            .transHash(new Hash().value(trans));

    server.receiveAmount(receiveAmountRequest);

    verifyAmount(server, 1000L);
  }
/*
  @Test
  public void toMuchBadServers() throws GeneralSecurityException, IOException, OperatorCreationException{
    ArrayList<ServerTypeWrapper> serverTypes = ServerTypeWrapper.get();
    serverTypes.get(6).setType(ServerTypeWrapper.ServerType.BADSIGN);
    ServersWrapper server = new ServersWrapper("user2", "pass2", serversUrls);


    verifyAmount(server, 1000L);
    SendAmountRequest send1req = new SendAmountRequest().amount(200).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    Tuple<CheckAccountResponse, Long> checkAccount = verifyAmount(server, 800L);

    String trans = checkAccount.first.getPending().get(0).getSendHash().getValue();

    ReceiveAmountRequest receiveAmountRequest =
        new ReceiveAmountRequest()
            .sourceKey(server.securityHelper.key)
            .destKey(server.securityHelper.key)
            .amount(200)
            .transHash(new Hash().value(trans));

    server.receiveAmount(receiveAmountRequest);

    verifyAmount(server, 1000L);

  }*/

}
