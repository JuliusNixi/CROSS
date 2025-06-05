package cross.client;

import java.io.IOException;
import java.util.LinkedList;
import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.InputFlag;
import org.jline.terminal.Attributes.LocalFlag;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;
import cross.api.requests.Request;
import cross.utils.ClientActionsUtils;
import cross.utils.ClientActionsUtils.ClientActions;

/**
 *  
 * processCommand -> ClientCLIThread

    CALLS:

        actionFromString -> ClientActionsUtils

        parseCommandFromString -> ClientCLICommandParser

        parseArgs -> ClientCLICommandParser -> CROSS OBJECTS

        getJSONRequest -> ClientCLICommandParser -> APIs OBJECTS-> JSON STRING

        sendJSONToServer -> Client
        
 * 
 */

/**
 *
 * This class is responsible for handling the client's CLI.
 * 
 * The client's CLI is a command line interface that allows the user to interact with the client to send requests to the server.
 * It will read the user's input from the keyboard and process the commands by sending them to the server as JSON requests through a TCP socket.
 *
 * It's a dedicated thread started with the CLIStart() method from the Client class.
 * This thread is unique for the Client class.
 *
 * It has an associated client that will be used to send the JSON requests to the server.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Client
 * 
 * @see ClientCLICommandParser
 * 
 * @see ClientActionsUtils
 */
class ClientCLIThread extends Thread {

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
            throw new NullPointerException("The client object to use in the CLI thread cannot be null.");

        this.client = client;

    }
    
    // Main CLI loop.
    @Override
    public void run() {

        Integer exitStatus = 0;

        // Will store the user's input command.
        StringBuilder buffer = new StringBuilder();

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
            // Disable the echo, to not see what we are typing, we will handle manually this behavior.
            rawAttributes.setLocalFlag(LocalFlag.ECHO, false);
            // Input Carriage Return to Newline enabled, '\r' -> '\n'.
            rawAttributes.setInputFlag(InputFlag.ICRNL, true);
            // Apply the new attributes.
            terminal.setAttributes(rawAttributes);

            NonBlockingReader reader = terminal.reader();

            final int BACKSPACE = 127;
            final int CTRL_H = 8;
            final int NEW_LINE = 10;
            final int CARRIAGE_RETURN = 13;

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
                }
                else if (ch == BACKSPACE || ch == CTRL_H) {
                    if (buffer.length() > 0) {
                        buffer.setLength(buffer.length() - 1);
                        System.out.print("\b \b");
                    }
                    continue;
                }
                else if (ch >= 32 && ch < BACKSPACE) {
                    // Valid char received, append it to the buffer.
                    char c = (char) ch;
                    buffer.append(c);
                    // Print the char to the terminal.
                    System.out.print(c);
                    continue;
                } else if (ch == NEW_LINE || ch == CARRIAGE_RETURN) {
                    // End of line '\n' received, process the command.
                    buffer.append('\n');
                    System.out.print(System.lineSeparator());
                }

                if (buffer.length() > 0 && buffer.toString().endsWith("\n")) {
                    // End of line '\n' received, process the command.
                    String line = buffer.toString();

                    if (!line.trim().equals("\n"))
                        System.out.printf("Command received: %s\n", line.replace("\n", ""));
                    else {
                        // Empty command, continue the loop.
                        System.out.print(PROMPT_STRING);
                        buffer.setLength(0);
                        continue;
                    }

                    // Processing the command.
                    Integer commandStatus;
                    commandStatus = this.processCommand(line);
                    if (commandStatus == -1) {
                        // A critical error occurred, exit.
                        running = false;
                        exitStatus = -1;
                        try {
                            this.client.disconnectClient();
                            System.out.println("Client disconnected successfully after a critical error in processing your command.");
                        } catch (IllegalStateException ex) {
                            exitStatus = -1;
                            System.err.println("Error disconnecting the client after a critical error in processing your command.");
                        } catch (IOException ex) {
                            exitStatus = -1;
                            System.err.println("I/O error in disconnecting the client after a critical error in processing your command.");
                        }
                        // Exit the loop at the next iteration.
                        continue;
                    }
                    if (commandStatus == 0) {
                        // Invalid command, continue.
                        // Message already printed.
                        System.out.print(PROMPT_STRING);
                        buffer.setLength(0);
                        continue;
                    }
                    if (commandStatus == 1) {
                        // Request completed successfully, continue.
                        // Message already printed.
                        System.out.print(PROMPT_STRING);
                        buffer.setLength(0);
                        continue;
                    }
                    if (commandStatus == 2) {
                        // Exit request received, exit.
                        // NO ERROR, just an exit request.
                        running = false;
                        exitStatus = 0;
                        try {
                            this.client.disconnectClient();
                            System.out.println("Client disconnected successfully after your exit request.");
                        } catch (IllegalStateException ex) {
                            exitStatus = -1;
                            System.err.println("Error disconnecting the client after your exit request.");
                        } catch (IOException ex) {
                            exitStatus = -1;
                            System.err.println("I/O error in disconnecting the client after your exit request.");
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

            System.err.println("Error in starting the client's CLI or in reading the user's input. Exiting...");

            if (this.client.isClientConnected()) {
                try {
                    this.client.disconnectClient();
                // IllegalStateException cannot occur here in the if.
                } catch (IOException ex2) {
                    exitStatus = -1;
                    System.err.println("I/O error in disconnecting the client after the critical getting input error in the client's CLI.");
                }
            }

        }

        // Assuming the client is already disconnected.

        client.notificationsStop();
        client.responsesStop();

        // Terminating ALL threads.
        System.exit(exitStatus);

    }

    /**
     *
     * This method is responsible for processing the command string received from the user.
     * Private method, called by the above run() method only, separated to make a cleaner code.
     *
     * Never throws an exception, just return an Integer to indicate the status of the processed command to the caller.
     * 
     * All the prints are done here, not in the caller.
     *
     * @param command The (user's) command string to process.
     *
     * @return An Integer. -1: A critical error occurred, exit. 0: A NON critical error occurred (invalid command), continue. 1: Request completed successfully, continue. 2: Normal exit request received, exit.
     *
     */
    private Integer processCommand(String command) {

        // Null check.
        if (command == null) {
            System.out.println("Null command received.");
            return 0;
        }

        // Trim the command.
        // No lowercase conversion for the command, the args (password) are case sensitive.
        command = command.trim();

        // Check if the command is valid and get the associated action enum.
        ClientActions action;
        try {
            action = ClientActionsUtils.actionFromString(command);
        } catch (IllegalArgumentException ex) {
            // This is not a critical error, just an invalid command.
            // Print here the error and not in the caller.
            // Custom message to adapt it to the user instead of using the exception's one.
            System.out.println("Unknown command, not recognized.");
            return 0;
        } catch (IllegalStateException ex) {
            // This is a critical error.
            // Print here the error and not in the caller.
            // Custom message to adapt it to the user instead of using the exception's one.
            // Disconnect the client in the run() method.
            System.err.println("Invalid commands registered as client actions.");
            return -1;
        }

        // Parse the command.
        LinkedList<String> args;
        try {
            args = ClientCLICommandParser.parseCommandFromString(command);
        } catch (IllegalArgumentException ex) {
            // This is not a critical error, just a invalid command.
            // Print here the error and not in the caller.
            // Here the exception message is a appropriate for the user.
            System.out.println(ex.getMessage());
            return 0;
        }
        // IllegalStateException catched before.

        // Parse the arguments.
        LinkedList<Object> parsedArgs;
        try {
            parsedArgs = ClientCLICommandParser.parseArgs(args, action);
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            // This is not a critical error, just a invalid command.
            // Print here the error and not in the caller.
            // Here the exception message is a appropriate for the user.
            System.out.println(ex.getMessage());
            return 0;
        }
        // IllegalStateException catched before.

        // Get the APIs' Request object.
        Request request;
        try {
            request = ClientCLICommandParser.getRequest(action, parsedArgs);
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            // This is not a critical error, just a invalid command.
            // Print here the error and not in the caller.
            // Here the exception message is a appropriate for the user.
            System.out.println(ex.getMessage());
            return 0;
        } catch (IllegalStateException ex) {
            // This is a critical error.
            // Print here the error and not in the caller.
            // Disconnect the client in the run() method.
            System.err.println(ex.getMessage());
            return -1;
        }
        // IllegalStateException HERE IS DIFFERENT, must be catched before.

        // Get the JSON request as string.
        String jsonToSend = ClientCLICommandParser.getJSONRequest(request);

        // Send the JSON request to the server.
        try {
            this.client.sendJSONToServer(jsonToSend);
            System.out.println("Request succesfully sent to the server.");
        } catch (IllegalStateException ex) {
            // This is a critical error.
            System.err.println("Error in sending your request to the server. Your client is not connected to the server.");
            // Disconnect the client in the run() method.
            return -1;
        } catch (IOException ex) {
            // This is a critical error.
            System.err.println("I/O error in sending your request to the server.");
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

        // Request sent successfully to the server.
        return 1;

    }

}
