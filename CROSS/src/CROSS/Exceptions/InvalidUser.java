package CROSS.Exceptions;

/**
 * 
 * Exception for invalid user. 
 * It's throwed, for example, when adding an user that already exists in the users database.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Exception
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
