package cross.api.requests.user;

import cross.users.User;
import cross.api.requests.Request;

/**
 *
 * UpdateCredentialsRequest is a class that extends RegisterLoginRequest and is used by the client to request to the API an update of the user's data (password).
 *
 * It adds the old password and the new password of the user to update the old one with the new one.
 * 
 * It's used as values in the Request object.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see RegisterLoginRequest
 *
 * @see User
 * 
 * @see Request
 *
 */
public class UpdateCredentialsRequest extends RegisterLoginRequest {

    // The username of the user is contained in the RegisterLoginRequest with the field's name "username".

    // The old password of the user to update it with the new one.
    private final String old_password;

    // The new password of the user to update the old one with.
    private final String new_password;

    /**
     *
     * Constructor of the class.
     * 
     * The old user is the user with the old password.
     * The new user is the user with the new password, the old one must be updated with the new one.
     *
     * @param userOld The old user as User object.
     * @param userNew The new user as User object.
     *
     * @throws NullPointerException If the old user or the new user are null.
     * @throws IllegalArgumentException If the new user username is different from the old user username.
     *
     */
    public UpdateCredentialsRequest(User userOld, User userNew) throws NullPointerException, IllegalArgumentException {

        // To set the username.
        super(userOld);
        
        // To remove the old password field with the wrong name.
        // null will not be serialized by Gson in the JSON request.
        super.password = null;

        // Null check.
        if (userNew == null) {
            throw new NullPointerException("The new user to use to update credentials of the old one in the corresponding request cannot be null.");
        }

        if (userOld.getUsername().compareToIgnoreCase(userNew.getUsername()) != 0) {
            throw new IllegalArgumentException("The new user username to use to send an update credentials request cannot be different from the old user username, only the password can be changed.");
        }

        // To set the old password.
        this.old_password = userOld.getPassword();

        // To set the new password.
        this.new_password = userNew.getPassword();

    }
    
    // GETTERS
    // The new user, the updated one.
    /**
     *
     * Getter for the new user.
     * 
     * Used to get the user with the new password.
     *
     * @return The updated user as User object.
     *
     */
    @Override
    public User getUser() {

        return new User(super.getUser().getUsername(), new_password);

    }

    /**
     *
     * Getter of the old user.
     * 
     * The old user is the user with the old password.
     *
     * @return The old user as User object.
     *
     */
    public User getUserOld() {

        return new User(super.getUser().getUsername(), old_password);

    }

}
