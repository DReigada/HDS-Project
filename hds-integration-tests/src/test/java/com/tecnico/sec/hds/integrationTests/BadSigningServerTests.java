package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.BeforeClass;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class BadSigningServerTests {

  private static ServersWrapper server;
  private static ServerHelper serverHelper;


  @BeforeClass
  public static void start() throws GeneralSecurityException, IOException, OperatorCreationException {
    serverHelper = new ServerHelper();
    List<String> serversUrls = serverHelper.startServers(4);

    server = new ServersWrapper("user1", "pass1", serversUrls);

    server.register();
  }

}
