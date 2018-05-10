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

public class MyTest {
  private static ServersWrapper server;
  private static ServerHelper serverHelper;

  @BeforeClass
  public static void start() throws GeneralSecurityException, IOException, OperatorCreationException {
    serverHelper = new ServerHelper();
    serverHelper.writeConfig(4);
    List<String> serversUrls = serverHelper.startServers(4, ServerTypeWrapper.ServerType.NORMAL);

    server = new ServersWrapper("user1", "pass1", serversUrls);

    server.register();
  }

  @AfterClass
  public static void afterClass() {
    serverHelper.stopServers();
    serverHelper.deleteConfig();
    new File("user1KeyStore.jce").delete();
  }

  public static Tuple<CheckAccountResponse, Long> verifyAmount(long expected) {
    Optional<Tuple<CheckAccountResponse, Long>> checkAccount2 = server.checkAccount(new CheckAccountRequest(), false);

    assertTrue(checkAccount2.isPresent());
    assertEquals(expected, (long) checkAccount2.get().second);
    return checkAccount2.get();
  }

  @Test
  public void test1() throws GeneralSecurityException, IOException {

    verifyAmount(1000L);

    SendAmountRequest send1req = new SendAmountRequest().amount(200).destKey(server.securityHelper.key);

    server.sendAmount(send1req);

    Tuple<CheckAccountResponse, Long> checkAccount = verifyAmount(800L);

    String trans = checkAccount.first.getPending().get(0).getSendHash().getValue();

    ReceiveAmountRequest receiveAmountRequest =
        new ReceiveAmountRequest()
            .sourceKey(server.securityHelper.key)
            .destKey(server.securityHelper.key)
            .amount(200)
            .transHash(new Hash().value(trans));

    server.receiveAmount(receiveAmountRequest);

    verifyAmount(1000L);
  }
}
