package CROSS.API.Requests.Orders;

/**
 * 
 * Cancel is a class used to submit a cancel order API request.
 * 
 * It is used to represent the request that is about the order's data.
 * 
 * It contains the order's id to cancel.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 */
public class Cancel {

    private final Integer orderId;

    /**
     * 
     * Constructor of the class.
     * 
     * @param orderId The order's ID to cancel.
     * 
     * @throws NullPointerException If the order's ID is null.
     * 
     */
    public Cancel(Integer orderId) throws NullPointerException {

        // Null check.
        if (orderId == null)
            throw new NullPointerException("The order's ID in the cancel request cannot be null.");

        this.orderId = orderId;

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

        return this.orderId;

    }

}
