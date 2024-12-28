package CROSS.Users;

/**
 * This class represents a User.
 * It implements the Comparable interface to allow sorting the users by username.
 * @version 1.0
 */
public class User implements Comparable<User> {
    
    private String username;
    private String password; 

    /**
     * This constructor creates a User with a username and a password.
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
        username = username.trim();
        password = password.trim();
        username = username.replaceAll("\\s+", " ");
        password = password.replaceAll("\\s+", " ");
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
        return this.username;
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
        return this.password;
    }

    @Override
    public int compareTo(User o) {
        return this.username.compareTo(o.getUsername());
    }

    @Override
    public String toString() {
        return String.format("Username [%s] - Password [%s]", this.getUsername(), this.getPassword());
    }

}
