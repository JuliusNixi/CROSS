package CROSS.API.Requests.Order;

import CROSS.API.JSON;

/**
 * Cancel is a class that extends JSON and is used to request orders data.
 * It is used to represent the request that is about the order's data.
 * It contains the order ID.
 * 
 * @version 1.0
 * @see JSON
 */
public class Cancel extends JSON {
    
    private Long orderId;

    /**
     * Constructor of the class.
     * 
     * @param orderId the order ID.
     * @throws IllegalArgumentException if the order ID is null.
     */
    public Cancel(Long orderId) throws IllegalArgumentException {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null.");
        }
        this.orderId = orderId;
    }

    /**
     * Getter of the order ID.
     * 
     * @return the order ID.
     */
    public Long getOrderId() {
        return orderId;
    }

}
