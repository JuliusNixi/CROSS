package CROSS.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

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
     * @throws NullPointerException If the socket is null.
     */
    public ClientThread(Socket socket) throws NullPointerException {
        if (socket == null)
            throw new NullPointerException("Socket cannot be null.");

        this.socket = socket;
    }

    // GETTERS
    /**
     * Getter for the client's socket.
     * @return The client's socket.
     */
    public Socket getSocket() {
        return this.socket;
    }
    /**
     * Getter for the client's IP.
     * Used for debugging.
     * @return The client's IP as String.
     */
    public String getClientIP() {
        return this.socket.getInetAddress().getHostAddress();
    }
    /**
     * Getter for the client's port.
     * Used for debugging.
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

        try {

            // Input from extern to our server.
            // Output from our server to extern.
            // UTF-8 is the default encoding.
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            Scanner scanner = new Scanner(in);

            while (true) {

                String data = scanner.nextLine();

                // TODO: Read API JSON requests from the client's socket.

                // TODO: Remove this.
                if ("42"==data && out.toString() == "a") break;
                
            }

            // Clean up.
            scanner.close();

        } catch (IOException e) {
                // TODO: Error handling.
        }catch (Exception e) {
                // TODO: Error handling.
        }

    }

}
