package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import io.swagger.client.model.SendAmountRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class BroadCastTester {
  private static ServersWrapper server;
  private static ServerHelper serverHelper;

  @BeforeClass
  public static void start() throws Exception {
    serverHelper = new ServerHelper();
    List<String> serversUrls = serverHelper.startServers(3);
    serversUrls.add(serverHelper.startByzantineServer(8));
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
    String response = server.sendAmount(body);
    System.out.println(response);
  }



  /*private Tuple<String, DefaultApi> spyServer(String url) throws Exception{
    ApiClient client = new ApiClient().setBasePath(url);
    DefaultApi spyServer = spy(new DefaultApi(client));
    String[] urlParsed = url.split(":");
    String port = urlParsed[2];
    String ip = urlParsed[1].substring(2);
    String fileName = "bank" + ip + "_" + port;
    ReliableBroadcastHelper reliableBroadcastHelper = spy(new ReliableBroadcastHelper());

  }*/
}
