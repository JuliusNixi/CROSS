package CROSS.API.Responses;

import CROSS.API.JSON;
import CROSS.Enums.ResponseCode;

/**
 * ResponseOnUserData is a class.
 * This class is used to represent the response of a request that is about the user's data.
 * 
 * @version 1.0
 * @see JSON
 */
public class ResponseOnUserData extends JSON {
    
    private Integer response = null;
    private String errorMessage = null;

    /**
     * Constructor of the ResponseOnUserData class.
     * 
     * @param response The response code of the response.
     * @param errorMessage The error message of the response.
     * @throws NullPointerException If the response is null or the error message is null.
     */
    public ResponseOnUserData(ResponseCode response, String errorMessage) throws NullPointerException {
        if (response == null) {
            throw new NullPointerException("Response is null.");
        }
        if (errorMessage == null) {
            throw new NullPointerException("Error message is null.");
        }
        this.response = response.getCode();
        this.errorMessage = errorMessage;
    }

    // Cannot returns a response object because i don't know the type of the response.
    /**
     * Getter for the response code.
     * 
     * @return The response code of the response.
     */
    public Integer getResponse() {
        return response;
    }

    /**
     * Getter for the error message.
     * 
     * @return The error message of the response.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

}
