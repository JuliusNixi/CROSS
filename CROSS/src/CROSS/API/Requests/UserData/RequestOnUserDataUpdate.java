package CROSS.API.Requests.UserData;

/**
 * RequestOnUserDataUpdate is a class that extends RequestOnUserData and is used to request an update of the user's data.
 * It is used to represent the request that is about the user's data.
 * 
 * @version 1.0
 * @see RequestOnUserData
 */
public class RequestOnUserDataUpdate extends RequestOnUserData {
    
    private String old_password;
    private String new_password;

    /**
     * Constructor of the RequestOnUserDataUpdate class.
     * 
     * @param username The username of the user.
     * @param old_password The old password of the user.
     * @param new_password The new password of the user.
     * @throws NullPointerException If the old_password or new_password are null.
     */
    public RequestOnUserDataUpdate(String username, String old_password, String new_password) throws NullPointerException {
        super(username);
        if (old_password == null) {
            throw new NullPointerException("Old password is null.");
        }
        if (new_password == null) {
            throw new NullPointerException("New password is null.");
        }
        this.old_password = old_password;
        this.new_password = new_password;
    }

    /**
     * Getter for the old password.
     * 
     * @return The old password.
     */
    public String getOldPassword() {
        return old_password;
    }
    /**
     * Getter for the new password.
     * 
     * @return The new password.
     */
    public String getNewPassword() {
        return new_password;
    }

}
