package CROSS.API.Responses;

import CROSS.API.JSON;
import CROSS.API.ResponseCode;

/**
 * ResponseAndMessage is a class.
 * This class is used to represent the response of a request.
 * It's include a code and a message.
 * 
 * @version 1.0
 * @see ResponseCode
 * @see JSON
 */
public class ResponseAndMessage extends JSON {
    
    private Integer code = null;
    private String errorMessage = null;

    /**
     * Constructor of the ResponseAndMessage class.
     * 
     * @param code The code of the response.
     * @param errorMessage The message of the response.
     * @throws NullPointerException If the code or the errorMessage are null.
     */
    public ResponseAndMessage(ResponseCode code, String errorMessage) throws NullPointerException {
        if (code == null) {
            throw new NullPointerException("code of the response is null.");
        }
        if (errorMessage == null) {
            throw new NullPointerException("errorMessage of the response is null.");
        }
        this.code = code.getCode();
        this.errorMessage = errorMessage;
    }


    /**
     * Getter for the errorMessage of the response.
     * 
     * @return The errorMessage of the response.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    // NB: The type of the response is lost.
    /**
     * Getter for the code of the response.
     * 
     * @return The code of the response.
     */
    public Integer getResponseCode() {
        return code;
    }

}
