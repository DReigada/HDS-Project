import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyStoreException;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    try {
      Client client = new Client();
      Class<?> c = Class.forName("Client");

      Scanner reader;
      String command;
      Method method;

      while (true) {
        reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("\nEnter a command: ");
        command = reader.nextLine();

        method = c.getDeclaredMethod(command);
        method.invoke(client);
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (KeyStoreException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}