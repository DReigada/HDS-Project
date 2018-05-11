package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.integrationTests.util.ServerHelper;
import com.tecnico.sec.hds.integrationTests.util.TestHelpers;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.model.CheckAccountResponse;
import io.swagger.client.model.Hash;
import io.swagger.client.model.ReceiveAmountRequest;
import io.swagger.client.model.SendAmountRequest;
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
    new File("user2KeyStore.jce").delete();
    new File("user3KeyStore.jce").delete();
    new File("user4KeyStore.jce").delete();
    new File("user5KeyStore.jce").delete();
    new File("user6KeyStore.jce").delete();
  }

  @Test
  public void testServerThatEchoes10Times() throws GeneralSecurityException, IOException, OperatorCreationException {

    ServerTypeWrapper.changeServersType(0, 9, ServerTypeWrapper.ServerType.NOECHOES);

    ServerTypeWrapper.changeServerType(9, ServerTypeWrapper.ServerType.ECHOS10);


    ServersWrapper server = new ServersWrapper("user1","pass1", serversUrls);

    server.register();


    TestHelpers.verifyAmount(server, 1000L);

    SendAmountRequest send1req = new SendAmountRequest().amount(50).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    TestHelpers.verifyAmount(server, 1000L);
  }

  @Test
  public void test7ServerThatEchoes10Times() throws GeneralSecurityException, IOException, OperatorCreationException{
    ServerTypeWrapper.changeServersType(0, 3, ServerTypeWrapper.ServerType.NOECHOES);

    ServerTypeWrapper.changeServersType(3,10, ServerTypeWrapper.ServerType.ECHOS10);


    ServersWrapper server = new ServersWrapper("user2","pass2", serversUrls);

    server.register();


    TestHelpers.verifyAmount(server, 1000L);

    SendAmountRequest send1req = new SendAmountRequest().amount(50).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    TestHelpers.verifyAmount(server, 950L);
  }

  @Test
  public void test6ServerThatEchoes10Times() throws GeneralSecurityException, IOException, OperatorCreationException{
    ServerTypeWrapper.changeServersType(0, 4, ServerTypeWrapper.ServerType.NOECHOES);

    ServerTypeWrapper.changeServersType(4,10, ServerTypeWrapper.ServerType.ECHOS10);


    ServersWrapper server = new ServersWrapper("user3","pass3", serversUrls);

    server.register();


    TestHelpers.verifyAmount(server, 1000L);

    SendAmountRequest send1req = new SendAmountRequest().amount(50).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    TestHelpers.verifyAmount(server, 1000L);
  }


  @Test
  public void testServerThatEchoes10TimesReceive() throws GeneralSecurityException, IOException, OperatorCreationException {


    ServerTypeWrapper.changeServersType(0, 10, ServerTypeWrapper.ServerType.NORMAL);


    ServersWrapper server = new ServersWrapper("user4","pass4", serversUrls);

    server.register();


    TestHelpers.verifyAmount(server, 1000L);

    SendAmountRequest send1req = new SendAmountRequest().amount(50).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    Tuple<CheckAccountResponse, Long> checkAccount = TestHelpers.verifyAmount(server, 950);

    String trans = checkAccount.first.getPending().get(0).getSendHash().getValue();

    ReceiveAmountRequest receiveAmountRequest =
        new ReceiveAmountRequest()
            .sourceKey(server.securityHelper.key)
            .destKey(server.securityHelper.key)
            .amount(50)
            .transHash(new Hash().value(trans));


    ServerTypeWrapper.changeServersType(0, 8, ServerTypeWrapper.ServerType.NOECHOES);

    ServerTypeWrapper.changeServerType(9, ServerTypeWrapper.ServerType.ECHOS10);

    server.receiveAmount(receiveAmountRequest);

    TestHelpers.verifyAmount(server, 950L);
  }

  @Test
  public void test7ServerThatEchoes10TimesReceive() throws GeneralSecurityException, IOException, OperatorCreationException {

    ServerTypeWrapper.changeServersType(0, 10, ServerTypeWrapper.ServerType.NORMAL);

    ServersWrapper server = new ServersWrapper("user5","pass5", serversUrls);

    server.register();


    TestHelpers.verifyAmount(server, 1000L);

    SendAmountRequest send1req = new SendAmountRequest().amount(50).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    Tuple<CheckAccountResponse, Long> checkAccount = TestHelpers.verifyAmount(server, 950L);

    String trans = checkAccount.first.getPending().get(0).getSendHash().getValue();

    ReceiveAmountRequest receiveAmountRequest =
        new ReceiveAmountRequest()
            .sourceKey(server.securityHelper.key)
            .destKey(server.securityHelper.key)
            .amount(50)
            .transHash(new Hash().value(trans));


    ServerTypeWrapper.changeServersType(0, 3, ServerTypeWrapper.ServerType.NOECHOES);
    ServerTypeWrapper.changeServersType(3,10, ServerTypeWrapper.ServerType.ECHOS10);

    server.receiveAmount(receiveAmountRequest);

    TestHelpers.verifyAmount(server, 1000L);
  }

  @Test
  public void test6ServerThatEchoes10TimesReceive() throws GeneralSecurityException, IOException, OperatorCreationException{

    ServerTypeWrapper.changeServersType(0, 10, ServerTypeWrapper.ServerType.NORMAL);

    ServersWrapper server = new ServersWrapper("user6","pass6", serversUrls);

    server.register();


    TestHelpers.verifyAmount(server, 1000L);

    SendAmountRequest send1req = new SendAmountRequest().amount(50).destKey(server.securityHelper.key);
    server.sendAmount(send1req);

    Tuple<CheckAccountResponse, Long> checkAccount = TestHelpers.verifyAmount(server, 950L);

    String trans = checkAccount.first.getPending().get(0).getSendHash().getValue();

    ReceiveAmountRequest receiveAmountRequest =
        new ReceiveAmountRequest()
            .sourceKey(server.securityHelper.key)
            .destKey(server.securityHelper.key)
            .amount(50)
            .transHash(new Hash().value(trans));


    ServerTypeWrapper.changeServersType(0, 4, ServerTypeWrapper.ServerType.NOECHOES);

    ServerTypeWrapper.changeServersType(4,10, ServerTypeWrapper.ServerType.ECHOS10);

    TestHelpers.verifyAmount(server, 950L);
  }


  @Test
  public void testFReadies(){

  }

}
