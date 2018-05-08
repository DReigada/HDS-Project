package com.tecnico.sec.hds.client;

import com.tecnico.sec.hds.client.commands.util.SecurityHelper;
import com.tecnico.sec.hds.util.Tuple;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import io.swagger.client.ApiClient;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServersWrapperTests {
  static Client client;
  static ServersWrapper serversWrapper;

  @BeforeClass
  public static void populate() throws Exception{
    client = new Client("1r0wo", "1r0wo");
    serversWrapper = spy(new ServersWrapper("1r0wo","1r0wo"));
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
  public void auditTransactionInDifOrder() throws Exception {
    List<TransactionInformation> transactions = createChainTransactions(5);
    DefaultApi dummyDefaultApi = mock(DefaultApi.class);
    ApiClient dummyApiClient = mock(ApiClient.class);
    when(dummyApiClient.getBasePath()).thenReturn("");
    when(dummyDefaultApi.getApiClient()).thenReturn(dummyApiClient);

    //---------------- Create List With Response From Each Server -----------------------------
    List<Tuple<DefaultApi, AuditResponse>> forEachServerResponse = new ArrayList<>();
    AuditResponse auditResponse = new AuditResponse();
    transactions.forEach(auditResponse::addListItem);
    for(int i = 0 ; i<4 ; i++){
      forEachServerResponse.add(new Tuple<>(dummyDefaultApi, auditResponse));
    }

    //---------------- Shuffle Last Transaction ------------------------------------------------
    List<TransactionInformation> transactionsShuffled = new ArrayList<>(transactions);
    Collections.shuffle(transactionsShuffled);

    //---------------- Add Shuffle Transaction To List Of Server Responses ---------------------
    AuditResponse mockedResponse = new AuditResponse();
    transactionsShuffled.stream().forEach(mockedResponse::addListItem);
    forEachServerResponse.add(0,new Tuple<>(dummyDefaultApi, mockedResponse));

    //--------------- Mock Methods -------------------------------------------------------------
    Stream stream = forEachServerResponse.stream();
    SecurityHelper securityHelper = spy(new SecurityHelper("1r0wo","1r0wo"));
    serversWrapper.setSecurityHelper(securityHelper);
    doReturn(true).when(serversWrapper.securityHelper).verifySignature(any(),any(),any());
    doReturn(stream).when(serversWrapper).forEachServer(any());
    AuditRequest auditRequest = new AuditRequest().publicKey(client.server.securityHelper.key);
    Optional<AuditResponse> auditResponseCaseTest = serversWrapper.audit(auditRequest);
    assertTrue("Audit shouldn't return null after running quorum", auditResponseCaseTest.isPresent());
    assertEquals("Different list result", transactions, auditResponseCaseTest.get());
  }

  private List<TransactionInformation> createChainTransactions(int n){
    List<TransactionInformation> transactions = new ArrayList<>();
    String hash = "";
    for (int i = 0 ; i< n ; i++){
      String message = client.server.securityHelper.key.getValue()
          + client.server.securityHelper.key.getValue()
          + i
          + hash;
      TransactionInformation transactionInformation = new TransactionInformation();
      transactionInformation.setSourceKey(client.server.securityHelper.key.getValue());
      transactionInformation.setDestKey(client.server.securityHelper.key.getValue());
      transactionInformation.setAmount("" + i);
      transactionInformation.setPending(true);
      transactionInformation.setReceive(false);
      transactionInformation.setTransID(i);
      transactionInformation.setSendHash(new Hash().value(hash));
      transactionInformation.receiveHash(new Hash().value(""));

      String signature = client.server.securityHelper.cryptoAgent.generateSignature(message);
      transactionInformation.setSignature(new Signature().value(signature));

      hash = client.server.securityHelper.chainHelper.generateTransactionHash(
          Optional.of(hash),
          Optional.of(""),
          client.server.securityHelper.key.getValue(),
          client.server.securityHelper.key.getValue(),
          i,
          ChainHelper.TransactionType.SEND_AMOUNT,
          signature);
    transactions.add(transactionInformation);
    }
    Collections.reverse(transactions);
    return transactions;
  }
}
