package com.tecnico.sec.hds.client;

import com.tecnico.sec.hds.client.commands.AbstractCommand;

import java.util.Arrays;
import java.util.Scanner;

public class ClientApp {
  public static void main(String[] args) {
    Scanner reader = new Scanner(System.in);  // Reading from System.in
    System.out.println("\nEnter Username: ");
    String username = reader.nextLine();
    System.out.println("\nEnter Password:");
    String password = reader.nextLine();
    Client client;

    try {
      System.setProperty("http.maxConnections", "100");

      client = new Client(username, password);

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
      return;
    }
      while (true) {
        System.out.println("\nEnter a command: ");
        String[] commandLine = reader.nextLine().split(" ");

        String commandName = commandLine[0];
        String[] commandArgs = Arrays.copyOfRange(commandLine, 1, commandLine.length);

        AbstractCommand command = client.getCommands().get(commandName);

        try {
          if (command != null) {
            command.run(client, commandArgs);
          } else {
            System.err.println("Invalid command: " + commandLine[0]);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

  }
}