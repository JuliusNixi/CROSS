package CROSS.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import CROSS.Client.ClientActionsUtils;
import CROSS.Client.ClientActionsUtils.ClientActions;
import java.io.BufferedInputStream;

/**
 * 
 * This class rapresent a thread that will handle a specific client.
 * Each client will have its own dedicated thread with this class.
 * 
 * This thread is started after a new client connection acceptance by the AcceptThread class.
 * This thread is then submitted to CachedThreadPool to be executed.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see AcceptThread
 * 
 */
public class ClientThread implements Runnable {
    
    // Specific client socket.
    // Will not change, so it's final.
    private final Socket socket;

    // Output and input streams for the client's socket.
    private InputStream in = null;
    private OutputStream out = null;

    // Buffered stream for the client's socket.
    private BufferedInputStream bin = null;
    
    /**
     * 
     * Constructor of the class.
     * 
     * @param socket The socket of the client that this thread will handle.
     * 
     * @throws NullPointerException If the socket is null.
     * 
     */
    public ClientThread(Socket socket) throws NullPointerException {

        // Null check.
        if (socket == null)
            throw new NullPointerException("Client socket in the client's thread cannot be null.");

        this.socket = socket;

    }

    // GETTERS
    /**
     * 
     * Getter for the client's IP.
     * 
     * @return The client's IP as String.
     * 
     */
    public String getClientIP() {

        return String.format("%s", this.socket.getInetAddress().getHostAddress());

    }
    /**
     * 
     * Getter for the client's port.
     * 
     * @return The client's port as Integer.
     * 
     */
    public Integer getClientPort() {

        return Integer.parseInt(this.socket.getPort() + "");

    }

    @Override
    public String toString() {

        return String.format("Client thread ID [%s] - IP [%s] - Port [%s]", Thread.currentThread().threadId(), this.getClientIP(), this.getClientPort());
    
    }
    
    // Main loop for client's actions handling logic.
    @Override
    public void run() {

        System.out.printf("%s thread started successfully.\n", this.toString());

        // Input from extern to our server.
        // Output from our server to extern.
        // UTF-8 is the default encoding.

        // Getting input and output streams.
        try {
            this.in = this.socket.getInputStream();
            this.out = this.socket.getOutputStream();
            this.bin = new BufferedInputStream(this.in);
        }catch (IOException ex) {

            // This is a dediacted thread, so I don't backward the exception, instead I print it and I try to continue.

            System.err.printf("Error while getting input and output streams from client %s:%s. Closing this connection...\n", this.getClientIP(), this.getClientPort());

            if (this.in != null) {
                try {
                    this.in.close();
                }catch (IOException ex2) {
                    System.err.printf("Error while closing input stream from client %s:%s.\n", this.getClientIP(), this.getClientPort());
                }
            }

            if (this.out != null) {
                try {
                    this.out.close();
                }catch (IOException ex2) {
                    System.err.printf("Error while closing output stream from client %s:%s.\n", this.getClientIP(), this.getClientPort());
                }
            }

            try {
                this.socket.close();
            }catch (IOException ex2) {
                System.err.printf("Error while closing client socket %s:%s.\n", this.getClientIP(), this.getClientPort());
            }

            // Terminate the thread.
            return;

        }

        Scanner scanner = new Scanner(this.in);

        while (true) {

            String data = null;
            try {
                // JSONs sent are always '\n' terminated.
                data = scanner.nextLine();

                if (data == null || data.isEmpty() || data.isBlank() || !data.endsWith("\n")) {
                    throw new NoSuchElementException("");
                }
            } catch (NoSuchElementException ex) {
                // Nothing received or '\n' NOT received.
                // Ignore request.
                continue;
            }

            ClientActions action = null;
            LinkedList<String> values = new LinkedList<String>();
            try {
                JsonElement jsonElement = JsonParser.parseString(data);
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                String operation = jsonObject.get("operation").getAsString();
                action = ClientActionsUtils.actionFromString(operation);

                JsonObject jsonObjectValues = jsonObject.get("values").getAsJsonObject();
                
                for (String key : jsonObjectValues.keySet()) {
                    String value = jsonObjectValues.get(key).getAsString();
                    values.add(value);
                }

                // This check IS ONLY FOR SYNTAX, THE SEMANTIC MUST BE CHECKED BELOW.
                ClientActionsUtils.parseArgs(values, action);

            } catch (JsonParseException | IllegalStateException | IllegalArgumentException | UnsupportedOperationException | NullPointerException ex) {
                // Error while parsing the JSON.
                // Ignore request after logging.
                System.out.printf("Error while parsing the JSON request from %s:%s.\n", this.getClientIP(), this.getClientPort());
                continue;
            }

            Boolean exit = false;
            // TODO: Parse the JSON request.
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

                case EXIT:

                    exit = true;

                    break;
            
                default:
                    // This should never happen.
                    System.exit(-1);
                    break;

            }

            if (exit)
                break;

                
        } // End While.

        // Disconnect the client.
        
        // Clean up.
        Boolean error = false;
        try {
            this.bin.close();
        }catch (IOException ex) {
            error = true;
            System.err.printf("Error while closing buffered input stream from %s:%s.\n", this.getClientIP(), this.getClientPort());
        }
        try {
            this.in.close();
        }catch (IOException ex) {
            error = true;
            System.err.printf("Error while closing input stream from %s:%s.\n", this.getClientIP(), this.getClientPort());
        }
        try {
            this.out.close();
        }catch (IOException ex) {
            error = true;
            System.err.printf("Error while closing output stream from %s:%s.\n", this.getClientIP(), this.getClientPort());
        }
        try {
            scanner.close();
        }catch (Exception ex) {
            error = true;
            System.err.printf("Error while closing scanner from %s:%s.\n", this.getClientIP(), this.getClientPort());
        }
        try {
            this.socket.close();
        }catch (IOException ex) {
            error = true;
            System.err.printf("Error while closing socket from %s:%s.\n", this.getClientIP(), this.getClientPort());
        }

        if (!error)
            System.out.printf("%s closed all resources successfully.\n", this.toString());

        // Terminate the thread.
        return;

    }

}
