package CROSS.API.Responses;

import CROSS.API.JSON;
import CROSS.API.RequestResponse;

/**
 * 
 * ResponseAndMessage is used to represent the response of a request.
 * 
 * It's include a response code and a message.
 * 
 * It's extends the JSON class.
 * 
 * It's used the ResponseCode class as core.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see RequestResponse
 * @see JSON
 * 
 */
public class ResponseAndMessage extends JSON {
    
    private Integer code = null;
    private String errorMessage = null;

    /**
     * Constructor of the ResponseAndMessage class.
     * 
     * If the errorMessage is empty, the error message is set to the name of the response content.
     * 
     * @param code The code of the response.
     * @param errorMessage The message of the response.
     * 
     * @throws NullPointerException If the code or the message are null.
     */
    public ResponseAndMessage(RequestResponse code, String errorMessage) throws NullPointerException {
        
        // Null check.
        if (code == null) {
            throw new NullPointerException("Code of the response cannot be null.");
        }
        if (errorMessage == null) {
            throw new NullPointerException("Error message of the response cannot be null.");
        }

        super(code.getType());

        this.code = code.getCode();
        this.errorMessage = errorMessage.trim();

        // A shortcut to set the error message if it's empty.
        if (this.errorMessage.isEmpty()) {
            this.errorMessage = code.getResponseContent().name().replace("_", " ").toLowerCase();
        }
    }

    // GETTERS
    /**
     * Getter for the errorMessage of the response.
     * 
     * @return The errorMessage of the response as a String.
     */
    public String getErrorMessage() {
        return String.format("%s", this.errorMessage);
    }
    // NB: The type of the response is lost.
    /**
     * Getter for the code of the response.
     * 
     * @return The code of the response.
     */
    public Integer getResponseCode() {
        return Integer.valueOf(this.code);
    }

    @Override
    public String toString() {
        return String.format("Code [%s] - Message [%s]", this.getResponseCode(), this.getErrorMessage());
    }

}
