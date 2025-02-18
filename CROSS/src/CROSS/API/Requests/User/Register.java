package CROSS.API.Requests.User;

import CROSS.Users.User;

/**
 * 
 * Register is a class that extends Generic and is used to request client's registration.
 * 
 * It is used to represent the request that is about the user's data.
 * 
 * It contains the user's password.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Generic
 * 
 * @see User
 * 
 */
public class Register extends Generic {
    
    private final String password;

    /**
     * 
     * Constructor of the class.
     * 
     * @param user The user to register.
     * 
     */
    public Register(User user) {

        // Setting the username.
        super(user);

        this.password = user.getPassword();

    }

    /**
     * 
     * Getter for the user registered.
     * 
     * @return The registered user.
     * 
     */
    public User getUser() {
        return new User(super.getUsername(), this.password);
    }

}
