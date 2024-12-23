package CROSS.Client;

import java.util.LinkedList;
import java.util.Scanner;


/**
 * This class is responsible for handling the client CLI.
 * It's a thread.
 * @version 1.0
 * @see Client
 * @see Thread
 * @see ClientActions
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
        if (client == null) throw new NullPointerException("The client object can't be null.");
        this.client = client;
    }
    
    @Override
    public void run(){

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Client CLI -> ");

            String command = scanner.nextLine().toLowerCase().trim();

            if (command.equals("exit")) {
                break;
            }

            // Check if the command is valid.
            ClientActions action = null;
            try {
                action = ClientActionsUtils.actionFromString(command);
            } catch (IllegalArgumentException | NullPointerException e) {
                // This is not a critical error, just an invalid command.
                System.out.println("Invalid string command.");
                continue;
            }

            // Parse the arguments.
            LinkedList<String> args = null;
            try {
                args = ClientActionsUtils.parseCommandFromString(command, action);
                ClientActionsUtils.parseArgs(args, action);
            } catch (IllegalArgumentException | NullPointerException e) {
                // This is not a critical error, just a invalid command.
                System.out.println("Invalid arguments.");
                continue;
            }

            // TODO: Send the JSON request to the server.
            String jsonToSend = "";
            switch (action) {
                case REGISTER:

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
                    break;
            }

            try {
                this.client.sendJSONToServer(jsonToSend);
            } catch (NullPointerException e) {
                System.err.println("Error sending JSON to the server.");
                Thread.currentThread().interrupt();
            }

        } // End while.

        // Clean up.
        scanner.close();
        
    }

}
