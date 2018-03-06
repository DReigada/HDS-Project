import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.PublicKey;
import io.swagger.client.model.RegisterRequest;
import io.swagger.client.model.SendAmountRequest;

public class Main {
  public static void main(String[] args) {
    ApiClient client = new ApiClient().setBasePath("http://localhost:8080");

    try {
      PublicKey key = new PublicKey().value("asdfsadfsaljdk");
      RegisterRequest request = new RegisterRequest().publicKey(key);

      System.out.println(request);

      new DefaultApi(client).register(request);
      SendAmountRequest sendAmount = new SendAmountRequest().destKey(key).sourceKey(key).amount(1);
      new DefaultApi(client).sendAmount(sendAmount);
    } catch (ApiException e) {
      System.err.println("Request failed:");
      System.err.println(e.getMessage());
    }

    System.out.println("I'm a client");
  }
}