package CROSS.API.Responses.User;

import CROSS.API.Responses.ResponseCode;

/**
 * 
 * Generic is a class used to respond to the requests about the user's data.
 * 
 * It is used to represent the responses that are about the user's data.
 * 
 * It contains the status code (number) and the message (string) to be sent to the client.
 * 
 * It uses the ResponseCode class to map the response code to the type of response and its content.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see ResponseCode
 * 
 */
public class Generic {
    
    // The status code of the response.
    private final Integer response;
    private final String message;

    /**
     * 
     * Constructor of the class.
     * 
     * @param responseCode The response code object used to extract the status code.
     * @param message The message string to be sent to the client.
     * 
     * @throws NullPointerException If the response code or the message are null.
     * 
     */
    public Generic(ResponseCode responseCode, String message) throws NullPointerException {

        // Null check.
        if (responseCode == null) {
            throw new NullPointerException("The response code in the user's data response cannot be null.");
        }
        if (message == null) {
            throw new NullPointerException("The message string in the user's data response cannot be null.");
        }

        this.response = responseCode.getCode();
        this.message = message;

    }

    // GETTERS
    /**
     * 
     * Getter for the message.
     * 
     * @return The message as string.
     * 
     */
    public String getMessage() {

        return this.message;

    }
    /**
     * 
     * Getter for the response code.
     * 
     * @return The response code as an Integer.
     * 
     */
    public Integer getResponseCode() {

        return this.response;

    }

}

