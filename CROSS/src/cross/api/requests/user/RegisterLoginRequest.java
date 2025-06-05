package cross.api.requests.user;

import cross.users.User;
import cross.api.requests.Request;

/**
 *
 * RegisterLoginRequest is a class used to rapresent the client's API requests about login and register.
 *
 * It contains the username and the password of the user that the request is about.
 *
 * It's used as values in the Request object.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see User
 *
 * @see Request
 *
 */
public class RegisterLoginRequest {

    private final String username;
    // Not final and protected (instead of private) to be modified by the UpdateCredentialsRequest constructor.
    protected String password;

    /**
     *
     * Constructor of the class.
     *
     * @param user The user as User object.
     *
     * @throws NullPointerException If the user is null.
     *
     */
    public RegisterLoginRequest(User user) throws NullPointerException {

        // Null check.
        if (user == null) {
            throw new NullPointerException("User to be used in the register / login request cannot be null.");
        }

        this.username = user.getUsername();
        this.password = user.getPassword();

    }

    // GETTERS
    /**
     *
     * Getter for the user.
     *
     * @return The user as User object.
     *
     */
    public User getUser() {

        if (password == null) {
            return new User(username, "placeholder");
        }
        return new User(username, password);

    }

}
