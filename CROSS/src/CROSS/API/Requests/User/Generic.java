package CROSS.API.Requests.User;

import CROSS.Users.User;

/**
 * 
 * Generic is an abstract class used as base to be extended by the client's specific requests about the user's data.
 * 
 * It is used to represent the requests that are about the user's data.
 * 
 * This class is not a concrete request, it's used to represent the common data of the requests about user's data.
 * It's extended by other classes that represent the concrete requests.
 * 
 * It contains the username of the user that the request is about.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Login
 * @see Register
 * @see Update
 * @see Logout
 * 
 * @see User
 * 
 */
public abstract class Generic {
    
    private final String username;

    /**
     * 
     * Constructor of the class.
     * 
     * @param user The user as User object.
     * 
     * @throws NullPointerException If the user is null.
     * 
     */
    public Generic(User user) throws NullPointerException {

        // Null check.
        if (user == null) {
            throw new NullPointerException("User to be used in the generic user's data request cannot be null.");
        }

        this.username = user.getUsername();

    }

    // GETTERS
    /**
     * 
     * Getter for the username.
     * 
     * @return The username as string.
     * 
     */
    public String getUsername() {
        
        return username;

    }

}
