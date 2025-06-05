package cross.api.responses.orders;

import cross.api.responses.ResponseCode;
import cross.api.responses.user.UserResponse;

/**
 *
 * CancelResponse is a class used by the server to respond to the client's API requests about an order cancellation.
 *
 * It extends the UserResponse response class since it has the same structure.
 * 
 * It's used as object in the Response object.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see UserResponse
 * 
 * @see Response
 *
 */
public class CancelResponse extends UserResponse {
    
    /**
     *
     * Constructor of the class.
     *
     * @param responseCode The response code object used to extract the status code.
     * @param message The message to be sent to the client.
     *
     * @throws NullPointerException If the response code or the message are null.
     *
     */
    public CancelResponse(ResponseCode responseCode, String message) throws NullPointerException {

        super(responseCode, message);

    }


}
