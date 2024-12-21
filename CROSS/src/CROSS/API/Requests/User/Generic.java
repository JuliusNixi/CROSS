package CROSS.API.Requests.User;

import CROSS.API.JSON;
import CROSS.Users.User;

/**
 * Generic is an abstract class.
 * It is used to represent the request that is about the user's data.
 * This class is not a concrete request, but an abstract one, used to represent the common data of the requests.
 * It's extended by other classes that represent the concrete requests.
 * It contains the username of the user that the request is about.
 * 
 * @version 1.0
 * @see Login
 * @see Register
 * @see Update
 * @see JSON
 * @see User
 */
public abstract class Generic extends JSON {
    
    // Protected to be accessed by the subclasses.
    protected String username = null;

    /**
     * Constructor of the Generic class.
     * 
     * @param user The user.
     * @throws NullPointerException If the user is null.
     */
    public Generic(User user) throws NullPointerException {
        if (user == null) {
            throw new NullPointerException("User is null.");
        }
        this.username = user.getUsername();
    }

}
