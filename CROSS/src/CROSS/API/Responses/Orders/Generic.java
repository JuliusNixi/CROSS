package CROSS.API.Responses.Orders;

import CROSS.Orders.Order;

/**
 * 
 * Generic is a class used to respond to the requests about the orders's data.
 * 
 * It is used to represent the responses that are about the orders's data.
 * 
 * It contains the order's id as positive Integer or -1 if there was an error.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Order
 * 
 */
public class Generic {
    
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
    public Generic(Order order) throws NullPointerException {

        // Null check.
        if (order == null) {
            throw new NullPointerException("The order in the orders' data response cannot be null.");
        }

        this.orderId = order.getId();

    }

    // GETTERS
    /**
     * 
     * Getter for the order's id.
     * 
     * @return The order's id as an Integer.
     * 
     */
    public Integer getOrderId() {
        return Integer.valueOf(this.orderId);
    }

}
