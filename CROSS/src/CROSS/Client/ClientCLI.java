package CROSS.Client;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import CROSS.Enums.ClientActions;
import CROSS.TMP.JSONInterface;

// This thread will handle the client CLI.
public class ClientCLI extends Thread {

    private Client client = null;
    public ClientCLI(Client client) {
        this.client = client;
    }
    
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
                // This is not a critical error, just a invalid command.
                System.out.println("Invalid string command.");
                continue;
            }

            // Parse the arguments.
            LinkedList<String> args = null;
            try {
                args = ClientActionsUtils.parseCommandFromString(command, action);
                ClientActionsUtils.parseArgs(args, action);
            } catch (IllegalArgumentException e) {
                // This is not a critical error, just a invalid command.
                System.out.println("Invalid arguments.");
                continue;
            }

            // TODO: Prepare the JSON request to send.
            String jsonToSend = "";
            switch (action) {
                case REGISTER:
                    jsonToSend = JSONInterface.userRegister(args.get(0), args.get(1));
                    break;
                case LOGIN:

                    break;
                case UPDATE_CREDENTIALS:

                    break;
                case LOGOUT:

                    break;
                case INSERT_LIMIT_ORDER:

                    break;
                case INSERT_MARKET_ORDER:

                    break;
                case INSERT_STOP_ORDER:

                    break;
                case CANCEL_ORDER:  

                    break;
                case GET_PRICE_HISTORY:

                    break;
                default:
                    throw new IllegalArgumentException("Invalid action.");
            }

            try {
                this.client.sendJSON(jsonToSend);
            } catch (NullPointerException | IOException e) {
                // TODO Auto-generated catch block
            }

            // TODO: Remove this.
            if ("1"==args.get(0)) break;

        } // End while.

        // Clean up.
        scanner.close();
        
    }

}
