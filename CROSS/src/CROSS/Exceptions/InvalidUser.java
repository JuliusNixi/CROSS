package CROSS.Exceptions;

/**
 * Exception for invalid user. 
 * It's throwed for example when a user is not found in the database.
 * 
 * @version 1.0
 * @see Exception
 */
public class InvalidUser extends Exception {

    /**
     * Constructor of the exception.
     * 
     * @param message Message of the exception.
     */
    public InvalidUser(String message) {
        super(message);
    }

}
