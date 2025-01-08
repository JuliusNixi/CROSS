package CROSS.API.Requests.Orders;

import CROSS.API.JSON;

/**
 * CancelOrderID is a class that extends JSON and is used to cancel an order by its id.
 * It is used to represent the request that is about the order's data.
 * 
 * It contains the order ID.
 * 
 * @version 1.0
 * @see JSON
 * @see CROSS.Orders.Order
 */
public class CancelOrderID extends JSON {
    
    // Integer because could be -1.
    private Integer orderId;

    /**
     * Constructor of the class.
     * 
     * @param order The order to get the ID from.
     * @throws IllegalArgumentException If the order is null.
     */
    public <O extends CROSS.Orders.Order> CancelOrderID(O order) throws IllegalArgumentException {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }

        this.orderId = order.getId();
    }
    /**
     * Constructor of the class.
     * 
     * @param orderId The order ID.
     * @throws NullPointerException If the order ID is null.
     * @throws IllegalArgumentException If the order ID is negative.
     */
    public CancelOrderID(Integer orderId) throws NullPointerException, IllegalArgumentException {
        if (orderId == null) {
            throw new NullPointerException("Order ID cannot be null.");
        }

        if (orderId < 0) {
            throw new IllegalArgumentException("Order ID cannot be negative.");
        }
        
        this.orderId = orderId;
    }

    /**
     * Getter of the order ID.
     * 
     * @return The order ID as Long.
     */
    public Integer getOrderId() {
        return Integer.valueOf(this.orderId);
    }

    @Override
    public String toString() {
        return String.format("Order ID [%s]", this.getOrderId());
    }

}
