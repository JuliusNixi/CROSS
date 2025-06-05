package cross.api.requests.orders;

import cross.orders.Order;

/**
 * 
 * CancelRequest is a class used to submit a cancel order API request from the client.
 * 
 * It contains the order's id to cancel.
 * 
 * It's used as values in the Request object.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Order
 * 
 * @see Request
 * 
 */
public class CancelRequest {

    // From client can be only a positive number, but Number (and not Long) to be extended by the ExecutionResponse.
    private final Number orderId;

    // CONSTRUCTORS
    /**
     * 
     * Constructor of the class.
     * 
     * @param order The order from which to get the order's ID to cancel.
     * 
     * @throws NullPointerException If the order is null.
     * 
     */
    public CancelRequest(Order order) throws NullPointerException {

        // Null check.
        if (order == null)
            throw new NullPointerException("The order in the cancel order request cannot be null.");

        this.orderId = order.getId();

    }
    /**
     * 
     * Alternative constructor used in the ClientCLICommandParser class when the other order's attributes are unknown.
     * 
     * @param orderId The order's id to cancel.
     * 
     * @throws NullPointerException If the order's id is null.
     * 
     */
    public CancelRequest(Number orderId) throws NullPointerException {

        // Null check.
        if (orderId == null)
            throw new NullPointerException("The order's id in the cancel order request cannot be null.");

        this.orderId = orderId;

    }
    
    // GETTERS
    /**
     * 
     * Getter for the order's id.
     * 
     * @return The order's id as a Number.
     * 
     */
    public Number getOrderId() {

        return this.orderId;

    }

}
