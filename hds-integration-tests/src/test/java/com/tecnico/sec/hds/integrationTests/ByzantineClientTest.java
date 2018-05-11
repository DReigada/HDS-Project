package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.integrationTests.util.ByzantineWrapper;
import com.tecnico.sec.hds.integrationTests.util.ServerHelper;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.model.*;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

import static com.tecnico.sec.hds.integrationTests.util.TestHelpers.verifyNumberOfPendingTransactions;
import static com.tecnico.sec.hds.integrationTests.util.TestHelpers.verifyNumberOfTransactions;
import static org.junit.Assert.assertTrue;

public class ByzantineClientTest {
  private static ByzantineWrapper byzantineClient;
  private static ServersWrapper normalClient;
  private static ServerHelper serverHelper;

  @BeforeClass
  public static void start() {
    System.setProperty("hds.coin.crypto.useLocalhost", "false");
    serverHelper = new ServerHelper();
    serverHelper.writeConfig(7);
  }

  @Before
  public void populate() throws GeneralSecurityException, IOException, OperatorCreationException {
    List<String> serversUrls = serverHelper.startServers(0, 7, ServerTypeWrapper.ServerType.NORMAL);
    byzantineClient = new ByzantineWrapper("user1", "pass1", serversUrls);
    normalClient = new ServersWrapper("user2", "pass2", serversUrls);
    normalClient.register();
    byzantineClient.register();
  }


  @Test
  public void transactionDiffTo2Server() throws Exception {
    SendAmountRequest normalBody = byzantineClient.getSendAmountBody(sendAmountRequest());
    SendAmountRequest changedBody = byzantineClient.getSendAmountBody(sendAmountRequest().amount(91));
    byzantineClient.sendAmount(normalBody, changedBody, 2);

    /*byzantineClient.setWriteBackSync(true);

    byzantineClient.checkAccount(new CheckAccountRequest(), true);
    byzantineClient.setWriteBackSync(false);*/

    Thread.sleep(3000);
    verifyNumberOfTransactions(byzantineClient, 2);
    verifyNumberOfPendingTransactions(normalClient, 1);
  }

  @Test
  public void transactionDiffTo3Server() throws Exception {
    SendAmountRequest normalBody = byzantineClient.getSendAmountBody(sendAmountRequest());
    SendAmountRequest changedBody = byzantineClient.getSendAmountBody(sendAmountRequest().amount(91));
    byzantineClient.sendAmount(normalBody, changedBody, 3);
    verifyNumberOfTransactions(byzantineClient, 1);
    verifyNumberOfPendingTransactions(normalClient, 0);
  }

  @Test
  public void wrongSignTransactionDiffTo3ServerWithDiff() throws Exception {
    SendAmountRequest normalBody = byzantineClient.getSendAmountBody(sendAmountRequest());
    SendAmountRequest changedBody = byzantineClient.getSendAmountBody(sendAmountRequest().amount(91));
    changedBody.setSignature(byzantineClient.signMessage("Im Byzantine!"));
    byzantineClient.sendAmount(normalBody, changedBody, 3);
    verifyNumberOfTransactions(byzantineClient, 1);
    verifyNumberOfPendingTransactions(normalClient, 0);
  }

  @Test
  public void allSignaturesWrong() throws Exception {
    SendAmountRequest normalBody = byzantineClient.getSendAmountBody(sendAmountRequest());
    SendAmountRequest changedBody = byzantineClient.getSendAmountBody(sendAmountRequest().amount(91));
    changedBody.setSignature(byzantineClient.signMessage("Im Byzantine!"));
    normalBody.setSignature(byzantineClient.signMessage("Im Byzantine!!!"));
    byzantineClient.sendAmount(normalBody, changedBody, 3);
    verifyNumberOfTransactions(byzantineClient, 1);
    verifyNumberOfPendingTransactions(normalClient, 0);
  }

  @Test
  public void replayAttacksSendAmount() throws Exception {
    SendAmountRequest body = byzantineClient.getSendAmountBody(sendAmountRequest());
    String bodyHash = body.getHash().getValue();
    String clientLastHash = byzantineClient.getLastHash();
    for (int i = 0; i < 5; i++) {
      byzantineClient.sendAmount(body);
      byzantineClient.setLastHash(clientLastHash);
      body.getHash().setValue(bodyHash);
    }
    verifyNumberOfTransactions(byzantineClient, 2);
    verifyNumberOfPendingTransactions(normalClient, 1);
  }


  @Test
  public void replayAttacksReceiveAmount() throws Exception {
    SendAmountRequest body = byzantineClient.getSendAmountBody(sendAmountRequest());
    byzantineClient.sendAmount(body);
    Optional<Tuple<CheckAccountResponse, Long>> checkAccountResponse = normalClient.checkAccount(new CheckAccountRequest(), true);
    assertTrue(checkAccountResponse.isPresent());
    TransactionInformation lastTransaction = checkAccountResponse.get().first.getPending().get(0);
    String bodyHash = lastTransaction.getSendHash().getValue();
    String clientLastHash = normalClient.securityHelper.getLastHash().getValue();
    for (int i = 0; i < 5; i++) {
      normalClient.receiveAmount(
          new ReceiveAmountRequest()
              .amount(Integer.valueOf(lastTransaction.getAmount()))
              .destKey(new PubKey().value(lastTransaction.getDestKey()))
              .sourceKey(new PubKey().value(lastTransaction.getSourceKey()))
              .transHash(new Hash().value(bodyHash)));
      normalClient.securityHelper.setLastHash(new Hash().value(clientLastHash));
    }
    verifyNumberOfPendingTransactions(normalClient, 0);
  }

  @After
  public void afterClass() {
    serverHelper.stopServers();
    ServerTypeWrapper.cleanServers();
    new File("HDSDB8180.mv.db").delete();
    new File("HDSDB8181.mv.db").delete();
    new File("HDSDB8182.mv.db").delete();
    new File("HDSDB8183.mv.db").delete();
    new File("HDSDB8184.mv.db").delete();
    new File("HDSDB8185.mv.db").delete();
    new File("HDSDB8186.mv.db").delete();
    new File("user1KeyStore.jce").delete();
    new File("user2KeyStore.jce").delete();
    new File("banklocalhost_8180KeyStore.jce").delete();
    new File("banklocalhost_8181KeyStore.jce").delete();
    new File("banklocalhost_8182KeyStore.jce").delete();
    new File("banklocalhost_8183KeyStore.jce").delete();
    new File("banklocalhost_8184KeyStore.jce").delete();
    new File("banklocalhost_8185KeyStore.jce").delete();
    new File("banklocalhost_8186KeyStore.jce").delete();
  }

  @AfterClass
  public static void clean(){
    serverHelper.deleteConfig();
    ServerTypeWrapper.cleanServers();
  }

  protected SendAmountRequest sendAmountRequest() {
    return new SendAmountRequest()
        .amount(1)
        .sourceKey(byzantineClient.securityHelper.key)
        .destKey(normalClient.securityHelper.key);
  }
}