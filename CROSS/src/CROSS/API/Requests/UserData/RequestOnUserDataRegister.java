package CROSS.API.Requests.UserData;

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
     * @param username The username of the user.
     * @param password The password of the user.
     * @throws NullPointerException If the password is null.
     */
    public RequestOnUserDataRegister(String username, String password) throws NullPointerException {
        super(username);
        if (password == null) {
            throw new NullPointerException("Password is null.");
        }
        this.password = password;
    }

    /**
     * Getter for the password.
     * 
     * @return The password of the user.
     */
    public String getPassword() {
        return password;
    }

}
