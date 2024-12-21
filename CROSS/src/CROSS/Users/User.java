package CROSS.Users;

public class User implements Comparable<User> {
    
    private String username;
    private String password; 

    public User(String username, String password) throws NullPointerException {
        if (username == null || password == null) {
            throw new NullPointerException("Username or password is null.");
        }
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    // DISCLAIMER:
    // This method and the toString() method are used for debugging.
    // In a real application, the password should not be shown.
    // The authentication should be done without handling the text plain password.
    public String getPassword() {
        return this.password;
    }

    @Override
    public int compareTo(User o) {
        return this.username.compareTo(o.username);
    }

    @Override
    public String toString() {
        return String.format("Username [%s] - Password [%s]", this.username, this.password);
    }

}
