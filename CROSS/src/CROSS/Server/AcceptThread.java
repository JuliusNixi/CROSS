package CROSS.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 
 * This thread is responsible for accepting new connections from clients for the server.
 * 
 * It's started after the server starting by the Server class itself by the startAccept() method.
 * 
 * It creates a new ClientThread for each new client connection accepted.
 * This latter thread is then executed by the a CachedThreadPool.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Server
 * @see ClientThread
 * 
 */
public class AcceptThread extends Thread {

    // The executor to be used to execute the client's threads.
    private final ThreadPoolExecutor executor;

    // The server to be used.
    private final Server server;

    /**
     * 
     * Constructor of the AcceptThread class.
     * 
     * @param server The server to be used.
     * 
     * @throws NullPointerException If the server is null.
     * 
     */
    public AcceptThread(Server server) throws NullPointerException {
        
        // Null check.
        if (server == null)
            throw new NullPointerException("Server in AcceptThread cannot be null.");

        this.server = server;
        this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    }

    @Override 
    public void run() {

        System.out.printf("I'm the thread that will be accept client. My thread id: %d.\n", Thread.currentThread().threadId());

        System.out.printf("Server started a CACHED pool of max %d threads.\n", executor.getMaximumPoolSize());

        System.out.printf("Waiting for connections...\n");
        while (true) {
            Socket clientSocket = null;
            try {
                // Accept connections from clients.

                // Synchronization is not needed here because the server Class (and eventually its user) cannot modify the clientSocket.
                clientSocket = server.getServerSocket().accept();

                System.out.printf("Connection accepted from %s:%s.\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                
                // Create a new thread for the client.
                executor.execute(new ClientThread(clientSocket));

                // Execution ok printed in the ClientThread class.
            }catch (IOException ex) {
                // This is a dediacted thread, so I don't backward the exception, instead I print it and I try to continue.
                System.err.printf("An I/O error occurred while accepting a connection. Trying to continue...\n");
                // Socket is null if an error occurred, so I don't need to close it.
                continue;
            } catch (RejectedExecutionException ex) {
                System.err.printf("An error occurred while submitting a new client's thread to the executor. Trying to continue...\n");
                try {
                    clientSocket.close();
                } catch (IOException ex2) {
                    System.err.printf("An error occurred while closing the client's socket. Trying to continue...\n");
                }
                continue;
            } catch (Exception ex) {
                System.err.printf("An unknown error occurred while accepting a connection from %s:%s. Trying to continue...\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                try {
                    clientSocket.close();
                } catch (IOException ex2) {
                    System.err.printf("An error occurred while closing the client's socket. Trying to continue...\n");
                }
                continue;
            }
        } // End of while.

    }
    
    // GETTERS
    /**
     * 
     * Getter for the server.
     * 
     * @return The server.
     * 
     */
    public Server getServer() {

        return this.server;

    }   

}
