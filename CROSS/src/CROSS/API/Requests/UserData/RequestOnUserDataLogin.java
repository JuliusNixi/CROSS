package CROSS.API.Requests.UserData;

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
     * @param username The username of the user.
     * @param password The password of the user.
     * @throws NullPointerException If the password is null.
     */
    public RequestOnUserDataLogin(String username, String password) throws NullPointerException {
        super(username);
        if (password == null) {
            throw new NullPointerException("Password is null.");
        }
        this.password = password;
    }

    /**
     * Returns the password of the user.
     * 
     * @return The password of the user.
     */
    public String getPassword() {
        return password;
    }
    
}
