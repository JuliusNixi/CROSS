package CROSS.Client;

import java.util.LinkedList;
import java.util.Scanner;

import CROSS.ClientActionsUtils;
import CROSS.Enums.ClientActions;

public class Client {
    
    // Command Line Interface.
    public static void CLI(){
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Client CLI -> ");

            String command = scanner.nextLine().toLowerCase().trim();

            // Check if the command is valid.
            ClientActions action = null;
            try {
                action = ClientActionsUtils.actionFromString(command);
            } catch (IllegalArgumentException e) {
                // TODO: ERROR.
            }

            // Parse the arguments.
            LinkedList<String> args = null;
            try {
                args = ClientActionsUtils.parseCommandFromString(command, action);
            } catch (IllegalArgumentException e) {
                // TODO: ERROR.
            }

            // Prepare the JSON request.

            // TODO: Remove this.
            if ("1"==args.get(0)) break;

        }

        // Clean up.
        scanner.close();

    }

}
