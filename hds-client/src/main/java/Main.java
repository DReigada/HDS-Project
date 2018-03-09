import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    try {
      Scanner reader;
      String command;
      Method method;

      reader = new Scanner(System.in);  // Reading from System.in
      System.out.println("\nEnter Username: ");
      String username = reader.nextLine();
      Client client = new Client(username);
      Class<?> c = Class.forName("Client");

      while (true) {
        System.out.println("\nEnter a command: ");
        command = reader.nextLine();

        method = c.getDeclaredMethod(command);
        method.invoke(client);
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
    }
  }
}