import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.CheckAmountRequest;
import io.swagger.client.model.PubKey;
import io.swagger.client.model.RegisterRequest;
import io.swagger.client.model.SendAmountRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Created by jp_s on 3/6/2018.
 */
public class Client {
    String id;
    ApiClient client;
    DefaultApi defaultApi;
    KeyStore keyStore;
    PubKey key = new PubKey().value("asdasdasd"); // TO BE CHANGED

    public Client(String id) throws KeyStoreException{
        this.id = id;
        client = new ApiClient().setBasePath("http://localhost:8080");
        defaultApi = new DefaultApi(client);
        keyStore = KeyStore.getInstance("JCEKS");
    }

    public void Register() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, ApiException {
        keyStore.load(null, id.toCharArray()); //Change id if necessary to a password


        //-------------------- Generate Key Pair -----------------------------------------------------
        KeyPairGenerator keygen = KeyPairGenerator .getInstance("EC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); // Change After
        keygen.initialize(112,random); // Change After
        KeyPair mykey = keygen.generateKeyPair();

        System.out.println("MyPrivKey:" + mykey.getPrivate().toString());

        //-------------------- Store Private Key-----------------------------------------------------
        /*KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(mykey.getPrivate(),new Certificate[]{cert});
        String alias = "User:" + id; //Change After;
        KeyStore.PasswordProtection password = new KeyStore.PasswordProtection(id.toCharArray());
        keyStore.setEntry(alias,privateKeyEntry,password);*/
        //------------------- Saving And Closing ----------------------------------------------------

        FileOutputStream fileOutputStream = new FileOutputStream("keystorefile.jce");
        keyStore.store(fileOutputStream, id.toCharArray());
        fileOutputStream.close();

        //------------------- Public Key to file -----------------------------------------------------

        byte[] pubKeyBytes = mykey.getPublic().getEncoded();
        File file = new File("PublicKey.txt");
        fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write( pubKeyBytes );
        fileOutputStream.close();

        //--------------------------------------------------------------------------------------

        RegisterRequest request = new RegisterRequest().publicKey(key);
        defaultApi.register(request);

        System.out.print("Invocado e criado");
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

}
