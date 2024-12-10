package CROSS.Users;

public class User implements Comparable<User> {
    
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    @Override
    public int compareTo(User o) {
        return this.username.compareTo(o.username);
    }

    @Override
    public String toString() {
        return "User [username=" + username + ", password=" + password + "]";
    }

}
