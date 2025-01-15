package CROSS.API.Requests.User;

import CROSS.API.JSON;
import CROSS.Users.User;

/**
 * Generic is an abstract class.
 * 
 * It is used to represent the requests that are about the user's data.
 * This class is not a concrete request, but an abstract one, used to represent the common data of the requests about users.
 * It's extended by other classes that represent the concrete requests.
 * 
 * It contains the username of the user that the request is about.
 * 
 * It extends the JSON class, so it can be converted to a JSON string.
 * 
 * @version 1.0
 * @see Login
 * @see Register
 * @see Update
 * 
 * @see JSON
 * @see User
 */
public abstract class Generic extends JSON {
    
    private String username = null;

    /**
     * Constructor of the Generic class.
     * 
     * @param user The user.
     * @throws NullPointerException If the user is null.
     */
    public Generic(User user) throws NullPointerException {
        if (user == null) {
            throw new NullPointerException("User cannot be null.");
        }

        super(null);

        this.username = user.getUsername();
    }

    /**
     * Getter for the username.
     * 
     * @return The username as string.
     */
    public String getUsername() {
        return String.format("%s", this.username);
    }

    @Override
    public String toString() {
        return String.format("Username [%s]", this.getUsername());
    }

}
