package CROSS.API.Requests.Orders;

import CROSS.Orders.Order;

/**
 * 
 * Cancel is a class used to submit a cancel order API request.
 * 
 * It is used to represent the request that is about the order's data.
 * 
 * It contains the order's id.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Order
 * 
 */
public class Cancel {

    private final Integer orderId;

    /**
     * 
     * Constructor of the class.
     * 
     * @param order The order to get the id from.
     * 
     * @throws NullPointerException If the order is null.
     * 
     */
    public Cancel(Order order) throws NullPointerException {

        // Null check.
        if (order == null)
            throw new NullPointerException("The order in the cancel request cannot be null.");

        this.orderId = order.getId();

    }

    // GETTERS
    /**
     * 
     * Getter for the order's id.
     * 
     * @return The order's id as Integer.
     * 
     */
    public Integer getOrderId() {

        return Integer.valueOf(this.orderId);
    }

}
