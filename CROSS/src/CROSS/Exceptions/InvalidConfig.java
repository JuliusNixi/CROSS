package CROSS.Exceptions;

/**
 * Exception for invalid configuration file server or client.
 * It's throwed for example when the IP or port are invalid (readed from config file) for server or client.
 * 
 * @version 1.0
 * @see Exception
 */
public class InvalidConfig extends Exception {

    /**
     * Constructor of the exception.
     * 
     * @param message Message of the exception.
     */
    public InvalidConfig(String message) {
        super(message);
    }   

}
