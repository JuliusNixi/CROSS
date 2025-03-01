package CROSS.Users;

/**
 * 
 * This class represents an User.
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

    // Each user has a file line id, it's used to update the user in the database file without rewriting the whole file.
    // It indicates the line of the users database file where the user is stored.
    // This users database file line when an user is updated is overwritten (using the file line id) with blank spaces and then the updated (new) user is appended at the end of the users database file.
    private Long fileLineId = null;

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
        return String.format("%s", this.username);
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
        return String.format("%s", this.password);
    }
    /**
     * 
     * This method returns the file line id of the User.
     * The file line id is used to update the User in the users database file without rewriting the whole file.
     * 
     * @return The file line id of the User as a Long or null if it's not set.
     * 
     */
    public Long getFileLineId() {

        // Null check.
        if (this.fileLineId == null) {
            return null;
        }

        return Long.valueOf(this.fileLineId);

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
