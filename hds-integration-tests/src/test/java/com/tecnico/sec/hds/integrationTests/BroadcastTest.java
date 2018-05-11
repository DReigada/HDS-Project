package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.integrationTests.util.ServerHelper;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class BroadcastTest {
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
  }

  @Test
  public void testServerThatEchoes10Times() throws GeneralSecurityException, IOException, OperatorCreationException {
    ServersWrapper server = new ServersWrapper("user1","pass1", serversUrls);

    server.register();


  /*
    TestHelpers.verifyAmount(server, 1000L);
    SendAmountRequest send1req = new SendAmountRequest().amount(50).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    TestHelpers.verifyAmount(server, 1000L);
*/
  }

}
