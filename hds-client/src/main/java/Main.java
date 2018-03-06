import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;

import java.util.stream.Collectors;

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
      RegisterResponse response = new DefaultApi(client).register(request);

      CheckAmountRequest checkAmountRequest = new CheckAmountRequest().publicKey(key);
      new DefaultApi(client).checkAmount(checkAmountRequest);
      response = new DefaultApi(client).register(request);

      System.out.println("Response:");
      System.out.println(response.getBla().stream().collect(Collectors.joining(" ")));
    } catch (ApiException e) {
      System.err.println("Request failed:");
      System.err.println(e.getMessage());
    }

    System.out.println("I'm a client");
  }
}