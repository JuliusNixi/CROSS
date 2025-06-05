package cross.exceptions;

/**
 *
 * Exception for invalid configuration file of the server or the client.
 * It's throwed, for example, when the configuration file doesn't end with the .properties extension.
 * It's also throwed when the required properties are not found in the configuration file.
 * 
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Exception
 * @see Server
 * @see Client
 *
 */
public class InvalidConfig extends Exception {

    /**
     *
     * Constructor of the InvalidConfig exception.
     *
     * @param message Message of the exception.
     *
     */
    public InvalidConfig(String message) {

        super(message);

    }
    
}