package CROSS.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

// TODO: Generate Javadoc.
/**
 * This thread is responsible for accepting new connections from clients for the server.
 * @version 1.0
 * @see Server
 */
public class AcceptThread extends Thread {

    private ThreadPoolExecutor executor = null;
    private Server server = null;

    /**
     * Constructor of the AcceptThread class.
     * @param server The server to be used.
     * @throws IllegalArgumentException If the server is null.
     */
    public AcceptThread(Server server) throws IllegalArgumentException {
        if (server == null)
            throw new IllegalArgumentException("Server cannot be null.");
        this.server = server;
    }

    @Override 
    public void run() {

        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        System.out.printf("Server started a CACHED pool of max %d threads, but the max number of client connections is %s.\n", executor.getMaximumPoolSize(), server.getMaxConnections().toString());

        System.out.printf("Waiting for connections...\n");
        while (true) {
            try {
                // Accept connections from clients.
                Socket clientSocket = server.getServerSocket().accept();
                System.out.printf("Connection accepted from %s:%s.\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                executor.execute(new ClientThread(clientSocket));
            }catch (IOException ex) {
                System.err.printf("Error IOException accepting connection from client.\n");
            } catch (RejectedExecutionException ex) {
                System.err.printf("Error RejectedExecutionException during the creation of a new client thread.\n");
            }
            // Generic exception.
            catch (Exception ex) {
                System.err.printf("Generic error during the acceptance of a new client or its thread creation.\n");
            }
        } // End of while.

    }
    
    /**
     * Getter for the executor.
     * @return The executor.
     */
    public ThreadPoolExecutor getExecutor() {
        return executor;
    } 
    /**
     * Getter for the server.
     * @return The server.
     */
    public Server getServer() {
        return server;
    }   

}
