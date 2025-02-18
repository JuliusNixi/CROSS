package CROSS.Users;

/**
 * 
 * This class represents an User.
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

    // Each user has a file line id, it is used to update the user in the database file without rewriting the whole file.
    // It indicates the line of the file where the user is stored.
    private Long fileLineId = null;

    /**
     * 
     * This constructor creates a User with a username and a password.
     * 
     * It sanitizes and checks if the username and the password are valid.
     * The username is trimmed, lowercased and the whitespaces are removed.
     * 
     * @param username The username of the User as a String.
     * @param password The password of the User as a String.
     * 
     * @throws NullPointerException If the username or the password are null.
     * @throws IllegalArgumentException If the username or the password are empty, too long or too short.
     * 
     */
    public User(String username, String password) throws NullPointerException, IllegalArgumentException {

        // Null checks.
        if (username == null) {
            throw new NullPointerException("Username is user creation cannot be null.");
        }
        if (password == null) {
            throw new NullPointerException("Password is user creation cannot be null.");
        }

        // Sanitize the username.
        username = username.trim().toLowerCase();
        // Remove all the whitespaces from the username.
        username = username.replaceAll(" ", "");

        // Check if the username and the password are valid.
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
    // This method and the toString() method are used for debugging and since this is not a real in production application.
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
        return String.format("%s", this.password);
    }
    /**
     * 
     * This method returns the file line id of the User.
     * The file line id is used to update the User in the database file without rewriting the whole file.
     * 
     * @return The file line id of the User as a Long.
     * 
     */
    public Long getFileLineId() {
        return Long.valueOf(this.fileLineId);
    }

    // SETTERS
    /**
     * 
     * This method sets the file line id of the User.
     * The file line id is used to update the User in the database file without rewriting the whole file.
     * 
     * Synchronized to avoid multiple threads to set the file line id at the same time.
     * 
     * @param fileLineId The file line id of the User as a Long.
     * 
     * @throws NullPointerException If the file line id is null.
     * 
     */
    public synchronized void setFileLineId(Long fileLineId) throws NullPointerException {
        
        // Null check.
        if (fileLineId == null) {
            throw new NullPointerException("File line id to set on an user cannot be null.");
        }

        this.fileLineId = fileLineId;

    }

    @Override
    public int compareTo(User otherUser) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (otherUser == null) {
            throw new NullPointerException("User to compare to cannot be null.");
        }

        // Check if the file line id is the same.
        if (this.getFileLineId() != null && otherUser.getFileLineId() != null && this.getFileLineId().equals(otherUser.getFileLineId()) && !this.getUsername().equals(otherUser.getUsername())) {
            throw new IllegalArgumentException("Comparing users with the same file line id is not allowed to prevent introducing bugs.");
        }

        return this.username.compareTo(otherUser.getUsername());

    }

    @Override
    public String toString() {

        String fileLineIdStr = this.getFileLineId() == null ? "null" : this.getFileLineId().toString();

        return String.format("Username [%s] - Password [%s] - FileLineID [%s]", this.getUsername(), this.getPassword(), fileLineIdStr);

    }


}
