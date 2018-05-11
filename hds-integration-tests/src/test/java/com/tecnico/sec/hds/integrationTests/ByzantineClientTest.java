package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.integrationTests.util.ServerHelper;
import com.tecnico.sec.hds.util.Tuple;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import io.swagger.client.model.*;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
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
  private void verifyNumberOfTransactions(ServersWrapper serversWrapper, int expected) {
    Optional<Tuple<CheckAccountResponse, Long>> check_account =
        serversWrapper.checkAccount(new CheckAccountRequest(), false);

    assertTrue(check_account.isPresent());
    assertEquals(expected, check_account.get().first.getHistory().size());
  }

  private void verifyNumberOfPendingTransactions(ServersWrapper client, int expected) {
    Optional<Tuple<CheckAccountResponse, Long>> check_account =
        client.checkAccount(new CheckAccountRequest(), false);

    assertTrue(check_account.isPresent());
    assertEquals(expected, check_account.get().first.getPending().size());
  }

  private SendAmountRequest sendAmountRequest() {
    return new SendAmountRequest()
        .amount(1)
        .sourceKey(byzantineClient.securityHelper.key)
        .destKey(normalClient.securityHelper.key);
  }

  private static class ByzantineWrapper extends ServersWrapper {

    public ByzantineWrapper(String user, String pass, List<String> serversUrls) throws IOException, GeneralSecurityException, OperatorCreationException {
      super(user, pass, serversUrls);
    }

    public void sendAmount(SendAmountRequest normalBody, SendAmountRequest changedBody, int n) {
      forEachServer(server -> {
        Set<String> serversNoneByzantine = servers.keySet().stream().skip(n).collect(Collectors.toSet());
        if (serversNoneByzantine.contains(server.getApiClient().getBasePath())) {
          return server.sendAmount(normalBody);
        } else {
          return server.sendAmount(changedBody);
        }
      }).collect(Collectors.toList());
    }

    public SendAmountRequest getSendAmountBody(SendAmountRequest body) throws GeneralSecurityException {
      body.setHash(securityHelper.createHash(
          Optional.of(securityHelper.getLastHash().getValue()),
          Optional.empty(),
          securityHelper.key.getValue(),
          body.getDestKey().getValue(), body.getAmount(),
          ChainHelper.TransactionType.SEND_AMOUNT));

      body.sourceKey(securityHelper.key);

      String message = securityHelper.key.getValue()
          + body.getDestKey().getValue()
          + body.getAmount().toString()
          + body.getHash().getValue();

      securityHelper.signMessage(message, body::setSignature);
      return body;
    }

    public void setLastHash(String hash) {
      securityHelper.getLastHash().setValue(hash);
    }

    public String getLastHash() {
      return super.securityHelper.getLastHash().getValue();
    }

    public Signature signMessage(String message){
      return new Signature().value(super.securityHelper.cryptoAgent.generateSignature(message));
    }

  }
}