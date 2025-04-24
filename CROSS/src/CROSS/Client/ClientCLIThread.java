package CROSS.Client;

import java.io.IOException;
import java.util.LinkedList;
import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.InputFlag;
import org.jline.terminal.Attributes.LocalFlag;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

/**
 * 
 * This class is responsible for handling the client's CLI.
 * 
 * It's a dedicated thread started with the CLI() method from the Client class.
 * 
 * It has an associated client that will be used.
 * 
 * It will read the user's input from the CLI and process the commands by sending them to the server as JSON requests.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Client
 * @see ClientActionsUtils
 * 
 */
public class ClientCLIThread extends Thread {

    // The client object that will be used with the CLI.
    private final Client client;

    // The prompt string to show to the user in the terminal.
    private final static String PROMPT_STRING = "Client CLI -> ";

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
            throw new NullPointerException("The client object in the CLI thread cannot be null.");

        this.client = client;

    }
    
    // Main CLI loop.
    @Override
    public void run() {

        Integer exitStatus = 0;

        // Will store the user's input command.
        StringBuffer buffer = new StringBuffer();

        // Try-with-resources to close the terminal at the end.
        try (Terminal terminal = TerminalBuilder.builder()
                .system(true)         // Use system's stdin / stdout.
                .build()) {

            // Store the original terminal attributes to restore them at the end.
            Attributes originalAttributes = terminal.getAttributes();

            // Enter in raw mode to have a better control of the input / terminal settings.
            terminal.enterRawMode();

            // We get the raw attributes to modify them.
            Attributes rawAttributes = terminal.getAttributes();
            // Re-enable the echo (to see what we are typing) disabled by raw mode.
            rawAttributes.setLocalFlag(LocalFlag.ECHO, true);
            // Disable the new line / carriage return conversion.
            rawAttributes.setInputFlag(InputFlag.ICRNL, true); 
            // Apply the new attributes.
            terminal.setAttributes(rawAttributes);

            NonBlockingReader reader = terminal.reader();

            System.out.print(PROMPT_STRING);

            Boolean running = true;
            while (running) {

                // It reads a char with the given timeout (ms). If the timeout expires -> ch == NonBlockingReader.READ_EXPIRED.
                int ch = reader.read(100);

                if (ch == NonBlockingReader.READ_EXPIRED) {
                    // No input received within the timeout, continue the loop.
                    continue;
                }

                if (ch == -1) {
                    // EOF received.
                    System.out.println("EOF (-1) received. Exiting CLI...");
                    running = false;
                    continue;
                } else if (ch == 4) {
                    // ASCII 4 = Ctrl + D in raw mode.
                    System.out.println("Ctrl + D captured. Exiting CLI...");
                    running = false;
                    continue;
                } else if (ch >= 0) {
                    // Valid char received, append it to the buffer.
                    char c = (char) ch;
                    buffer.append(c);
                }

                if (buffer.length() > 0 && buffer.toString().endsWith("\n")) {
                    // End of line '\n' received, process the command.
                    String line = buffer.toString();

                    if (!line.trim().equals("\n"))
                        System.out.printf("Command received: %s\n", line.replace("\n", ""));
                    else
                        // Empty command, continue the loop.
                        continue;

                    // Processing the command.
                    Integer commandStatus = null;
                    commandStatus = this.processCommand(line);
                    if (commandStatus == -1) {
                        // A critical error occurred, exit.
                        System.err.println("Error processing the command. Exiting...");
                        running = false;
                        exitStatus = -1;
                        // Exit the loop at the next iteration.
                        continue;
                    }
                    if (commandStatus == 0) {
                        // Invalid command, continue.
                        // Message already printed.
                    }
                    if (commandStatus == 1) {
                        // Request completed successfully, continue.
                        System.out.println("Request sent successfully.");
                    }
                    if (commandStatus == 2) {
                        // Exit request received, exit.
                        // NO ERROR, just an exit request.
                        running = false;
                        exitStatus = 0;
                        try {
                            this.client.disconnectClient();
                        } catch (Exception ex) {
                            exitStatus = -1;
                            System.err.println("Error disconnecting the client after an exit request received. Exiting...");
                        }
                        // Exit the loop at the next iteration.
                        continue;
                    }

                    // Reset buffer.
                    buffer.setLength(0);

                    // Print the prompt string.
                    System.out.print(PROMPT_STRING);

                }

            }

            // Exiting...
            System.out.println("Exiting the client CLI.");

            // Restore the original terminal attributes.
            terminal.setAttributes(originalAttributes);

        } catch (IOException ex) {
            // This is a critical error.
            // Need to show the error to the user and exit.

            exitStatus = -1;

            System.err.println("Error in starting the Client CLI or in reading the user input. Exiting...");

            if (this.client.isClientConnected()) {
                try {
                    this.client.disconnectClient();
                } catch (Exception ex2) {
                    System.err.println("Error disconnecting the client after a critical error in the CLI. Exiting...");
                }
            }
            
        }

        // Assuming the client is already disconnected.

        // Terminating ALL threads, since the client is exited, to avoid someone else thread use the dead client object.
        System.exit(exitStatus);

    }

    /**
     * 
     * This method is responsible for processing the command string received from the user.
     * Private method, called only by the run() method to make a cleaner code.
     * 
     * Never throws an exception, just return an Integer to indicate the status of the command to the caller.
     * 
     * @param command The (user's) command string to process.
     * 
     * @return An Integer. -1: A critical error occurred, exit. 0: A NON critical error occurred (invalid command), continue. 1: Request completed successfully, continue. 2: Normal exit request received, exit.
     * 
     */
    private Integer processCommand(String command) {

        // Null check.
        if (command == null) {
            System.err.println("Null command to process received.");
            return -1;
        }
        
        // Trim the command.
        // No lowercase conversion for the command, the args (password) are case sensitive.
        command = command.trim();

        // Check if the command is valid.
        ClientActionsUtils.ClientActions action = null;
        try {
            action = ClientActionsUtils.actionFromString(command);
        } catch (IllegalArgumentException ex) {
            // This is not a critical error, just an invalid command.
            // Forward the error message to the user.
            System.out.println(ex.getMessage());
            return 0;
        } catch (RuntimeException ex) {
            // This is a critical error.
            return -1;
        }
        
        // Parse the arguments.
        LinkedList<String> args = null;
        LinkedList<Object> parsedArgs = null;
        try {
            args = ClientActionsUtils.parseCommandFromString(command);
            parsedArgs = ClientActionsUtils.parseArgs(args, action);
        } catch (IllegalArgumentException ex) {
            // This is not a critical error, just a invalid command.
            // Forward the error message to the user.
            System.out.println(ex.getMessage());
            return 0;
        }
        // Runtime exception is already handled above, cannot occur here.





        
        
        // TODO: Getting the JSON string to send to the server. NEED TO WRITE THE METHOD TO GET THE JSON STRING TO SEND IN THE CLIENTACTIONUTILS CLASS.
        // TODO: Correct the code since here.
        String jsonToSend = null;
        try {
            jsonToSend = ClientActionsUtils.getJSONRequest(action, parsedArgs, client.getConnectedUser());
        }catch (Exception ex) {
            // TODO: Handle exception.
        }
        if (jsonToSend == null) {
            // This is not a critical error, just a invalid command.
            System.out.println("Error processing your request, please check it and try again.");
            return 0;
        }
        // TODO: Correct the code since here.

        System.out.println("DEBUG: Request to send to the server: " + jsonToSend);
        
        // Send the JSON request to the server.
        try {
            this.client.sendJSONToServer(jsonToSend);
            System.out.println("Request succesfully sent to the server.");
        } catch (Exception ex) {
            // This is a critical error.
            System.err.println("Error sending the request to the server. Your request will be ignored. Trying to disconnecting you...");

            // Disconnect the client in the run() method.
            return -1;
            
        }
        
        // Normal exit request.
        // Request to the server to exit gracefully already sent before above.
        if (action == ClientActionsUtils.ClientActions.EXIT) {

            System.out.println("Exit request sent to the server. Exiting...");

            // Disconnect the client in the run() method.
            return 2;
            
        }

        return 1;

    }

}
