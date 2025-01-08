package CROSS.API.Requests.User;

import CROSS.Users.User;

/**
 * Update is a class that extends Generic and is used to request an update of the user's data.
 * 
 * It is used to represent the request that is about the user's data.
 * 
 * It contains the old password and the new password of the user.
 * 
 * @version 1.0
 * @see Generic
 * @see User
 */
public class Update extends Generic {
    
    private String old_password;
    private String new_password;

    /**
     * Constructor of the Update class.
     * 
     * @param updateUser The user to update.
     * @param new_password The new password of the user as string.
     * @throws NullPointerException If the new_password is null.
     * @throws IllegalArgumentException If the new_password is the same as the old password.
     */
    public Update(User updateUser, String new_password) throws NullPointerException, IllegalArgumentException {
        super(updateUser);

        if (new_password == null) {
            throw new NullPointerException("New password is null.");
        }

        if (new_password.equals(old_password)) {
            throw new IllegalArgumentException("New password is the same as the old password.");
        }

        this.old_password = updateUser.getPassword();
        this.new_password = new_password;
    }

    // GETTERS
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

    @Override
    public String toString() {
        return String.format("Old User [%s] - New User [%s] - %s", this.getOldUser(), this.getNewUser(), super.toString());
    }

}
