package cross.api.responses.user;

import cross.api.responses.ResponseCode;
import cross.api.responses.ResponseCode.ResponseContent;
import cross.api.responses.ResponseCode.ResponseType;

/**
 *
 * UserResponse is a class used by the server to respond to the client's API requests about the user's data.
 *
 * It contains the status code (integer) and the message (string) to be sent to the client.
 *
 * It uses the ResponseCode class to map the response code to the type of response and its content.
 * 
 * It's used as object in the Response object.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see ResponseCode
 * 
 * @see ResponseType
 * @see ResponseContent
 * 
 * @see Response
 *
 */
public class UserResponse {

    // "response" is the status code of the response extracted from the response code object.
    private final Integer response;
    private final String errorMessage;

    // To wrap back the response code as Integer in a responde code object in the getter getResponseCode() method.
    // Transient to avoid serialization by Gson in JSON.
    private final transient ResponseType responseType;
    private final transient ResponseContent responseContent;

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
    public UserResponse(ResponseCode responseCode, String message) throws NullPointerException {

        // Null check.
        if (responseCode == null) {
            throw new NullPointerException("The response code in the user's data response cannot be null.");
        }
        if (message == null) {
            throw new NullPointerException("The message string in the user's data response cannot be null.");
        }

        this.response = responseCode.getCode();
        // Just to remove the unused warning.
        if (this.response == null);
        
        this.errorMessage = message;

        this.responseType = responseCode.getType();
        this.responseContent = responseCode.getResponseContent();

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

        return String.format("%s", this.errorMessage);

    }
    /**
     *
     * Getter for the response code.
     *
     * @return The response code as a ResponseCode object.
     *
     */
    public ResponseCode getResponseCode() {

        return new ResponseCode(this.responseType, this.responseContent);

    }

}
