package CROSS.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import CROSS.API.Responses.ResponseAndMessage;
import CROSS.API.Responses.ResponseCode;
import CROSS.API.Responses.ResponseCode.AllResponses;
import CROSS.API.Responses.ResponseCode.ResponseType;

/**
 * This thread is responsible for accepting new connections from clients for the server.
 * 
 * It's started after the server is started by the Server class.
 * 
 * It creates a new ClientThread for each new client connection accepted.
 * This latter thread is then executed by the executor.
 * @version 1.0
 * @see Server
 */
public class AcceptThread extends Thread {

    private ThreadPoolExecutor executor = null;
    private Server server = null;

    // The current accepting client's socket.
    private Socket clientSocket = null;

    /**
     * Constructor of the AcceptThread class.
     * @param server The server to be used.
     * @throws NullPointerException If the server is null.
     */
    public AcceptThread(Server server) throws NullPointerException {
        // Null check.
        if (server == null)
            throw new NullPointerException("Server cannot be null.");

        this.server = server;
        this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    // ERRORS HANDLING
    /**
     * This method is called when the server is full and a new connection is refused.
     * The internal exception must be handled by the caller.
     * @throws IOException If an I/O error occurs.
     */
    private void acceptError() throws IOException {
        OutputStream out = this.clientSocket.getOutputStream();

        // Create a response.
        AllResponses responseContent = AllResponses.SERVER_FULL;
        ResponseType responseType = ResponseType.SERVER_FULL;
        ResponseCode responseCode = new ResponseCode(responseType, responseContent);
        ResponseAndMessage response = new ResponseAndMessage(responseCode, "Server is full. Try again later.");
        String responseString = response.toJSON();

        System.err.printf("Connection refused from %s:%s.\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());

        // Write the response to the client.
        out.write(responseString.getBytes());
        out.flush();

        // Close the connection.
        out.close();
        this.clientSocket.close();

    }
    /**
     * This method is called when an error occurs and the server cannot close the connection peacefully.
     */
    private void writeError() {

        System.err.printf("An error occurred while closing the connection from %s:%s.\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());

    }

    @Override 
    public void run() {

        System.out.printf("Server started a CACHED pool of max %d threads, but the max number of client connections is %s.\n", executor.getMaximumPoolSize(), server.getMaxConnections().toString());

        System.out.printf("Waiting for connections...\n");
        while (true) {
            try {
                // Accept connections from clients.
                // Synchronization is not needed here because the server doesn't modify the clientSocket.
                this.clientSocket = server.getServerSocket().accept();

                System.out.printf("Connection accepted from %s:%s.\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                
                // Create a new thread for the client.
                executor.execute(new ClientThread(clientSocket));
                // Execution ok printed in the ClientThread class.
            }catch (IOException ex) {
                writeError();
                 // Try to continue accepting new connections.
                 continue;
            } catch (RejectedExecutionException ex) {
                try {
                    acceptError();
                    // Try to continue accepting new connections.
                    continue;
                }catch (IOException ex2) {
                    writeError();
                    // Try to continue accepting new connections.
                    continue;
                }
            } catch (Exception ex) {
                writeError();
                // Try to continue accepting new connections.
                continue;
            }
        } // End of while.

    }
    
    // GETTERS
    /**
     * Getter for the executor.
     * @return The executor as a ThreadPoolExecutor.
     */
    public ThreadPoolExecutor getExecutor() {
        return this.executor;
    } 
    /**
     * Getter for the server.
     * @return The server.
     */
    public Server getServer() {
        return this.server;
    }   

}
