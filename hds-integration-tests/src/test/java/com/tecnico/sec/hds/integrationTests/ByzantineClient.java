package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.util.TransactionGetter;
import com.tecnico.sec.hds.util.Tuple;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import domain.Transaction;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ByzantineClient {
  private static bla byzantineClient;
  private static ServersWrapper normalClient;
  private static ServerHelper serverHelper;

  @Before
  public void start() throws GeneralSecurityException, IOException, OperatorCreationException {
    System.setProperty("hds.coin.crypto.useLocalhost", "false");
    serverHelper = new ServerHelper();
    List<String> serversUrls = serverHelper.startServers(4);

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
    /*new File("bankDESKTOP-6ON3D5U_8180KeyStore.jce").delete();
    new File("bankDESKTOP-6ON3D5U_8181KeyStore.jce").delete();
    new File("bankDESKTOP-6ON3D5U_8182KeyStore.jce").delete();
    new File("bankDESKTOP-6ON3D5U_8183KeyStore.jce").delete();*/
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
    SendAmountRequest body = sendAmountRequest();
    SendAmountRequest normalBody = byzantineClient.getSendAmountBody(body);
    SendAmountRequest changedBody = byzantineClient.getSendAmountBody(body.amount(91));
    byzantineClient.sendAmount(normalBody, changedBody, 2);
    boolean transactionResponses = compareTransactions(byzantineClient.checkAccountFromAllServers(),
        normalBody,
        changedBody);
    assertFalse(transactionResponses);
  }

  @Test
  public void wrongSignTransactionDiffTo2ServerWithDiff() throws Exception {
    SendAmountRequest body = sendAmountRequest();
    SendAmountRequest normalBody = byzantineClient.getSendAmountBody(body);
    SendAmountRequest changedBody = byzantineClient.getSendAmountBody(body.amount(91));
    changedBody.setSignature(new Signature().value("Im Byzantine!"));
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
    changedBody.setSignature(new Signature().value("Im Byzantine!"));
    normalBody.setSignature(new Signature().value("Im Byzantine!!!"));
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

    public String sendAmount(SendAmountRequest normalBody, SendAmountRequest changedBody, int n) throws GeneralSecurityException {
      Tuple<DefaultApi, SendAmountResponse> response = forEachServer(server -> {
        List serversNoneByzantine = new ArrayList<>(servers.keySet().stream().skip(n).collect(Collectors.toList()));
        if (serversNoneByzantine.contains(server.getApiClient().getBasePath())) {
          return server.sendAmount(normalBody);
        } else {
          System.out.println("ByzEntrei");
          return server.sendAmount(changedBody);
        }
      }).collect(Collectors.toList())
          .get(0);

      SendAmountResponse sendAmountResponse = response.second;
      String message = sendAmountResponse.getNewHash().getValue() + sendAmountResponse.getMessage();

      if (securityHelper.verifyBankSignature(message, sendAmountResponse.getSignature().getValue(), response.first.getApiClient().getBasePath())
          && sendAmountResponse.isSuccess()) {

        if (sendAmountResponse.isSuccess()) {
          securityHelper.setLastHash(sendAmountResponse.getNewHash());
        }


        return sendAmountResponse.getMessage();
      }

      return "Unexpected error from server. \n Try Again Later.";
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