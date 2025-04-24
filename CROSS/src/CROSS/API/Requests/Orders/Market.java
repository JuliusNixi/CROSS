package CROSS.API.Requests.Orders;

/**
 * 
 * Market is a class that extends Generic and is used to submit a market order API request.
 * 
 * It is used to represent a request that is about the order's data.
 * 
 * The price is not present, since it's sent by the client and executed by the server at the best price.
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
     * @throws NullPointerException If the order is null.
     * 
     */
    public Market(CROSS.Orders.MarketOrder order) throws NullPointerException {

        super(order);

    }
    
}
