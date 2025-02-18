package CROSS.API.Requests.User;

import CROSS.Users.User;

/**
 * 
 * Update is a class that extends Generic and is used to request an update of the user's data.
 * 
 * It is used to represent the request that is about the user's data.
 * 
 * It contains the old password and the new password of the user.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Generic
 * 
 * @see User
 * 
 */
public class Update extends Generic {
    
    // The current password of the user.
    private final String old_password;
    // The new password of the user to update the old one with.
    private final String new_password;

    /**
     * 
     * Constructor of the class.
     * 
     * @param updateUser The user to update as object.
     * @param new_password The new password of the user as string.
     * 
     * @throws NullPointerException If the new password is null.
     * @throws IllegalArgumentException If the new password is the same as the old password.
     * 
     */
    public Update(User updateUser, String new_password) throws NullPointerException, IllegalArgumentException {
        
        // Setting the username.
        super(updateUser);

        // Null check.
        if (new_password == null) {
            throw new NullPointerException("New password in the update credentials request cannot be null.");
        }

        // Check if the new password is the same as the old password.
        if (new_password.equals(updateUser.getPassword())) {
            throw new IllegalArgumentException("New password is the same as the old password in the update credentials request.");
        }

        this.old_password = updateUser.getPassword();
        this.new_password = new_password;

    }

    // GETTERS
    /**
     * 
     * Getter for the old user.
     * Used to get the user with the old password.
     * 
     * @return The unupdated user.
     * 
     */
    public User getOldUser() {
        return new User(super.getUsername(), this.old_password);
    }
    /**
     * 
     * Getter for the new user.
     * Used to get the user with the new password.
     * 
     * @return The updated user.
     * 
     */
    public User getNewUser() {
        return new User(super.getUsername(), this.new_password);
    }

}
