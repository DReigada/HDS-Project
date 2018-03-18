package com.tecnico.sec.hds.client;

import com.tecnico.sec.hds.client.Client;
import com.tecnico.sec.hds.client.commands.AbstractCommand;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Scanner;

public class ClientApp {
  public static void main(String[] args) {
    Scanner reader = new Scanner(System.in);  // Reading from System.in
    System.out.println("\nEnter Username: ");
    String username = reader.nextLine();

    try {
      Client client = new Client(username);

      while (true) {
        System.out.println("\nEnter a command: ");
        String[] commandLine = reader.nextLine().split(" ");

        String commandName = commandLine[0];
        String[] commandArgs = Arrays.copyOfRange(commandLine, 1, commandLine.length);

        AbstractCommand command = client.getCommands().get(commandName);

        if (command != null) {
          command.run(client, commandArgs);
        } else {
          System.err.println("Invalid command: " + commandLine[0]);
        }
      }

    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
      e.printStackTrace();
    }

  }
}