package cross.users;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

/**
 *
 * This class represents an User.
 *
 * An user has an username and a password as strings.
 * 
 * It also has a file line id, that is used to update the user (its credentials) in the users database file without rewriting the whole file.
 * 
 * It also has a list of sockets, that are the sockets of the clients currently logged with the user.
 *
 * It implements the Comparable interface to allow comparing and sorting the users by username.
 *
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Comparable
 *
 */
public class User implements Comparable<User> {

    // User's data.
    private final String username;
    private final String password;

    // Each user has an unique file line id.
    // It's setted when loading the users database file in the memory.
    // It's used to update the user (its credentials) in the users database file without rewriting the whole file.
    // It indicates the line of the users database file where the user is stored (written).
    // This users database file line, when an user is updated (its credentials), is overwritten (using the file line id) with blank spaces and then the updated (new credentials) user is appended at the end of the users database file.
    // This creates an users database file with a lot of blank lines (when there are a lot of users that change their credentials), but it's faster than rewriting the whole file when there are a lot of registered users.
    // This prevent intensive (and slow) I/O operations.
    // If needed, it's also possible to add a function to defragment the users database file when loading it, before starting to serve the users' requests, so to remove the blank lines and rewrite the whole file without blank lines.
    // This would be an optimal solution (excluding the possibility of going from the file to a DBMS).
    // Transient to avoid the serialization by GSON in / to JSON.
    private transient Long fileLineId = null;

    // An user could have multiple sockets (so multiple clients) connected and logged with it.
    // This list memorizes the sockets of the user currently used.
    // It's used server-side to bind a socket to an user on login / register, so to check if the user is authenticated.
    // Transient to avoid serialization of the sockets list by Gson in / to JSON.
    private transient LinkedList<Socket> loggedSockets;

    private transient LinkedList<InetSocketAddress> notificationsSocket;

    /**
     *
     * This constructor creates an User with a given username and a given password as strings.
     *
     * It checks if the username and the password are valid.
     * Username and password cannot be too long, too short, empty or null.
     * Username MUST contains ONLY lowercase letters and numbers to avoid ambiguity.
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
            throw new IllegalArgumentException("Username or password in user creation is too long.");
        }
        if (username.length() <= 3 || password.length() <= 3) {
            throw new IllegalArgumentException("Username or password in user creation is too short.");
        }
        if (!username.matches("[a-z0-9]+")) {
            throw new IllegalArgumentException("Username in user creation contains characters other than lowercase letters and numbers.");
        }

        this.username = username;
        this.password = password;
        this.loggedSockets = new LinkedList<>();
        this.notificationsSocket = new LinkedList<>();

    }

    public User(String username, String password, Boolean noPasswordCheck)  throws NullPointerException, IllegalArgumentException {

        // Null checks.
        if (username == null) {
            throw new NullPointerException("Username in user creation cannot be null.");
        }
        if (password == null) {
            throw new NullPointerException("Password in user creation cannot be null.");
        }
        if (noPasswordCheck == null) {
            throw new NullPointerException("No password check flag in user creation cannot be null.");
        }

        // Check if the username and the password are valid.
        if (!noPasswordCheck && password.isBlank()) {
            throw new IllegalArgumentException("Password in user creation cannot be empty.");
        }
        if (!noPasswordCheck && (password.length() > 40 || password.length() <= 3)) {
            throw new IllegalArgumentException("Password in user creation is too long or too short.");
        }
        if (username.isBlank()) {
            throw new IllegalArgumentException("Username in user creation cannot be empty.");
        }
        if (username.length() <= 3 || username.length() > 40) {
            throw new IllegalArgumentException("Username in user creation is too short or too long.");
        }
        if (!username.matches("[a-z0-9]+")) {
            throw new IllegalArgumentException("Username in user creation contains characters other than lowercase letters and numbers.");
        }

        this.username = username;
        this.password = password;
        this.loggedSockets = new LinkedList<>();
        this.notificationsSocket = new LinkedList<>();
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

        return String.format("%s", this.username);

    }
    // DISCLAIMER:
    // This method and the toString() method are used for debugging and since this is not a real in-production application.
    // In a real application, the password should not be shown or saved in plain text in the database.
    // The authentication should be done without handling the text plain password.
    /**
     *
     * This method returns the password of the User.
     *
     * @return The password of the User as a String.
     *
     */
    public String getPassword() {

        return String.format("%s", this.password);

    }
    /**
     *
     * This getter method returns the file line id of the User.
     * The file line id is used to update the User (its credentials) in the users database file without rewriting the whole users database file.
     * 
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
     * This setter method sets the file line id of the User.
     * The file line id is used to update the User (its credentials) in the users database file without rewriting the whole users database file.
     *
     * Synchronized to avoid multiple threads to set the file line id at the same time.
     *
     * @param fileLineId The file line id of the User as a Long. It could be null.
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
            throw new NullPointerException("User to compare with cannot be null.");
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

        return String.format("User [Username [%s] - Password [%s] - FileLineID [%s]]", this.getUsername(), this.getPassword(), fileLineIdStr);

    }

    // SOCKETS
    /**
     *
     * Add a socket to the list of the sockets logged with this user.
     *
     * Synchronized to avoid multiple threads to add a socket at the same time.
     *
     * @param socket The socket to add to the list of the sockets logged with this user.
     *
     * @throws NullPointerException If the socket is null.
     * @throws IllegalArgumentException If the socket is already in the list.
     *
     */
    public synchronized void addSocket(Socket socket) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (socket == null) {
            throw new NullPointerException("Socket to add to the list of the sockets logged with this user cannot be null.");
        }

        // Check if the socket is already in the list.
        if (this.loggedSockets == null) {
            this.loggedSockets = new LinkedList<>();
        }

        if (this.loggedSockets.contains(socket)) {
            throw new IllegalArgumentException("Socket already in the list of the sockets logged with this user.");
        }

        this.loggedSockets.add(socket);

    }
    /**
     *
     * Remove a socket from the list of the sockets logged with this user.
     *
     * Synchronized to avoid multiple threads to remove a socket at the same time.
     *
     * @param socket The socket to remove from the list of the sockets logged with this user.
     *
     * @throws NullPointerException If the socket is null.
     * @throws IllegalArgumentException If the socket is not in the list.
     *
     */
    public synchronized void removeSocket(Socket socket) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (socket == null) {
            throw new NullPointerException("Socket to remove from the list of the sockets logged with this user cannot be null.");
        }

        if (this.loggedSockets == null) {
            this.loggedSockets = new LinkedList<>();
        }

        // Check if the socket is in the list.
        if (!this.loggedSockets.contains(socket)) {
            throw new IllegalArgumentException("Socket to remove from the list of the sockets logged with this user is not in the list.");
        }

        this.loggedSockets.remove(socket);

    }
    /**
     *
     * Check if a socket is in the list of the sockets logged with this user.
     *
     * Synchronized to avoid multiple threads to add / remove a socket during the check.
     *
     * @param socket The socket to check if is in the list of the sockets logged with this user.
     *
     * @throws NullPointerException If the socket is null.
     *
     */
    public synchronized Boolean containsSocket(Socket socket) throws NullPointerException {

        // Null check.
        if (socket == null) {
            throw new NullPointerException("Socket to check if is in the list of the sockets logged with this user cannot be null.");
        }

        if (this.loggedSockets == null) {
            this.loggedSockets = new LinkedList<>();
        }

        return this.loggedSockets.contains(socket);

    }


    // NOTIFICATION SOCKETS
    /**
     *
     * Add a notification socket to the list of the notification sockets.
     *
     * Synchronized to avoid multiple threads to add a notification socket at the same time.
     *
     * @param socket The socket to add to the list of the notification sockets.
     *
     * @throws NullPointerException If the notification socket is null.
     * @throws IllegalArgumentException If the notification socket is already in the list.
     *
     */
    public synchronized void addNotificationSocket(InetSocketAddress socket) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (socket == null) {
            throw new NullPointerException("Notification socket to add to the list of the notification sockets cannot be null.");
        }

        // Check if the socket is already in the list.
        if (this.notificationsSocket == null) {
            this.notificationsSocket = new LinkedList<>();
        }

        if (this.notificationsSocket.contains(socket)) {
            throw new IllegalArgumentException("Notification socket already in the list of the notification sockets.");
        }

        this.notificationsSocket.add(socket);

    }
    /**
     *
     * Remove a notification socket from the list of the notification sockets.
     *
     * Synchronized to avoid multiple threads to remove a notification socket at the same time.
     *
     * @param socket The notification socket to remove from the list of the notification sockets.
     *
     * @throws NullPointerException If the notification socket is null.
     * @throws IllegalArgumentException If the notification socket is not in the list.
     *
     */
    public synchronized void removeNotificationSocket(InetSocketAddress socket) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (socket == null) {
            throw new NullPointerException("Notification socket to remove from the list of the notification sockets cannot be null.");
        }

        if (this.notificationsSocket == null) {
            this.notificationsSocket = new LinkedList<>();
        }

        // Check if the notification socket is in the list.
        if (!this.notificationsSocket.contains(socket)) {
            throw new IllegalArgumentException("Notification socket to remove from the list of the notification sockets is not in the list.");
        }

        this.notificationsSocket.remove(socket);

    }
    /**
     *
     * Check if a notification socket is in the list of the notification sockets.
     *
     * Synchronized to avoid multiple threads to add / remove a notification socket during the check.
     *
     * @param socket The notification socket to check if is in the list of the notification sockets.
     *
     * @throws NullPointerException If the notification socket is null.
     *
     */
    public synchronized Boolean containsNotificationSocket(InetSocketAddress socket) throws NullPointerException {

        // Null check.
        if (socket == null) {
            throw new NullPointerException("Notification socket to check if is in the list of the notification sockets cannot be null.");
        }

        if (this.notificationsSocket == null) {
            this.notificationsSocket = new LinkedList<>();
        }

        return this.notificationsSocket.contains(socket);

    }
    public LinkedList<InetSocketAddress> getNotificationsSockets() {

        if (this.notificationsSocket == null) {
            this.notificationsSocket = new LinkedList<>();
        }
        
        return this.notificationsSocket;

    }



}
