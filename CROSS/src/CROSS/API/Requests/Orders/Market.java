package CROSS.API.Requests.Orders;

/**
 * 
 * Market is a class that extends Generic and is used to submit a market order API request.
 * 
 * It is used to represent the request that is about the order's data.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Generic
 * 
 * @see CROSS.Orders.MarketOrder
 * 
 */
public class Market extends Generic {

    /**
     * 
     * Constructor of the class.
     * 
     * @param order The market order to get the price from.
     * 
     */
    public Market(CROSS.Orders.MarketOrder order) {
        super(order);
    }
    
}
