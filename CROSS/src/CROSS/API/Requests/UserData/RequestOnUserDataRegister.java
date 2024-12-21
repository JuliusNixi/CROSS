package CROSS.API.Requests.UserData;

import CROSS.Users.User;

/**
 * RequestOnUserDataRegister is a class that extends RequestOnUserData and is used to request register data.
 * It is used to represent the request that is about the user's data.
 * It contains the user's password.
 * 
 * @version 1.0
 * @see RequestOnUserData
 */
public class RequestOnUserDataRegister extends RequestOnUserData {
    
    private String password = null;

    /**
     * Constructor of the RequestOnUserData class.
     * 
     * @param user The user.
     */
    public RequestOnUserDataRegister(User user) {
        super(user);
        this.password = user.getPassword();
    }

    /**
     * Getter for the user.
     * 
     * @return The user.
     */
    public User getUser() {
        return new User(getUsername(), password);
    }

}
