package CROSS.API.Requests.User;

import CROSS.API.RequestResponse.ResponseType;
import CROSS.Users.User;

/**
 * Login is a class that extends Generic and is used to request login.
 * 
 * It is used to represent the request that is about the user's data.
 * 
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

        super.setOperation(ResponseType.LOGIN);

        this.password = user.getPassword();
    }

    /**
     * Getter for the user.
     * 
     * @return The user.
     */
    public User getUser() {
        return new User(super.getUsername(), this.password);
    }
    
    @Override
    public String toString() {
        return String.format("Password [%s] - %s", this.getUser().getPassword(), super.toString());
    }

}
