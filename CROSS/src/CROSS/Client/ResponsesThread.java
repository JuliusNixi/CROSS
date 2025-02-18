package CROSS.Client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * This class is responsible for handling the responses / notifications from the server.
 * It's a dedicated thread, started with the responsesStart() method from the Client class.
 * 
 * It has an associated client that will be used to read from the socket.
 * 
 * Could be used indipendently from the Client's CLI.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Client
 * 
 */
public class ResponsesThread extends Thread {

    // The client object that will be used with this thread to receive responses / notifications from the server.
    private final Client client;

    /**
     * 
     * Constructor of the class.
     * 
     * @param client The client object that will be used with this thread.
     * 
     * @throws NullPointerException If the client object is null.
     * 
     */
    public ResponsesThread(Client client) throws NullPointerException {

        // Null check.
        if (client == null) {
            throw new NullPointerException("The client object to use to receive responses cannot be null.");
        }

        this.client = client;

    }
    
    @Override
    public void run() {

        InputStream in = null;
        BufferedInputStream bin = null;
        try {
            in = client.getInputStream();
            bin = new BufferedInputStream(in);
        } catch (IOException | RuntimeException ex) {
            // Since this is a dedicated thread, I don't backward the exception.
            System.err.println("An error occurred while trying to get the input stream from the client. No responses from the server will be received.");
            // Terminate the thread.
            return;
        }

        Scanner scanner = new Scanner(bin);

        String JSONresponse = null;
        while (true) {

            try {
                // JSON responses from the server are all '\n' terminated.
                JSONresponse = scanner.nextLine();
            } catch (Exception ex) {
                // Since this is a dedicated thread, I don't backward the exception.
                // Trying to continue the thread ignoring the exception.
                System.err.println("An error occurred while trying to read a response from the server. The thread will continue.");
                continue;
            }

            // TODO: Parse the JSON response and print it to the console.
            JsonElement response = JsonParser.parseString(JSONresponse);
            JsonObject responseObj = response.getAsJsonObject();
            // Debug.
            if (responseObj.has("notification")) {
                break;
            }
            System.out.print(JSONresponse);

        }

        scanner.close();

    }


}
