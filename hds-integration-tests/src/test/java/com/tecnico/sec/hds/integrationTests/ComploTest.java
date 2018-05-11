package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.integrationTests.util.ByzantineWrapper;
import com.tecnico.sec.hds.integrationTests.util.ServerHelper;
import com.tecnico.sec.hds.integrationTests.util.TestHelpers;
import io.swagger.client.model.SendAmountRequest;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class ComploTest {
  private static ByzantineWrapper byzantineClient;
  private static ServersWrapper normalClient;
  private static ServerHelper serverHelper;

  @BeforeClass
  public static void start() {
    System.setProperty("hds.coin.crypto.useLocalhost", "false");
    serverHelper = new ServerHelper();
    serverHelper.writeConfig(4);
  }

  @Before
  public void populate() throws GeneralSecurityException, IOException, OperatorCreationException {
    List<String> serversUrls = serverHelper.startServers(0, 4, ServerTypeWrapper.ServerType.NORMAL);
    byzantineClient = new ByzantineWrapper("user1", "pass1", serversUrls);
    normalClient = new ServersWrapper("user2", "pass2", serversUrls);
    normalClient.register();
    byzantineClient.register();
  }

  @Test
  public void bla() throws Exception{
    SendAmountRequest normalBody = byzantineClient.getSendAmountBody(sendAmountRequest());
    SendAmountRequest changedBody = byzantineClient.getSendAmountBody(sendAmountRequest().amount(91));
    byzantineClient.sendAmount(normalBody, changedBody, 1);
    ServerTypeWrapper.changeServerType(3,ServerTypeWrapper.ServerType.IGNORE);

    TestHelpers.verifyNumberOfTransactions(normalClient,2);
  }

  @After
  public void afterClass() {
    serverHelper.stopServers();
    ServerTypeWrapper.cleanServers();
    new File("HDSDB8180.mv.db").delete();
    new File("HDSDB8181.mv.db").delete();
    new File("HDSDB8182.mv.db").delete();
    new File("HDSDB8183.mv.db").delete();
    new File("user1KeyStore.jce").delete();
    new File("user2KeyStore.jce").delete();
    new File("banklocalhost_8180KeyStore.jce").delete();
    new File("banklocalhost_8181KeyStore.jce").delete();
    new File("banklocalhost_8182KeyStore.jce").delete();
    new File("banklocalhost_8183KeyStore.jce").delete();
  }

  @AfterClass
  public static void clean(){
    serverHelper.deleteConfig();
    ServerTypeWrapper.cleanServers();
  }

  private SendAmountRequest sendAmountRequest() {
    return new SendAmountRequest()
        .amount(1)
        .sourceKey(byzantineClient.securityHelper.key)
        .destKey(normalClient.securityHelper.key);
  }
}
