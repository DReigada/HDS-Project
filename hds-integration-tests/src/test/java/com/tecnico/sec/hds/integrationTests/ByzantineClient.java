package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.util.TransactionGetter;
import com.tecnico.sec.hds.util.Tuple;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import domain.Transaction;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ByzantineClient {
  private static bla byzantineClient;
  private static ServersWrapper normalClient;
  private static ServerHelper serverHelper;

  @AfterClass
  public static void clearEverything() {
    serverHelper.deleteConfig();
  }

  @Before
  public void start() throws GeneralSecurityException, IOException, OperatorCreationException {
    System.setProperty("hds.coin.crypto.useLocalhost", "false");
    serverHelper = new ServerHelper();
    serverHelper.writeConfig(4);
    List<String> serversUrls = serverHelper.startServers(0, 4, ServerTypeWrapper.ServerType.NORMAL);

    byzantineClient = new bla("user1", "pass1", serversUrls);
    normalClient = new ServersWrapper("user2", "pass2", serversUrls);
    normalClient.register();
    byzantineClient.register();
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

  @Test
  public void transactionDiffTo1Server() throws Exception {
    SendAmountRequest body = sendAmountRequest();
    SendAmountRequest normalBody = byzantineClient.getSendAmountBody(body);
    SendAmountRequest changedBody = byzantineClient.getSendAmountBody(body.amount(91));
    byzantineClient.sendAmount(normalBody, changedBody, 1);
    boolean transactionResponses = compareTransactions(byzantineClient.checkAccountFromAllServers(),
        normalBody,
        changedBody);
    assertFalse(transactionResponses);
  }

  @Test
  public void transactionDiffTo2Server() throws Exception {
    SendAmountRequest normalBody = byzantineClient.getSendAmountBody(sendAmountRequest());
    SendAmountRequest changedBody = byzantineClient.getSendAmountBody(sendAmountRequest().amount(91));
    byzantineClient.sendAmount(normalBody, changedBody, 2);

    Optional<Tuple<CheckAccountResponse, Long>> check_account =
        byzantineClient.checkAccount(new CheckAccountRequest(), true);

    assertTrue(check_account.isPresent());
    assertEquals(1, check_account.get().first.getHistory().size());
  }

  @Test
  public void wrongSignTransactionDiffTo2ServerWithDiff() throws Exception {
    SendAmountRequest body = sendAmountRequest();
    SendAmountRequest normalBody = byzantineClient.getSendAmountBody(body);
    SendAmountRequest changedBody = byzantineClient.getSendAmountBody(body.amount(91));
    changedBody.signature(new Signature().value(Base64.getEncoder().encodeToString("Im Byzantine!".getBytes())));
    byzantineClient.sendAmount(normalBody, changedBody, 2);
    boolean transactionResponses = compareTransactions(byzantineClient.checkAccountFromAllServers(),
        normalBody,
        changedBody);
    assertTrue(transactionResponses);
  }

  @Test
  public void allWrongSignatures() throws Exception {
    SendAmountRequest body = sendAmountRequest();
    SendAmountRequest normalBody = byzantineClient.getSendAmountBody(body);
    SendAmountRequest changedBody = byzantineClient.getSendAmountBody(body.amount(91));
    changedBody.signature(new Signature().value(Base64.getEncoder().encodeToString("Im Byzantine!".getBytes())));
    normalBody.signature(new Signature().value(Base64.getEncoder().encodeToString("Im Byzantine!!!".getBytes())));
    byzantineClient.sendAmount(normalBody, changedBody, 2);
    boolean transactionResponses = compareTransactions(byzantineClient.checkAccountFromAllServers(),
        normalBody,
        changedBody);
    assertTrue(transactionResponses);
  }


  private boolean compareTransactions(List<Transaction> transactionResponses,
                                      SendAmountRequest normalBody,
                                      SendAmountRequest changedBody) {

    return transactionResponses.stream()
        .anyMatch(t ->
            !(t.hash.equals(normalBody.getHash().getValue()) &&
                t.amount == 1 ||
                t.hash.equals(changedBody.getHash().getValue()) &&
                    t.amount == 91));
  }

  private SendAmountRequest sendAmountRequest() {
    return new SendAmountRequest()
        .amount(1)
        .sourceKey(byzantineClient.securityHelper.key)
        .destKey(normalClient.securityHelper.key);
  }


  private static class bla extends ServersWrapper {

    public bla(String user, String pass, List<String> serversUrls) throws IOException, GeneralSecurityException, OperatorCreationException {
      super(user, pass, serversUrls);
    }

    public List<Transaction> checkAccountFromAllServers() throws ApiException {
      CheckAccountRequest checkAccount = new CheckAccountRequest().publicKey(securityHelper.key);
      List<Transaction> checkAccountResponses = new ArrayList<>();
      for (DefaultApi defaultApi : servers.values()) {
        List<TransactionInformation> response = defaultApi.checkAccount(checkAccount).getHistory();
        Transaction transaction = TransactionGetter.InformationToTransaction(response).get(0);
        checkAccountResponses.add(transaction);
      }
      return checkAccountResponses;
    }

    public String sendAmount(SendAmountRequest normalBody, SendAmountRequest changedBody, int n) {
      forEachServer(server -> {
        Set<String> serversNoneByzantine = servers.keySet().stream().skip(n).collect(Collectors.toSet());
        if (serversNoneByzantine.contains(server.getApiClient().getBasePath())) {
          return server.sendAmount(normalBody);
        } else {
          return server.sendAmount(changedBody);
        }
      })
          .collect(Collectors.toList());

      return "asdf";
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

  }
}