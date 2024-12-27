package CROSS.API.Responses;

import CROSS.API.JSON;

/**
 * Order is a class.
 * This class is used to represent the response of a request that is about the orders.
 * It contains the order id.
 * 
 * @version 1.0
 * @see JSON
 */
public class Order extends JSON {
    
    private Long orderId = null;

    /**
     * Constructor of the Order class.
     * 
     * @param orderId The orderId of the order.
     * @throws NullPointerException If the order id is null.
     */
    public Order(Long orderId) throws NullPointerException {
        if (orderId == null) {
            throw new NullPointerException("The orderId is null.");
        }
        this.orderId = orderId;
    }

    /**
     * Getter for the orderId.
     * 
     * @return The orderId.
     */
    public Long getOrderId() {
        return orderId;
    }

}
