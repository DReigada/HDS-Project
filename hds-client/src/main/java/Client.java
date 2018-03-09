import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.CheckAmountRequest;
import io.swagger.client.model.PubKey;
import io.swagger.client.model.RegisterRequest;
import io.swagger.client.model.SendAmountRequest;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
        //RegisterResponse response = new DefaultApi(client).register(request); NEED TO CORRECT
    }

    public void checkAmount() throws ApiException {
        CheckAmountRequest checkAmountRequest = new CheckAmountRequest().publicKey(key);
        new DefaultApi(client).checkAmount(checkAmountRequest);
    }
}
