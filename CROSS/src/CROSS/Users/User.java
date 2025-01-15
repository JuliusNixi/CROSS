package CROSS.Users;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;

import CROSS.API.Notifications.Trade;

/**
 * This class represents a User.
 * It implements the Comparable interface to allow sorting the users by username.
 * @version 1.0
 */
public class User implements Comparable<User> {
    
    private String username;
    private String password; 

    private Long fileLineId = null;
    
    // The IP address of the client. Used server-side to notify the client about its orders.
    private InetAddress ip = null;
    // The port where the client is listening for notifications.
    private static final Integer clientPort = 4242;

    /**
     * This constructor creates a User with a username and a password.
     * It sanitizes the input and checks if the username and the password are valid.
     * The username is trimmed, lowercased and the whitespaces are removed.
     * The password is trimmed and the whitespaces are replaced with a single space.
     * @param username The username of the User as a String.
     * @param password The password of the User as a String.
     * @throws NullPointerException If the username or the password are null.
     * @throws IllegalArgumentException If the username or the password are empty, too long or too short.
     */
    public User(String username, String password) throws NullPointerException, IllegalArgumentException {

        if (username == null || password == null) {
            throw new NullPointerException("Username or password are null.");
        }

        // Sanitize the input.
        username = username.trim().toLowerCase();
        password = password.trim();
        username = username.replaceAll("\\s+", " ");
        password = password.replaceAll("\\s+", " ");
        // Remove all the whitespaces from the username.
        username = username.replaceAll("\\s", "");

        if (username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Username or password are empty.");
        }

        if (username.length() > 40 || password.length() > 40) {
            throw new IllegalArgumentException("Username or password are too long.");
        }
        if (username.length() <= 3 || password.length() <= 3) {
            throw new IllegalArgumentException("Username or password are too short.");
        }
        
        this.username = username;
        this.password = password;
    }

    // GETTERS
    /**
     * This method returns the username of the User.
     * @return The username of the User as a String.
     */
    public String getUsername() {
        return String.format("%s", this.username);
    }
    // DISCLAIMER:
    // This method and the toString() method are used for debugging.
    // In a real application, the password should not be shown.
    // The authentication should be done without handling the text plain password.
    /**
     * This method returns the password of the User.
     * @return The password of the User as a String.
     */
    public String getPassword() {
        return String.format("%s", this.password);
    }
    /**
     * This method returns the file line id of the User.
     * The file line id is used to update the User in the file without rewriting the whole file.
     * @return The file line id of the User as a Long.
     */
    public Long getFileLineId() {
        return this.fileLineId;
    }
    /**
     * This method returns the IP address of the User.
     * Used to notify the User about its orders from the server.
     * @return The IP address of the User as an InetAddress.
     */
    public InetAddress getIP() {
        return this.ip;
    }
    /**
     * This method returns the port where the client is listening for notifications.
     * @return The port where the client is listening for notifications as an Integer.
     */
    public static Integer getClientPort() {
        return Integer.valueOf(clientPort);
    }

    // SETTERS
    /**
     * This method sets the file line id of the User.
     * The file line id is used to update the User in the file without rewriting the whole file.
     * @param fileLineId The file line id of the User as a Long.
     * @throws NullPointerException If the file line id is null.
     */
    public void setFileLineId(Long fileLineId) throws NullPointerException {
        if (fileLineId == null) {
            throw new NullPointerException("File line id is null.");
        }
        this.fileLineId = fileLineId;
    }
    /**
     * This method sets the IP address of the User.
     * Used to notify the User about its orders from the server.
     * @param ip The IP address of the User as an InetAddress.
     * @throws NullPointerException If the IP address is null.
     * @throws IllegalArgumentException If the IP address is already set.
     */
    public void setIP(InetAddress ip) throws NullPointerException, IllegalArgumentException {
        if (ip == null) {
            throw new NullPointerException("IP address is null.");
        }

        if (this.ip != null) {
            throw new IllegalArgumentException("IP address is already set.");
        }

        this.ip = ip;
    }

    @Override
    public int compareTo(User o) throws IllegalArgumentException {
        if (this.getFileLineId() != null && o.getFileLineId() != null && this.getFileLineId().equals(o.getFileLineId()) && !this.getUsername().equals(o.getUsername())) {
            throw new IllegalArgumentException("Comparing users with the same file line id.");
        }
        return this.username.compareTo(o.getUsername());
    }

    @Override
    public String toString() {
        String fileLineIdStr = this.getFileLineId() == null ? "null" : this.getFileLineId().toString();
        return String.format("Username [%s] - Password [%s] - FileLineID [%s]", this.getUsername(), this.getPassword(), fileLineIdStr);
    }

    /**
     * Notify the user that some orders has been executed.
     * @param executedOrders The list of orders that has been executed.
     */
    public void notifyTrades(LinkedList<CROSS.Orders.Order> executedOrders) {

        // Socket.
        // 0 means use an effimeral port to send the notification.
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(0);
            socket.setSoTimeout(1000);
        }catch (SocketException ex) {
            // TODO: Handle this error.
        }

        // Target host.
        InetAddress clientTarget = null;
        try {
            clientTarget = this.getIP();
            if (clientTarget == null) {
                throw new Exception("IP is null.");
            }
        } catch (Exception ex) {
            // TODO: Handle this error. Adapt the exception to the real one that could be thrown by the getIP method.
        }

        // Notification.
        CROSS.API.Responses.NotificationResponse notification = new CROSS.API.Responses.NotificationResponse();
        for (CROSS.Orders.Order order : executedOrders) {
            Trade trade = null;
            if (order instanceof CROSS.Orders.MarketOrder) {
                CROSS.Orders.MarketOrder marketOrder = (CROSS.Orders.MarketOrder) order;
                trade = new Trade(marketOrder);
            }else if (order instanceof CROSS.Orders.StopMarketOrder) {
                CROSS.Orders.StopMarketOrder stopOrder = (CROSS.Orders.StopMarketOrder) order;
                trade = new Trade(stopOrder);
            }else if (order instanceof CROSS.Orders.LimitOrder) {
                CROSS.Orders.LimitOrder limitOrder = (CROSS.Orders.LimitOrder) order;
                trade = new Trade(limitOrder);
            }else {
                throw new IllegalArgumentException("Order type not supported.");
            }
            notification.addTrade(trade);
        }
        String message = notification.toJSON(true);
        byte[] bytes = message.getBytes();

        // Packet.
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, clientTarget, clientPort);
        try {
            socket.send(packet);
        } catch (IOException ex) {
            // TODO: Handle this error.
        }

    }

}
