package CROSS.API.Requests.User;

import CROSS.Users.User;

/**
 * 
 * Login is a class that extends Generic and is used to request login.
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
public class Login extends Generic {
    
    private final String password;

    /**
     * 
     * Constructor of the class.
     * 
     * @param user The user object.
     * 
     * @throws NullPointerException If the user is null.
     * 
     */
    public Login(User user) throws NullPointerException {

        // To set the username.
        super(user);

        this.password = user.getPassword();

    }

    // GETTERS
    /**
     * 
     * Getter for the user.
     * 
     * @return The user as User object.
     * 
     */
    public User getUser() {

        return new User(super.getUsername(), this.password);
        
    }
    
}
