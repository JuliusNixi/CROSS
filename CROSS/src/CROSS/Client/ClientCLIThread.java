package CROSS.Client;

import java.util.LinkedList;
import java.util.Scanner;

/**
 * 
 * This class is responsible for handling the client CLI.
 * It's a thread. Can be started with the start() method from the Client class.
 * 
 * @version 1.0
 * @see Client
 * @see Thread
 * @see ClientActionsUtils
 * 
 */
public class ClientCLIThread extends Thread {

    // The client object that will be used with the CLI.
    private Client client = null;
    
    /**
     * 
     * Constructor of the class.
     * 
     * @param client The client object that will be used with the CLI.
     * 
     * @throws NullPointerException If the client object is null.
     * 
     */
    public ClientCLIThread(Client client) throws NullPointerException {
        
        // Null check.
        if (client == null)
            throw new NullPointerException("The client object cannot be null.");

        this.client = client;

    }
    
    // Main CLI loop.
    @Override
    public void run(){

        Scanner scanner = new Scanner(System.in);

        // Main CLI loop.
        while (true) {

            Boolean exit = false;

            System.out.print("Client CLI -> ");

            String command = scanner.nextLine().toLowerCase().trim();

            // Check if the command is valid.
            ClientActionsUtils.ClientActions action = null;
            try {
                action = ClientActionsUtils.actionFromString(command);
            } catch (IllegalArgumentException | NullPointerException ex) {
                // This is not a critical error, just an invalid command.
                System.out.println("Invalid string command.");
                continue;
            }

            // Parse the arguments.
            LinkedList<String> args = null;
            try {
                args = ClientActionsUtils.parseCommandFromString(command, action);
                ClientActionsUtils.parseArgs(args, action);
            } catch (IllegalArgumentException | NullPointerException ex) {
                // This is not a critical error, just a invalid command.
                System.out.println("Invalid arguments.");
                continue;
            }

            String jsonToSend = null;
            jsonToSend = ClientActionsUtils.getJSONRequest(client, action, args);
            if (jsonToSend == null) {
                // This is not a critical error, just a invalid command.
                System.out.println("Error processing your request, please check it and try again.");
                continue;
            }

            System.out.println("DEBUG: Request to send to the server: " + jsonToSend);

            try {
                if (action != ClientActionsUtils.ClientActions.EXIT) {
                    this.client.sendJSONToServer(jsonToSend);
                    System.out.println("Request sent to the server.");
                }
            } catch (Exception ex) {
                // This is a critical error.
                System.err.println("Error sending the request to the server. Your request will be ignored.");
                try {
                    this.client.disconnectClient();
                } catch (Exception ex2) {
                    System.err.println("Error disconnecting the client: " + ex2.getMessage());
                }
                exit = true;
            }
            if (exit)
                break;

            if (action == ClientActionsUtils.ClientActions.EXIT) {
                // If we arrive here, the exit request was NOT SENT to the server and the client wants to exit.
                try {
                    this.client.disconnectClient();
                    System.out.println("Request sent to the server.");
                } catch (Exception ex) {
                    System.err.println("Error disconnecting the client: " + ex.getMessage());
                }
                exit = true;
            }
            if (exit)
                break;

        } // End while.

        // Exiting...
        System.out.println("Exiting the client CLI.");
        scanner.close();

        System.exit(-1);
        
    }

}
