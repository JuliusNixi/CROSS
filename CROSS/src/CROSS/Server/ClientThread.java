package CROSS.Server;

import java.net.Socket;

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
    }

}
