package CROSS.API.Requests.Orders;

import CROSS.Orders.LimitOrder;
import CROSS.Types.Price.GenericPrice;

/**
 * 
 * Limit is a class that extends Generic and is used to submit a limit order API request.
 * 
 * It is used to represent the request that is about the order's data.
 * 
 * It contains the order's price.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Generic
 * 
 * @see CROSS.Orders.LimitOrder
 * 
 * @see CROSS.Types.Price.GenericPrice
 * 
 */
public class Limit extends Generic {

    private final Integer price;

    /**
     * 
     * Constructor of the class.
     * 
     * @param order The limit order to get the price from.
     * 
     */
    public Limit(LimitOrder order) {

        super(order);

        this.price = order.getPrice().getValue();

    }

    /**
     * 
     * Getter of the price.
     * 
     * @return The price of the order as GenericPrice.
     * 
     */
    public GenericPrice getPrice() {
        return new GenericPrice(this.price);
    }

}
