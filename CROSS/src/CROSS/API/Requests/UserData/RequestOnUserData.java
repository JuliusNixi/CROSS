package CROSS.API.Requests.UserData;

/**
 * RequestOnUserData is an abstract class.
 * It is used to represent the request that is about the user's data.
 * This class is not a concrete request, but an abstract one, used to represent the common data of the requests.
 * It contains the username of the user that the request is about.
 * 
 * @version 1.0
 * @see RequestOnUserDataLogin
 * @see RequestOnUserDataRegister
 * @see RequestOnUserDataUpdate
 */
public abstract class RequestOnUserData extends CROSS.API.JSON {
    
    private String username = null;

    /**
     * Constructor of the RequestOnUserData class.
     * 
     * @param username The username of the user.
     * @throws NullPointerException If the username is null.
     */
    public RequestOnUserData(String username) throws NullPointerException {
        if (username == null) {
            throw new NullPointerException("Username is null.");
        }
        this.username = username;
    }

    /**
     * Getter for the username.
     * 
     * @return The username of the user.
     */
    public String getUsername() {
        return username;
    }
 
}
