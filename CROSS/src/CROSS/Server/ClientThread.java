package CROSS.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import CROSS.API.RequestResponse;
import CROSS.API.RequestResponse.AllResponses;
import CROSS.API.RequestResponse.RequestResponseType;
import CROSS.API.RequestResponse.ResponseType;
import CROSS.API.Responses.ResponseAndMessage;

import java.io.BufferedInputStream;

/**
 * 
 * This class rapresent a thread that will handle a specific client.
 * Each client will have its own dedicated thread.
 * 
 * This thread is started after a new client connection acceptance by the AcceptThread class.
 * This thread is then submitted to CachedThreadPool.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see AcceptThread
 * 
 */
public class ClientThread implements Runnable {
    
    // Specific client socket.
    private final Socket socket;

    // Output and input streams for the client's socket.
    private InputStream in = null;
    private OutputStream out = null;

    // Buffered stream for the client's socket.
    private BufferedInputStream bin = null;
    
    /**
     * 
     * Constructor of the ClientThread class.
     * 
     * @param socket The socket of the client that this thread will handle.
     * 
     * @throws NullPointerException If the socket is null.
     * 
     */
    public ClientThread(Socket socket) throws NullPointerException {

        // Null check.
        if (socket == null)
            throw new NullPointerException("Socket in the client's thread cannot be null.");

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

        return this.socket.getInetAddress().getHostAddress();

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
        try (InputStream in = this.socket.getInputStream(); OutputStream out = this.socket.getOutputStream()) {
            this.in = in;
            this.out = out;
        }catch (IOException ex) {

            // This is a dediacted thread, so I don't backward the exception, instead I print it and I try to continue.

            System.err.printf("Error while getting input and output streams from %s:%s. Closing this connection...\n", this.getClientIP(), this.getClientPort());
            
            try {
                this.socket.close();
            }catch (IOException ex2) {
                System.err.printf("Error while closing socket %s:%s.\n", this.getClientIP(), this.getClientPort());
            }

            if (this.in != null) {
                try {
                    this.in.close();
                }catch (IOException ex2) {
                    System.err.printf("Error while closing input stream from %s:%s.\n", this.getClientIP(), this.getClientPort());
                }
            }

            return;

        }

        // Buffered input stream.
        try (BufferedInputStream bin = new BufferedInputStream(this.in)) {
            this.bin = bin;
        }catch (IOException ex) {

            // This is a dediacted thread, so I don't backward the exception, instead I print it and I try to continue.

            System.err.printf("Error while getting buffered input stream from %s:%s. Closing this connection...\n", this.getClientIP(), this.getClientPort());
            
            try {
                this.socket.close();
            }catch (IOException ex2) {
                System.err.printf("Error while closing socket %s:%s.\n", this.getClientIP(), this.getClientPort());
            }

            return;

        }

        Scanner scanner = new Scanner(this.in);

        while (true) {

            // JSONs sent are always '\n' terminated.
            String data = null;
            try {
                data = scanner.nextLine();
                if (data == null) {
                    // Message printed in the catch block.
                    throw new NoSuchElementException("");
                }
            } catch (NoSuchElementException ex) {
                // Nothing received or '\n' NOT received.
                // Ignore request.
                continue;
            }

            // Checking if the received JSON is valid and getting the operation.
            RequestResponseType receivedRequest = null;
            try {
                
                // Getting received operation.
                String operationDetected = null;

                JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();

                JsonElement jsonElement = jsonObject.get("operation");
                if (jsonElement == null) {
                    // Message printed in the catch block.
                    throw new UnsupportedOperationException("");
                }

                operationDetected = jsonElement.getAsString();
                operationDetected = operationDetected.toLowerCase().trim();

                // Checking if the operation is supported.
                for (RequestResponseType requestResponseType : RequestResponseType.values()) {

                    String rstr = requestResponseType.toString().toLowerCase().trim();
                    if (operationDetected.equals(rstr)) {
                        receivedRequest = requestResponseType;
                        break;
                    }
                    
                }

                if (receivedRequest == null) {
                    // Message printed in the catch block.
                    throw new UnsupportedOperationException("");
                }

            } catch (JsonParseException | UnsupportedOperationException | IllegalStateException ex) {

                // Received an invalid JSON or an unsupported operation.

                // Sending an error response to the client.

                // Create a response.
                RequestResponseType type = RequestResponseType.INVALID_REQUEST;
                // No content - code for this response's type.
                RequestResponse response = new RequestResponse(type);
                String responseString = response.toJSON(true);

                System.err.printf("Invalid request from %s:%s.\n", this.socket.getInetAddress().getHostAddress(), this.socket.getPort());

                // Write the response to the client.
                try {
                    out.write(responseErrString.getBytes());
                    out.flush();
                }catch (IOException ex2) {
                    // Ignore request.
                    System.err.printf("Error while writing an 'invalid request' response to %s:%s.\n", this.getClientIP(), this.getClientPort());
                    continue;
                }

                continue;
        
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

            this.bin.close();
            this.in.close();

            this.out.close();

            scanner.close();

            this.socket.close();

        }catch (IOException ex) {

            System.err.printf("Error while closing all resources from %s:%s.\n", this.getClientIP(), this.getClientPort());
            return;

        }

        System.out.printf("%s closed all resources successfully.\n", this.toString());

        // Terminate the thread.
        return;

    }

}
