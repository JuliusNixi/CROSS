package CROSS.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import com.google.gson.Gson;

public class ClientThread implements Runnable {
    
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
                // TODO: Read API json java object request.
                // gson.toJson("Hello", System.out);
            }
        } catch (IOException e) {
                // TODO: ERROR.
        }catch (Exception e) {
                // TODO: ERROR.
        }


    }

}
