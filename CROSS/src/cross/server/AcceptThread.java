package cross.server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * This thread is responsible for accepting new connections from clients for the server.
 *
 * It's started after the server initialization and the server starting by the Server class itself by the startAccept() method.
 *
 * It creates a new ClientThread for each new client connection accepted.
 * This latter thread is then executed by a CachedThreadPool.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Server
 * @see ClientThread
 *
 */
class AcceptThread extends Thread {

    // The server to be used to accept clients.
    private final Server server;

    // The executor to be used to execute the client's threads.
    private final ThreadPoolExecutor executor;

    /**
     *
     * Constructor of the AcceptThread class.
     *
     * @param server The server to be used to accept clients.
     *
     * @throws NullPointerException If the server is null.
     *
     */
    public AcceptThread(Server server) throws NullPointerException {

        // Null check.
        if (server == null)
            throw new NullPointerException("Server to be used to accept clients in AcceptThread cannot be null.");

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
                // The serverSocket is not null, since this thread is executed only after the startServer() method of the Server class.
                clientSocket = server.getServerSocket().accept();

                System.out.printf("Connection accepted from %s:%s.\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());

                // Create a new thread for the client.
                executor.execute(new ClientThread(clientSocket));

                System.out.printf("Client's %s:%s thread submitted to the executor.\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());

                // Execution ok printed in the ClientThread class.
            }catch (IOException ex) {
                // This is a dediacted thread, so I don't backward the exception, instead I print it and I try to continue.
                System.err.printf("An I/O error occurred while accepting a connection. Trying to continue...\n");
                // Socket is null if an error occurred, so I don't need to close it.
            } catch (RejectedExecutionException ex) {
                // This is a dediacted thread, so I don't backward the exception, instead I print it and I try to continue.
                System.err.printf("An error occurred while submitting a new client's thread to the executor. Trying to continue...\n");
                // Closing the client's socket.
                try {
                    if (clientSocket != null)
                        clientSocket.close();
                } catch (IOException ex2) {
                    System.err.printf("An error occurred while closing the client's socket. Trying to continue...\n");
                }
            }
        } // End of while.

    }
    
}
