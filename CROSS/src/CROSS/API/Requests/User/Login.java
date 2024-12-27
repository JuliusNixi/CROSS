package CROSS.API.Requests.User;

import CROSS.Users.User;

/**
 * Login is a class that extends Generic and is used to request login data.
 * It is used to represent the request that is about the user's data.
 * It contains the user's password.
 * 
 * @version 1.0
 * @see Generic
 * @see User
 */
public class Login extends Generic {
    
    private String password = null;

    /**
     * Constructor of Login the class.
     * 
     * @param user The user.
     */
    public Login(User user) {
        super(user);
        this.password = user.getPassword();
    }

    /**
     * Returns the user.
     * 
     * @return The user.
     */
    public User getUser() {
        return new User(super.getUsername(), password);
    }
    
}
