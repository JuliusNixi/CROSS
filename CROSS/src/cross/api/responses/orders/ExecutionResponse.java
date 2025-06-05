package cross.api.responses.orders;

import cross.api.requests.orders.CancelRequest;
import cross.orders.Order;

/**
 *
 * ExecutionResponse is a class used by the server to respond to the client's requests about the orders's creation.
 *
 * It extends the CancelRequest class, since the response returns the order's id as a Number.
 * 
 * It's used as object in the Response object.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Order
 * 
 * @see CancelRequest
 * 
 * @see Response
 *
 */
public class ExecutionResponse extends CancelRequest {

    // CONSTRUCTORS
    /**
     *
     * Constructor of the class.
     *
     * @param order The order to get the id from.
     *
     * @throws NullPointerException If the order is null.
     *
     */
    public ExecutionResponse(Order order) throws NullPointerException {

        super(order);

    }
     /**
     * 
     * Alternative constructor used in the Response to parse the response object from a JSON string.
     * 
     * @param orderId The order's id.
     * 
     * @throws NullPointerException If the order's id is null.
     * 
     */
    public ExecutionResponse(Number orderId) throws NullPointerException {

        super(orderId);

    }

}
