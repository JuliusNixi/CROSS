package CROSS.API.Requests.UserData;

import CROSS.Users.User;

/**
 * RequestOnUserDataLogin is a class that extends RequestOnUserData and is used to request login data.
 * It is used to represent the request that is about the user's data.
 * It contains the user's password.
 * 
 * @version 1.0
 * @see RequestOnUserData
 */
public class RequestOnUserDataLogin extends RequestOnUserData {
    
    private String password = null;

    /**
     * Constructor of RequestOnUserDataLogin the class.
     * 
     * @param user The user.
     */
    public RequestOnUserDataLogin(User user) {
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
