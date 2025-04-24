package CROSS.API.Responses.Orders;

import CROSS.API.Responses.ResponseCode;

/**
 * 
 * Cancel is a class used to respond to the requests about an order cancellation.
 * 
 * It is used to represent the responses that are about an order cancellation.
 * 
 * It's extend the User's Generic response class sice it has the same structure.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see CROSS.API.Responses.User.Generic
 * 
 */
public class Cancel extends CROSS.API.Responses.User.Generic {

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
    public Cancel(ResponseCode responseCode, String message) throws NullPointerException {

        super(responseCode, message);

    }

}
