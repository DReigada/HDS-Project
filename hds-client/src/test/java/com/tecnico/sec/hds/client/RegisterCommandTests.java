package com.tecnico.sec.hds.client;

import com.tecnico.sec.hds.client.commands.RegisterCommand;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.RegisterResponse;
import io.swagger.client.model.Signature;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RegisterCommandTests {

  private static CryptoAgent cryptoAgent;
  private PrintStream ps;
  private PrintStream old;
  private ByteArrayOutputStream byteArrayOutputStream;

  @Mock
  DefaultApi server = mock(DefaultApi.class);

  @InjectMocks
  Client client = new Client("user","123");

  public RegisterCommandTests() throws Exception {
  }

  @BeforeClass
  public static void populate() throws Exception {
    cryptoAgent = new CryptoAgent("bank","1234");
  }

  @Test
  public void registerUserWithRightSignature() throws Exception {
    RegisterCommand registerCommand = new RegisterCommand();
    RegisterResponse registerResponse = new RegisterResponse();
    String message = "success";
    Signature signature = new Signature().value(cryptoAgent.generateSignature(message));
    registerResponse.setMessage(message);
    registerResponse.setSignature(signature);
    System.out.println(signature);
    doReturn(registerResponse).when(server).register(any());
    verify(server, Mockito.times(1)).register(any());

    /*doAnswer(invocationOnMock -> {
      RegisterResponse registerResponse = new RegisterResponse();
      String message = "success";
      Signature signature = new Signature().value(cryptoAgent.generateSignature(message));
      registerResponse.setMessage(message);
      registerResponse.setSignature(signature);
      return registerResponse;}).when(defaultApi).register(any(RegisterRequest.class));*/

    registerCommand.doRun(client,null);
    assertEquals("success", byteArrayOutputStream.toString());
  }

  /*@Test
  public void registerWithWrongSignature() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, ApiException, SignatureException, InvalidKeyException {
    RegisterCommand registerCommand = new RegisterCommand();
    doAnswer(invocationOnMock -> {
          String message = "Trying to send message with wrong signature";
          Signature signature = new Signature().value(cryptoAgent.generateSignature(message) + "alterada");
          RegisterResponse registerResponse = new RegisterResponse();
          registerResponse.setMessage(message);
          registerResponse.setSignature(signature);
          return registerResponse;}).when(defaultApi).register(any(RegisterRequest.class));

    registerCommand.doRun(client,null);
    assertEquals("Enexpected error from server. \n Try Again Later.", byteArrayOutputStream.toString());
  }*/

  @Before
  public void changePrintStream(){
    byteArrayOutputStream = new ByteArrayOutputStream();
    ps = new PrintStream(byteArrayOutputStream);
    old = System.out;
    System.setOut(ps);
  }

  @After
  public void setOldPrintStream() throws IOException {
    byteArrayOutputStream.close();
    System.setOut(old);
  }
}
