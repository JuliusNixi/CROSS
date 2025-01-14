package CROSS.Client;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * This class is responsible for handling the client CLI.
 * It's a thread. Can be started with the start() method from the Client class.
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
     */
    public ClientCLIThread(Client client) throws NullPointerException {
        // Null check.
        if (client == null)
            throw new NullPointerException("The client object can't be null.");

        this.client = client;

    }
    
    @Override
    public void run(){

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Client CLI -> ");

            String command = scanner.nextLine().toLowerCase().trim();

            // Also shutdown the client (not only the CLI).
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

            String jsonToSend = null;
            synchronized (this.client) {
                jsonToSend = ClientActionsUtils.getJSONRequest(client, action, args);
            }
            if (jsonToSend == null) {
                // This is not a critical error, just a invalid command.
                System.out.println("Error processing your request, please check it and try again.");
                continue;
            }

            try {
                this.client.sendJSONToServer(jsonToSend);
            } catch (Exception ex) {
                // This is a critical error.
                System.err.println("Error sending the request to the server. Your request will be ignored. Use 'exit' to close the client.");
                continue;
            }

        } // End while.

        // Exiting...

        scanner.close();

        try {
            synchronized (this.client) {
                this.client.disconnectClient();
            }
        } catch (IOException ex) {
            System.err.println("Error disconnecting the client: " + ex.getMessage());
        }

        // The thread using the Client class will be stopped, since disconnectClient() will close the socket.

        // Terminating this thread.
        return;
        
    }

}
