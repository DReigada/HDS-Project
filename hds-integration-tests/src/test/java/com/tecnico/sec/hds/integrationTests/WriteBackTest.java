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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class WriteBackTest {
  private static ServerHelper serverHelper;

  @BeforeClass
  public static void start() {
    serverHelper = new ServerHelper();
    serverHelper.writeConfig(4);
    serverHelper.startServers(0, 7, ServerTypeWrapper.ServerType.NORMAL);
  }

  @AfterClass
  public static void afterClass() {
    serverHelper.stopServers();
    serverHelper.deleteConfig();
    ServerTypeWrapper.cleanServers();
    new File("user1KeyStore.jce").delete();
  }


  @Test
  public void writeBackOneServer() throws GeneralSecurityException, OperatorCreationException, IOException, ApiException, InterruptedException {
    ServersWrapperHelper server = new ServersWrapperHelper("user1", "pass1");
    server.register();

    ServerTypeWrapper.changeServerType(0, ServerTypeWrapper.ServerType.IGNORE);

    SendAmountRequest request =
        new SendAmountRequest()
            .amount(10)
            .destKey(server.securityHelper.key);

    Optional<String> response1 = server.sendAmount(request);
    assertTrue(response1.isPresent());

    ServerTypeWrapper.changeServerType(0, ServerTypeWrapper.ServerType.NORMAL);

    CheckAccountResponse responseBeforeWriteBack = server.checkAccountToOneServer(0);

    assertEquals(1, responseBeforeWriteBack.getHistory().size());

    server.setWriteBackSync(true);

    Optional<Tuple<CheckAccountResponse, Long>> response2 =
        server.checkAccount(new CheckAccountRequest(), true);

    assertTrue(response2.isPresent());
    assertEquals(990, (long) response2.get().second);

    CheckAccountResponse responseAfterWriteBack = server.checkAccountToOneServer(0);
    assertEquals(2, responseAfterWriteBack.getHistory().size());

    new File("user1KeyStore.jce").delete();
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
