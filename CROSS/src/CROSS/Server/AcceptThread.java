package CROSS.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

public class AcceptThread extends Thread {

    private ThreadPoolExecutor executor = null;
    private Server server = null;

    AcceptThread(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        System.out.printf("Server started a CACHED pool of max %s threads, but the max number of client connections is %s.\n", executor.getMaximumPoolSize(), server.getMaxConnectionsInt());

        System.out.printf("Waiting for connections...\n");
        while (true) {
            try {
                // Accept connections.
                Socket clientSocket = server.getServerSocket().accept();
                System.out.printf("Connection accepted from %s:%s.\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                executor.execute(new ClientThread(clientSocket));
            }catch (IOException ex) {
                System.err.printf("Error IOException accepting connection.\n");
            } catch (RejectedExecutionException ex) {
                System.err.printf("Error RejectedExecutionException during the creation of a new client thread.\n");
            }
            // Generic exception.
            catch (Exception ex) {
                System.err.printf("Generic error reading during the acceptance of a new client or its thread creation.\n");
            }
        } // End of while.
    }
    
    public ThreadPoolExecutor getExecutor() {
        return executor;
    } 
    
    public Server getServer() {
        return server;
    }   

}
