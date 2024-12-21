package CROSS.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import com.google.gson.Gson;

import CROSS.API.JSONInterface;
import CROSS.Enums.ClientActions;
import CROSS.Exceptions.InvalidUser;
import CROSS.Users.User;
import CROSS.Users.Users;

// Client thread.
// Each client has its own thread.
public class ClientThread implements Runnable {
    
    // Specific client socket.
    private final Socket socket;
    
    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getClientIP() {
        return socket.getInetAddress().getHostAddress();
    }

    public Integer getClientPort() {
        return Integer.parseInt(socket.getPort() + "");
    }

    @Override
    public String toString() {
        return String.format("Client thread ID: %s - IP: %s - Port: %s", Thread.currentThread().threadId(), getClientIP(), getClientPort());
    }
    
    // Main loop logic.
    @Override
    public void run() {
        System.out.printf("%s started successfully.\n", this.toString());
        try {
            // Input from extern to our server.
            // Output from our server to extern.
            InputStream in = socket.getInputStream();
            // UTF-8 is the default encoding.
            Scanner scanner = new Scanner(in);
            OutputStream out = socket.getOutputStream();
            Gson gson = new Gson();
            while (true) {
                String data = scanner.nextLine();
                // TODO: Read API json java object request from the socket.

                ClientActions action = JSONInterface.parseRequest(data);
                switch (action) {
                    case REGISTER:
                        User user = JSONInterface.getUser();
                        try {
                            Users.addUser(user);
                        }catch (InvalidUser e) {
                            // TODO: Error handling.
                            System.err.println(e.getMessage());
                            System.exit(-1);
                        }
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
                    default:
                        throw new IllegalArgumentException("Invalid action.");
                }

                // TODO: Remove this.
                if ("42"==data) {
                    gson.toJson("Hello", System.out);
                    out.write("42".getBytes());
                    break;
                }
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
