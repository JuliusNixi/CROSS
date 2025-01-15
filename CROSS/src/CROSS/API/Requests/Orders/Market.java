package CROSS.API.Requests.Orders;

import CROSS.API.Responses.ResponseCode.ResponseType;

/**
 * Market is a class that extends Generic and is used to submit a market order.
 * It is used to represent the request that is about the order's data.
 * 
 * @version 1.0
 * @see Generic
 * @see CROSS.Orders.Order
 */
public class Market extends Generic {

    /**
     * Constructor of the Market class.
     * 
     * @param order The order to be submitted.
     */
    public <O extends CROSS.Orders.Order> Market(O order) {
        super(order);
        super.setOperation(ResponseType.INSERT_MARKET_ORDER);
    }

    // Getters defined in the super class.

}
