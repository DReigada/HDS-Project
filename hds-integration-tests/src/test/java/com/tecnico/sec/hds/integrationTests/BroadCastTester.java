package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.model.CheckAccountRequest;
import io.swagger.client.model.CheckAccountResponse;
import io.swagger.client.model.SendAmountRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class BroadCastTester {
  private static ServersWrapper server;
  private static ServerHelper serverHelper;

  @BeforeClass
  public static void start() throws Exception {
    System.setProperty("hds.coin.crypto.useLocalhost", "false");
    serverHelper = new ServerHelper();
    List<String> serversUrls = serverHelper.startServers(2);
    serversUrls.add(serverHelper.startByzantineServer(2));
    serversUrls.add(serverHelper.startByzantineServer(3));
    server = new ServersWrapper(
        "user1",
        "pass1",
        serversUrls);
  }

  @AfterClass
  public static void afterClass() {
    serverHelper.stopServers();
    new File("user1KeyStore.jce").delete();
  }

  @Test
  public void sendAmountSucess()throws Exception{
    server.register();
    SendAmountRequest body = new SendAmountRequest().amount(200).destKey(server.securityHelper.key);
    server.sendAmount(body);

    Optional<Tuple<CheckAccountResponse, Long>> response = server.checkAccount(new CheckAccountRequest(), false);
    long balance = response.get().second;
    assertEquals(balance, 1000);
  }
}
