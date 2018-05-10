package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.model.CheckAccountRequest;
import io.swagger.client.model.CheckAccountResponse;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;

public class Test1 {
  private static ServerHelper serverHelper = new ServerHelper();

  @AfterClass
  public static void afterClass() {
    new File("user1KeyStore.jce").delete();
  }

  @Test
  public void test1() throws Exception {
    serverHelper.writeConfig(3);

    List<String> serversUrls = serverHelper.startServers(3, ServerTypeWrapper.ServerType.NORMAL);

    ServersWrapper server = new ServersWrapper("user1", "pass", serversUrls);

    server.register();
    Optional<Tuple<CheckAccountResponse, Long>> response = server.checkAccount(new CheckAccountRequest(), false);

    assertTrue(response.isPresent());

    serverHelper.stopServers();
    serverHelper.deleteConfig();
  }
}
