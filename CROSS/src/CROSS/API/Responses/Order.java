package CROSS.API.Responses;

import CROSS.API.Responses.ResponseCode.ResponseType;

/**
 * Order is a class.
 * This class is used to represent the response of a request that is about the orders.
 * It extends the CancelOrderID (attention CancelOrderID is a request) class since the content is the same.
 * It's used to get the order ID.
 * 
 * @version 1.0
 * @see CROSS.API.Requests.Orders.CancelOrderID 
 * @see CROSS.Orders.Order
 */
public class Order extends CROSS.API.Requests.Orders.CancelOrderID {

    /**
     * Constructor of the Order class.
     * 
     * @param order The order to be used for the ID.
     */
    public <O extends CROSS.Orders.Order> Order(O order) {
        super(order);
        super.setOperation(ResponseType.ORDER_INFO);
    }

    // Getters defined in the super class.

}
