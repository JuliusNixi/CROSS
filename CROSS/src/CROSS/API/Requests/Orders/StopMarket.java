package CROSS.API.Requests.Orders;

import CROSS.Orders.StopMarketOrder;
import CROSS.Types.Price.GenericPrice;

/**
 * 
 * StopMarket is a class that extends Generic and is used to submit a stop market order API request.
 * 
 * It is used to represent a request that is about the order's data.
 * 
 * It contains the order's price.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Generic
 * 
 * @see CROSS.Orders.StopMarketOrder
 * 
 * @see CROSS.Types.Price.GenericPrice
 * 
 */
public class StopMarket extends Generic {

    private final Integer price;

    /**
     * 
     * Constructor of the class.
     * 
     * @param order The stop market order to get the price from.
     * 
     * @throws NullPointerException If the order is null.
     * 
     */
    public StopMarket(StopMarketOrder order) throws NullPointerException {

        super(order);

        this.price = order.getPrice().getValue();

    }

    /**
     * 
     * Getter of the price.
     * 
     * @return The price of the order as GenericPrice object.
     * 
     */
    public GenericPrice getPrice() {

        return new GenericPrice(this.price);

    }

}
