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
 * This class is responsible for handling the client CLI.
 * It's a thread started with the start() method from the Client class.
 * It has an associated client that will be used.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * @see Client
 * @see ClientActionsUtils
 * 
 */
public class ClientCLIThread extends Thread {

    // The client object that will be used with the CLI.
    private Client client = null;

    private final String PROMPT_STRING = "Client CLI -> ";

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
    
    // true == i need to exit.
    private Boolean processCommand(String command) throws NullPointerException, RuntimeException {

        // Null check.
        if (command == null)
            throw new NullPointerException("The command string cannot be null.");
        
        // Trim the command and convert it to lowercase.
        command = command.trim().toLowerCase();

        // Check if the command is valid.
        ClientActionsUtils.ClientActions action = null;
        try {
            action = ClientActionsUtils.actionFromString(command);
        } catch (IllegalArgumentException | NullPointerException ex) {
            // This is not a critical error, just an invalid command.
            System.out.println("Invalid string command.");
            return false;
        }
        
        // Parse the arguments.
        LinkedList<String> args = null;
        try {
            args = ClientActionsUtils.parseCommandFromString(command, action);
            ClientActionsUtils.parseArgs(args, action);
        } catch (IllegalArgumentException ex) {
            // This is not a critical error, just a invalid command.
            System.out.println("Invalid arguments.");
            return false;
        }
        
        // Getting the JSON string to send to the server.
        String jsonToSend = null;
        jsonToSend = ClientActionsUtils.getJSONRequest(client, action, args);
        if (jsonToSend == null) {
            // This is not a critical error, just a invalid command.
            System.out.println("Error processing your request, please check it and try again.");
            return false;
        }
        
        System.out.println("DEBUG: Request to send to the server: " + jsonToSend);

        // Loging checks (also performed server-side, but here to avoid spam).
        // TODO: Set log state after login and register in the readed responses from the server.
        if (action != ClientActionsUtils.ClientActions.LOGIN && action != ClientActionsUtils.ClientActions.REGISTER) {
            if (this.client.getLoggedUser() == null) {
                System.out.println("You must be logged in to perform this action.");
                return false;
            }
        }else {
            if (this.client.getLoggedUser() != null) {
                System.out.println("You are already logged in. Please logout to perform this action.");
                return false;
            }
        }
        
        try {
            this.client.sendJSONToServer(jsonToSend);
            System.out.println("Request succesfully sent to the server.");
        } catch (Exception ex) {
            // This is a critical error.
            System.err.println("Error sending the request to the server. Your request will be ignored. Trying to disconnect the client...");
            try {
                this.client.disconnectClient();
            } catch (Exception ex2) {
                System.err.println("Error disconnecting the client after failing to send the request to the server. Exiting...");
            }
            throw new RuntimeException("Error sending the request to the server.");
        }
        
        if (action == ClientActionsUtils.ClientActions.EXIT) {
            System.out.println("Exit request sent to the server. Exiiting...");

            try {
                this.client.disconnectClient();
            } catch (Exception ex) {
                System.err.println("Error disconnecting after an exit request sent.");
                throw new RuntimeException("Error while exiting.");
            }
            
            // Exit OK.
            return true;
        }

        return false;

    }

    // Main CLI loop.
    @Override
    public void run() {

        StringBuffer buffer = new StringBuffer();

        try (Terminal terminal = TerminalBuilder.builder()
                .system(true)         // Usa stdin/stdout di sistema.
                .build()) {

            // Salva gli attributi "originali" per ripristinarli al termine.
            Attributes originalAttributes = terminal.getAttributes();

            // Entra in raw mode.
            terminal.enterRawMode();

            // Dopo essere entrati in raw mode, otteniamo gli attributi attuali per modificarli.
            Attributes rawAttributes = terminal.getAttributes();
            // Riabilitiamo l’ECHO (mostra tasto premuto) di default disabilitato in raw mode.
            rawAttributes.setLocalFlag(LocalFlag.ECHO, true);
            // Disabilitiamo il CR/NL (carriage return / new line) in raw mode.
            rawAttributes.setInputFlag(InputFlag.ICRNL, true); 
            // Applichiamo i nuovi attributi.
            terminal.setAttributes(rawAttributes);

            NonBlockingReader reader = terminal.reader();

            System.out.print(PROMPT_STRING);

            Boolean running = true;
            while (running) {

                // Legge una riga con timeout (ms). Se scade -> ch == NonBlockingReader.READ_EXPIRED.
                int ch = reader.read(100);

                if (ch == NonBlockingReader.READ_EXPIRED) {
                    // Nessun tasto premuto entro il timeout.
                    continue;
                }

                if (ch == -1) {
                    // EOF ricevuto (terminale chiuso).
                    System.out.println("EOF (-1) received. Exiting CLI...");
                    running = false;
                } else if (ch == 4) {
                    // ASCII 4 = Ctrl + D in raw mode.
                    System.out.println("Ctrl + D captured. Exiting CLI...");
                    running = false;
                } else if (ch >= 0) {
                    // Altro carattere digitato, aggiungilo al buffer.
                    char c = (char) ch;
                    buffer.append(c);
                }

                if (buffer.length() > 0 && buffer.toString().endsWith("\n")) {
                    // Fine riga, '\n' rilevato.
                    String line = buffer.toString();

                    if (!line.trim().equals("\n"))
                        System.out.printf("Command received: %s\n", line.replace("\n", ""));

                    // Processing the command.
                    try {
                        running = processCommand(line);
                    } catch (Exception ex) {
                        // This is a critical error.
                        System.err.println("Error processing the command. Exiting...");

                        running = false;
                    }
                    if (!running) continue;

                    // Reset buffer.
                    buffer.setLength(0);

                    System.out.print(PROMPT_STRING);
                }

            }

            System.out.println("Exiting CLI...");

            // Ripristina gli attributi originali.
            terminal.setAttributes(originalAttributes);

        } catch (IOException ex) {

            // TODO: Handle exception.
            System.err.println("Error in starting the Client CLI or in reading the user input. Exiting...");

            if (this.client.getSocket() != null) {
                try {
                    this.client.disconnectClient();
                } catch (Exception ex2) {
                    System.err.println("Error disconnecting the client. Exiting...");
                }
            }
            
        }

        // Exiting...
        System.out.println("Exiting the client CLI.");

        // Assuming the client is already disconnected.

        // Terminating ALL threads, since the client is exited, to avoid someone else thread use the dead client object.
        System.exit(0);

    }


}
