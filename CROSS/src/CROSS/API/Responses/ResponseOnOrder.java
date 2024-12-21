package CROSS.API.Responses;

/**
 * ResponseOnOrder is a class.
 * This class is used to represent the response of a request that is about the orders.
 * 
 * @version 1.0
 */
public class ResponseOnOrder {
    
    private Long orderId = null;

    /**
     * Constructor of the ResponseOnOrder class.
     * 
     * @param orderId The id of the order.
     * @throws NullPointerException If the orderId is null.
     */
    public ResponseOnOrder(Long orderId) throws NullPointerException {
        if (orderId == null) {
            throw new NullPointerException("orderId is null.");
        }
        this.orderId = orderId;
    }

    /**
     * Getter for the orderId.
     * 
     * @return The id of the order.
     */
    public Long getOrderId() {
        return orderId;
    }

}
