package CROSS.API.Requests.User;

import CROSS.Users.User;

/**
 * Register is a class that extends Generic and is used to request register data.
 * It is used to represent the request that is about the user's data.
 * It contains the user's password.
 * 
 * @version 1.0
 * @see Generic
 * @see User
 */
public class Register extends Generic {
    
    private String password = null;

    /**
     * Constructor of the Register class.
     * 
     * @param user The user.
     */
    public Register(User user) {
        super(user);
        this.password = user.getPassword();
    }

    /**
     * Getter for the user.
     * 
     * @return The user.
     */
    public User getUser() {
        return new User(super.getUsername(), password);
    }

}
