package CROSS.Users;

/**
 * This class represents a User.
 * It implements the Comparable interface to allow sorting the users by username.
 * @version 1.0
 */
public class User implements Comparable<User> {
    
    private String username;
    private String password; 
    private Long fileLineId = null;

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

}
