package CROSS.API.Requests.UserData;

import CROSS.Users.User;

/**
 * RequestOnUserDataUpdate is a class that extends RequestOnUserData and is used to request an update of the user's data.
 * It is used to represent the request that is about the user's data.
 * It contains the old password and the new password of the user.
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
     * @param user The user.
     * @param new_password The new password of the user as string.
     * @throws NullPointerException If the new_password is null.
     */
    public RequestOnUserDataUpdate(User user, String new_password) throws NullPointerException {
        super(user);
        if (new_password == null) {
            throw new NullPointerException("New password is null.");
        }
        this.old_password = user.getPassword();
        this.new_password = new_password;
    }

    /**
     * Getter for the old user.
     * Used to get the user with the old password.
     * 
     * @return The unupdated user.
     */
    public User getOldUser() {
        return new User(super.getUsername(), this.old_password);
    }
    /**
     * Getter for the new user.
     * Used to get the user with the new password.
     * 
     * @return The updated user.
     */
    public User getNewUser() {
        return new User(super.getUsername(), this.new_password);
    }

}
