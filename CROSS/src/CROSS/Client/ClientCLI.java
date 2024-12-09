package CROSS.Client;

import java.util.LinkedList;
import java.util.Scanner;

import CROSS.Enums.ClientActions;

public class ClientCLI extends Thread {
    
    @Override
    public void run(){

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Client CLI -> ");

            String command = scanner.nextLine().toLowerCase().trim();

            // Check if the command is valid.
            ClientActions action = null;
            try {
                action = ClientActionsUtils.actionFromString(command);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid command.");
                continue;
            }

            // Parse the arguments.
            LinkedList<String> args = null;
            try {
                args = ClientActionsUtils.parseCommandFromString(command, action);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid arguments.");
                continue;
            }

            // TODO: Prepare the JSON request to send.

            // TODO: Remove this.
            if ("1"==args.get(0)) break;

        } // End while.

        // Clean up.
        scanner.close();
        
    }

}
