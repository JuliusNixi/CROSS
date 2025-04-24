package CROSS.Users;

import java.util.LinkedList;
import CROSS.Client.Client;

/**
 * 
 * This class represents an User.
 * 
 * An user has a username and a password.
 * 
 * It implements the Comparable interface to allow comparing and sorting the users by username.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 */
public class User implements Comparable<User> {
    
    // User's data.
    private final String username;
    private final String password;

    // Each user has a file line id, it's used to update the user in the users database file without rewriting the whole file.
    // It indicates the line of the users database file where the user is stored.
    // This users database file line, when an user is updated, is overwritten (using the file line id) with blank spaces and then the updated (new) user is appended at the end of the users database file.
    // Transient to avoid serialization of the clients list by GSON in / to JSON.
    private transient Long fileLineId = null;

    // A user can have multiple clients connected with it.
    // Transient to avoid serialization of the clients list by GSON in / to JSON.
    private transient LinkedList<Client> connectedClients = new LinkedList<Client>();

    /**
     * 
     * This constructor creates an User with a given username and a given password.
     * 
     * It checks if the username and the password are valid.
     * Username and password cannot be too long, too short, empty or null.
     * Username MUST contains ONLY lowercase letters and numbers.
     * 
     * @param username The username of the User as a String.
     * @param password The password of the User as a String.
     * 
     * @throws NullPointerException If the username or the password are null.
     * @throws IllegalArgumentException If the username or the password are empty, too long or too short. If the username contains characters other than lowercase letters and numbers.
     * 
     */
    public User(String username, String password) throws NullPointerException, IllegalArgumentException {

        // Null checks.
        if (username == null) {
            throw new NullPointerException("Username in user creation cannot be null.");
        }
        if (password == null) {
            throw new NullPointerException("Password in user creation cannot be null.");
        }

        // Check if the username and the password are valid.
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Username or password in user creation cannot be empty.");
        }
        if (username.length() > 40 || password.length() > 40) {
            throw new IllegalArgumentException("Username or password in user creation are too long.");
        }
        if (username.length() <= 3 || password.length() <= 3) {
            throw new IllegalArgumentException("Username or password in user creation are too short.");
        }
        if (!username.matches("[a-z0-9]+")) {
            throw new IllegalArgumentException("Username in user creation contains characters other than lowercase letters and numbers.");
        }
        
        this.username = username;
        this.password = password;

    }

    // GETTERS
    /**
     * 
     * This method returns the username of the User.
     * 
     * @return The username of the User as a String.
     * 
     */
    public String getUsername() {

        return this.username;

    }
    // DISCLAIMER:
    // This method and the toString() method are used for debugging and since this is not a real in-production application.
    // In a real application, the password should not be shown.
    // The authentication should be done without handling the text plain password.
    /**
     * 
     * This method returns the password of the User.
     * 
     * @return The password of the User as a String.
     * 
     */
    public String getPassword() {

        return this.password;

    }
    /**
     * 
     * This method returns the file line id of the User.
     * The file line id is used to update the User in the users database file without rewriting the whole file.
     * Could be null if it's not set.
     * 
     * @return The file line id of the User as a Long or null if it's not set.
     * 
     */
    public Long getFileLineId() {

        return this.fileLineId;

    }

    // SETTERS
    /**
     * 
     * This method sets the file line id of the User.
     * The file line id is used to update the User in the users database file without rewriting the whole file.
     * 
     * Synchronized to avoid multiple threads to set the file line id at the same time.
     * 
     * @param fileLineId The file line id of the User as a Long.
     * 
     */
    public synchronized void setFileLineId(Long fileLineId) {

        // File line id COULD BE NULL, used to remove the file line id in the addUser() during an exception handling.

        this.fileLineId = fileLineId;

    }

    @Override
    public synchronized int compareTo(User otherUser) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (otherUser == null) {
            throw new NullPointerException("User to compare to cannot be null.");
        }

        synchronized (otherUser) {

            // Check if the file line id is the same.
            if (this.getFileLineId() != null && otherUser.getFileLineId() != null && this.getFileLineId().compareTo(otherUser.getFileLineId()) == 0 && this.getUsername().compareToIgnoreCase(otherUser.getUsername()) != 0) {
                throw new IllegalArgumentException("Comparing users with the same file line id but different username is not allowed to prevent introducing bugs.");
            }

            return this.username.compareTo(otherUser.getUsername());

        }

    }

    @Override
    public synchronized String toString() {

        String fileLineIdStr = this.getFileLineId() == null ? "null" : this.getFileLineId().toString();

        return String.format("Username [%s] - Password [%s] - FileLineID [%s]", this.getUsername(), this.getPassword(), fileLineIdStr);

    }

    /**
     * 
     * Add a client to the list of the clients connected with this user.
     * 
     * Synchronized to avoid multiple threads to add a client at the same time.
     * Synchronized on the client to avoid multiple threads to modify the client during the addition.
     * 
     * @param client The client to add to the list of the clients connected with this user.
     * 
     * @throws NullPointerException If the client is null.
     * 
     */
    public synchronized void addClient(Client client) throws NullPointerException {

        // Null check.
        if (client == null) {
            throw new NullPointerException("Client connected to add to the list in the user cannot be null.");
        }

        synchronized (client) {

            this.connectedClients.add(client);

        }

    }
    /**
     * 
     * Remove a client from the list of the clients connected with this user.
     * 
     * Synchronized to avoid multiple threads to remove a client at the same time.
     * Synchronized on the client to avoid multiple threads to modify the client during the removal.
     * 
     * @param client The client to remove from the list of the clients connected with this user.
     * 
     * @throws NullPointerException If the client is null.
     * @throws IllegalArgumentException If the client is not in the list.
     * 
     */
    public synchronized void removeClient(Client client) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (client == null) {
            throw new NullPointerException("Client connected to remove from the list in the user cannot be null.");
        }

        synchronized (client) {

            Boolean result = this.connectedClients.remove(client);
            if (!result) {
                throw new IllegalArgumentException("Client connected to remove from the list in the user is not in the list.");
            }

        }

    }


}
