package CROSS.Client;

import java.util.LinkedList;
import java.util.Scanner;

/**
 * This class is responsible for handling the client CLI.
 * It's a thread.
 * @version 1.0
 * @see Client
 * @see Thread
 * @see ClientActionsUtils
 */
public class ClientCLIThread extends Thread {

    private Client client = null;
    
    /**
     * Constructor of the class.
     * @param client The client object that will be used with the CLI.
     * @throws NullPointerException If the client object is null.
     * @throws RuntimeException If the client's socket is null.
     */
    public ClientCLIThread(Client client) throws NullPointerException, RuntimeException {
        if (client == null)
            throw new NullPointerException("The client object can't be null.");

        if (client.getSocket() == null)
            throw new RuntimeException("The client's socket in the CLI cannot be null.");

        this.client = client;
    }
    
    @Override
    public void run(){

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Client CLI -> ");

            String command = scanner.nextLine().toLowerCase().trim();

            if (command.equals("exit") || command.equals("quit") || command.equals("q")) {
                System.out.println("Exiting the client CLI.");
                break;
            }

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

            String jsonToSend = ClientActionsUtils.getJSONRequest(client, action, args);
            if (jsonToSend == null) {
                // This is not a critical error, just a invalid command.
                System.out.println("Error processing your request.");
                continue;
            }

            try {
                this.client.sendJSONToServer(jsonToSend);
            } catch (RuntimeException ex) {
                // This is a critical error.
                System.err.println("Error sending the request to the server.");
                // TODO: Error handling.
                break;
            }

        } // End while.

        // TODO: Clean up.
        scanner.close();
        
    }

}
