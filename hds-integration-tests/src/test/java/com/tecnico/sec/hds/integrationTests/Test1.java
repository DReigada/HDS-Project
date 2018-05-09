package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import io.swagger.client.model.CheckAccountRequest;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class Test1 {
  private static ServerHelper serverHelper = new ServerHelper();

  @AfterClass
  public static void afterClass() {
    new File("user1KeyStore.jce").delete();
  }

  @Test
  public void test1() throws Exception {
    List<String> serversUrls = serverHelper.startServers(3);

    ServersWrapper server = new ServersWrapper("user1", "pass", serversUrls);

    server.register();
    server.checkAccount(new CheckAccountRequest(), false);

    serverHelper.stopServers();
  }
}
