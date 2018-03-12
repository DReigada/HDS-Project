import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by jp_s on 3/6/2018.
 */


public class Client {
    ApiClient client;
    DefaultApi defaultApi;
    CryptoAgent cryptoAgent;
    PubKey key;

    public Client(String username) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        client = new ApiClient().setBasePath("http://localhost:8080");
        cryptoAgent = new CryptoAgent(username);
        defaultApi = new DefaultApi(client);
    }

    public void register() throws ApiException {
        key =  new PubKey().value(cryptoAgent.getPublicKey());
        RegisterRequest request = new RegisterRequest().publicKey(key);
        defaultApi.register(request);
    }

    public void sendAmount() throws ApiException {
        SendAmountRequest sendAmount = new SendAmountRequest().destKey(key).sourceKey(key).amount(1);
        defaultApi.sendAmount(sendAmount);
        //RegisterResponse response = new DefaultApi(client).sendAmount(sendAmount); NEED TO CORRECT
    }

    public void checkAmount() throws ApiException {
        CheckAmountRequest checkAmountRequest = new CheckAmountRequest().publicKey(key);
        new DefaultApi(client).checkAmount(checkAmountRequest);
    }

    public void receiveAmount() throws ApiException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String message = "";
        ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest();
        receiveAmountRequest.publicKey(key);
        Signature signature = new Signature().value(cryptoAgent.generateSignature(message));
        receiveAmountRequest.signature(signature);
        defaultApi.receiveAmount(receiveAmountRequest);
    }

    public void audit() throws ApiException {
        AuditRequest auditRequest = new AuditRequest();
        auditRequest.publicKey(key);
        defaultApi.audit(auditRequest);
    }
}
