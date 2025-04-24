package CROSS.Exceptions;

/**
 * 
 * Exception for invalid configuration file of the server or the client.
 * It's throwed, for example, when the IP or port are invalid (readed from the corresponding configuration file) for the server or the client.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Exception
 * 
 */
public class InvalidConfig extends Exception {

    /**
     * 
     * Constructor of the exception.
     * 
     * @param message Message of the exception.
     * 
     */
    public InvalidConfig(String message) {

        super(message);
        
    }   

}
