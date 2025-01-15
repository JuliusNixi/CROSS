package CROSS.API.Requests.User;

import CROSS.API.Responses.ResponseCode.ResponseType;
import CROSS.Users.User;

/**
 * Register is a class that extends Generic and is used to request registration.
 * 
 * It is used to represent the request that is about the user's data.
 * 
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
     * @param user The user to register.
     */
    public Register(User user) {
        super(user);

        super.setOperation(ResponseType.REGISTER);

        this.password = user.getPassword();
    }

    /**
     * Getter for the user registered.
     * 
     * @return The registered user.
     */
    public User getUser() {
        return new User(super.getUsername(), this.password);
    }

    @Override
    public String toString() {
        return String.format("Password [%s] - %s", this.getUser().getPassword(), super.toString());
    }

}
