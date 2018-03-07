import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.CheckAmountRequest;
import io.swagger.client.model.PubKey;
import io.swagger.client.model.RegisterRequest;
import io.swagger.client.model.SendAmountRequest;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Created by jp_s on 3/6/2018.
 */
public class Client {
    ApiClient client;
    DefaultApi defaultApi;
    PublicKey publicKey;
    PrivateKey privateKey;
    PubKey key;

    public Client() throws KeyStoreException{
        client = new ApiClient().setBasePath("http://localhost:8080");
        defaultApi = new DefaultApi(client);
    }

    public void Register() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, ApiException {
        GenerateKey();
        key =  new PubKey().value(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        SaveKeys();
        RegisterRequest request = new RegisterRequest().publicKey(key);
        defaultApi.register(request);
    }

    public void SendAmount() throws ApiException {
        SendAmountRequest sendAmount = new SendAmountRequest().destKey(key).sourceKey(key).amount(1);
        defaultApi.sendAmount(sendAmount);
        //RegisterResponse response = new DefaultApi(client).register(request); NEED TO CORRECT
    }

    public void CheckAmount() throws ApiException {
        CheckAmountRequest checkAmountRequest = new CheckAmountRequest().publicKey(key);
        new DefaultApi(client).checkAmount(checkAmountRequest);
    }

    private void GenerateKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keygen = KeyPairGenerator .getInstance("EC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); // Change After
        keygen.initialize(112,random); // Change After
        KeyPair key = keygen.generateKeyPair();
        publicKey = key.getPublic();
        privateKey = key.getPrivate();
    }

    private void SaveKeys() throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter("PublicKey.txt"));
        out.write(key.getValue());
        out.close();

        out = new BufferedWriter(new FileWriter("PrivateKey.txt"));
        String priKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        out.write(priKey);
        out.close();
    }

    public void LoadKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        BufferedReader out = new BufferedReader(new FileReader("PublicKey.txt"));
        String key = out.readLine();
        KeyFactory  keyFactory = KeyFactory.getInstance("EC");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        publicKey = keyFactory.generatePublic(keySpec);
        out.close();

        out = new BufferedReader(new FileReader("PrivateKey.txt"));
        key = out.readLine();
        keyFactory = KeyFactory.getInstance("EC");
        keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        out.close();
    }

}
