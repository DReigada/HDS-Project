package com.tecnico.sec.hds.client;

import com.tecnico.sec.hds.client.commands.RegisterCommand;
import com.tecnico.sec.hds.client.commands.SendAmountCommand;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AuditRequest;
import io.swagger.client.model.AuditResponse;
import io.swagger.client.model.TransactionInformation;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServersWrapperTests {
  static ServersWrapper serversWrapper;
  static Client client;

  @BeforeClass
  public static void populate() throws Exception{
    serversWrapper = mock(ServersWrapper.class);
    RegisterCommand registerCommand = new RegisterCommand();
    client = new Client("1r0wo", "1r0wo");
    registerCommand.doRun(client, null);
  }

  /*@AfterClass
  public static void clean(){
    List<File> filesToDelete  = new ArrayList<>();
    filesToDelete.add(new File("HDSDB8080.mv.db"));
    filesToDelete.add(new File("HDSDB8081.mv.db"));
    filesToDelete.add(new File("HDSDB8082.mv.db"));
    filesToDelete.add(new File("HDSDB8083.mv.db"));
    filesToDelete.add(new File("bank192_168_56_1_8080KeyStore.jce"));
    filesToDelete.add(new File("bank192_168_56_1_8081KeyStore.jce"));
    filesToDelete.add(new File("bank192_168_56_1_8082KeyStore.jce"));
    filesToDelete.add(new File("bank192_168_56_1_8083KeyStore.jce"));
    filesToDelete.add(new File("1r0woKeyStore.jce"));
    filesToDelete.stream().parallel().forEach(File::delete);
  }*/

  @Test
  public void auditTransactionInDifOrder(){
    SendAmountCommand sendAmountCommand = new SendAmountCommand();
    String[] args = {client.server.securityHelper.key.getValue(), "100" };
    for (int i = 0; i < 5 ; i++) {sendAmountCommand.doRun(client,args);}
    AuditRequest auditRequest = new AuditRequest();
    auditRequest.setPublicKey(client.server.securityHelper.key);
    Optional<AuditResponse> auditResponse = client.server.audit(auditRequest);
    List<TransactionInformation> transactions = new ArrayList<>();
    if (auditResponse.isPresent()) {
      transactions = client.server.audit(auditRequest).get().getList();
    }
    else {
      fail();
    }

    List<Tuple<DefaultApi, AuditResponse>> forEachServerResponse = new ArrayList<>();
    int i = 0;
    while(i < client.server.servers.size() - 1){
      forEachServerResponse.add(new Tuple<>(client.server.servers.get(i), auditResponse.get()));
      i++;
    }
    List<TransactionInformation> transactionsShuffled = new ArrayList<>(transactions);
    Collections.shuffle(transactionsShuffled);
    AuditResponse mockedResponse = new AuditResponse();
    transactionsShuffled.stream().forEach(mockedResponse::addListItem);

    System.out.println("i:" + i + " list size" + client.server.servers.size());

    forEachServerResponse.add(new Tuple<>(client.server.servers.get(i), mockedResponse));
    Stream stream = forEachServerResponse.stream();
    when(serversWrapper.forEachServer(any())).thenReturn(stream);

    Optional<AuditResponse> auditResponseCaseTest = serversWrapper.audit(auditRequest);
    if (auditResponseCaseTest.isPresent()) {
      assertEquals(transactions,auditResponseCaseTest.get());
    }
    else {
      fail();
    }
  }
}
