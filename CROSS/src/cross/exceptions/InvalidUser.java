package cross.exceptions;

/**
 *
 * Exception for invalid user.
 * It's throwed, for example, when adding an user that already exists in the users database.
 * It's also throwed when the password provided does not match the one in the users database during the login or the credentials update process.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Exception
 *
 * @see cross.users.db.Users
 *
 */
public class InvalidUser extends Exception {
    
    /**
     *
     * Constructor of the exception.
     *
     * @param message Message of the exception.
     *
     */
    public InvalidUser(String message) {

        super(message);

    }

}
