package com.tecnico.sec.hds.integrationTests;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.integrationTests.util.ServerHelper;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.ApiException;
import io.swagger.client.model.CheckAccountRequest;
import io.swagger.client.model.CheckAccountResponse;
import io.swagger.client.model.SendAmountRequest;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class WriteBackTest {
  private static ServerHelper serverHelper;

  @BeforeClass
  public static void start() {
    serverHelper = new ServerHelper();
    serverHelper.writeConfig(7);
    serverHelper.startServers(0, 7, ServerTypeWrapper.ServerType.NORMAL);
  }

  @AfterClass
  public static void afterClass() {
    serverHelper.stopServers();
    serverHelper.deleteConfig();
    ServerTypeWrapper.cleanServers();
    new File("user1KeyStore.jce").delete();
  }

  @After
  public void after() {
    new File("user1KeyStore.jce").delete();
  }


  @Test
  public void writeBackOneTransactionOneServer() throws GeneralSecurityException, OperatorCreationException, IOException, ApiException {
    writeBackOnServers(1, new int[]{0});
  }

  @Test
  public void writeBackOneTransactionMultipleServers() throws GeneralSecurityException, OperatorCreationException, IOException, ApiException {
    writeBackOnServers(1, new int[]{0, 1});
  }

  @Test
  public void writeBackMultipleTransactionOneServer() throws GeneralSecurityException, OperatorCreationException, IOException, ApiException {
    writeBackOnServers(4, new int[]{0});
  }

  @Test
  public void writeBackMultipleTransactionMultipleServers() throws GeneralSecurityException, OperatorCreationException, IOException, ApiException {
    writeBackOnServers(4, new int[]{0, 1});
  }

  private void writeBackOnServers(int numberOfTransactions, int[] ignoringServers) throws GeneralSecurityException, OperatorCreationException, IOException, ApiException {
    ServersWrapperHelper server = new ServersWrapperHelper("user1", "pass1");
    server.register();

    for (int i : ignoringServers) {
      ServerTypeWrapper.changeServerType(i, ServerTypeWrapper.ServerType.IGNORE);
    }

    int accountAmount = 1000;

    for (int i = 1; i <= numberOfTransactions; i++) {
      accountAmount -= i;
      SendAmountRequest request =
          new SendAmountRequest()
              .amount(i)
              .destKey(server.securityHelper.key);

      Optional<String> response1 = server.sendAmount(request);
      assertTrue(response1.isPresent());
    }

    for (int i : ignoringServers) {
      ServerTypeWrapper.changeServerType(i, ServerTypeWrapper.ServerType.NORMAL);
      CheckAccountResponse responseBeforeWriteBack = server.checkAccountToOneServer(i);
      assertEquals(1, responseBeforeWriteBack.getHistory().size());
    }

    server.setWriteBackSync(true);

    Optional<Tuple<CheckAccountResponse, Long>> response2 =
        server.checkAccount(new CheckAccountRequest(), true);

    server.setWriteBackSync(false);

    assertTrue(response2.isPresent());
    assertEquals(accountAmount, (long) response2.get().second);

    for (int i : ignoringServers) {
      CheckAccountResponse responseAfterWriteBack = server.checkAccountToOneServer(i);
      assertEquals(numberOfTransactions + 1, responseAfterWriteBack.getHistory().size());
    }

  }

  class ServersWrapperHelper extends ServersWrapper {

    public ServersWrapperHelper(String user, String pass) throws IOException, OperatorCreationException, GeneralSecurityException {
      super(user, pass);
    }

    public CheckAccountResponse checkAccountToOneServer(int server) throws ApiException {
      return servers.get("http://localhost:818" + server)
          .checkAccount(new CheckAccountRequest().publicKey(securityHelper.key));
    }
  }

}
