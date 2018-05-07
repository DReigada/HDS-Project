package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.client.ServersWrapper;
import io.swagger.client.model.CheckAccountRequest;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class Test1 {

  @AfterClass
  public static void afterClass() {
    new File("user1KeyStore.jce").delete();
  }

  @Test
  public void test1() throws Exception {
    List<String> serversUrls = ServerHelper.startServers(2);


    ServersWrapper server = new ServersWrapper("user1", "pass", serversUrls);

    server.register();
    server.checkAccount(new CheckAccountRequest());
  }
}
