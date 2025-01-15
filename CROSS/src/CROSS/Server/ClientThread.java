package CROSS.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import CROSS.API.Responses.ResponseAndMessage;
import CROSS.API.Responses.ResponseCode;
import CROSS.API.Responses.ResponseCode.AllResponses;
import CROSS.API.Responses.ResponseCode.ResponseType;

/**
 * This class rapresent a thread that will handle a specific client.
 * Each client will have its own dedicated thread.
 * 
 * This thread is started after a new client connection acceptance by the AcceptThread class.
 * 
 * This thread is submitted to CachedThreadPool.
 * 
 * @version 1.0
 * @see Server
 * @see AcceptThread
 */
public class ClientThread implements Runnable {
    
    // Specific client socket.
    private final Socket socket;
    
    /**
     * Constructor of the ClientThread class.
     * 
     * @param socket The socket of the client that this thread will handle.
     * 
     * @throws NullPointerException If the socket is null.
     */
    public ClientThread(Socket socket) throws NullPointerException {
        // Null check.
        if (socket == null)
            throw new NullPointerException("Socket cannot be null.");

        this.socket = socket;
    }

    // GETTERS
    /**
     * Getter for the client's socket.
     * 
     * @return The client's socket.
     */
    public Socket getSocket() {
        return this.socket;
    }
    /**
     * Getter for the client's IP.
     * Used for debugging.
     * 
     * @return The client's IP as String.
     */
    public String getClientIP() {
        return this.socket.getInetAddress().getHostAddress();
    }
    /**
     * Getter for the client's port.
     * Used for debugging.
     * 
     * @return The client's port as Integer.
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

        System.out.printf("%s started successfully.\n", this.toString());

        // Input from extern to our server.
        // Output from our server to extern.
        // UTF-8 is the default encoding.
        InputStream in = null;
        OutputStream out = null;
        
        try {
            in = this.socket.getInputStream();
            out = this.socket.getOutputStream();
        }catch (IOException ex) {
            // TODO: Error hanlding.
        }

        Scanner scanner = new Scanner(in);

        while (true) {

            // JSONs sent are always '\n' terminated.
            String data = null;
            try {
                data = scanner.nextLine();
            }catch (NoSuchElementException | IllegalStateException ex) {
                // TODO: Error handling.
            }

            ResponseType response = null;
            try {
                
                // Getting received operation.
                String operationDetected = null;
                JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
                operationDetected = jsonObject.get("operation").getAsString();
                operationDetected = operationDetected.toLowerCase().trim();

                // Checking if the operation is supported.
                for (ResponseType responseCurrent : ResponseType.values()) {
                    String rstr = responseCurrent.toString().toLowerCase();
                    if (operationDetected.equals(rstr)) {
                        response = responseCurrent;
                        break;
                    }
                }

                if (response == null) {
                    throw new UnsupportedOperationException("Operation not supported.");
                }

            }catch (JsonParseException | UnsupportedOperationException | IllegalStateException ex) {

                // Sending an error response to the client.
                // Create a response.
                AllResponses responseContent = AllResponses.INVALID_REQUEST;
                ResponseType responseType = ResponseType.INVALID_REQUEST;
                ResponseCode responseCode = new ResponseCode(responseType, responseContent);
                ResponseAndMessage responseErr = new ResponseAndMessage(responseCode, "Invalid request.");
                String responseErrString = responseErr.toJSON(true);

                System.err.printf("Invalid request from %s:%s.\n", this.socket.getInetAddress().getHostAddress(), this.socket.getPort());

                // Write the response to the client.
                try {
                    out.write(responseErrString.getBytes());
                    out.flush();
                    continue;
                }catch (IOException ex2) {
                    // Ignore request.
                    continue;
                }
        
            }
        
            // TODO: Read API JSON requests from the client's socket.
            Boolean exit = false;
            switch (response) {
                case REGISTER:
                case UPDATE_CREDENTIALS:
                case LOGIN:
                case LOGOUT:
                case INSERT_LIMIT_ORDER:
                case INSERT_MARKET_ORDER:
                case INSERT_STOP_ORDER:
                case CANCEL_ORDER:
                case CLOSED_TRADES:
                case GET_PRICE_HISTORY:
                case SERVER_FULL:
                case EXIT:
                    exit = true;
                    break;
                case INVALID_REQUEST:
                case ORDER_INFO:
            }

            if (exit) break;
                
        } // End While.

        // I received an exit request.
        
        // Clean up.
        try {
            in.close();
            out.close();
            scanner.close();
            this.socket.close();
        }catch (IOException ex) {
            System.err.printf("Error while closing %s: %s.\n", this.toString(), ex.getMessage());
            return;
        }

        System.out.printf("%s closed successfully.\n", this.toString());
        return;

    }

}
