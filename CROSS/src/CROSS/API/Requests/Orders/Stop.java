package CROSS.API.Requests.Orders;

import CROSS.API.Responses.ResponseCode.ResponseType;

/**
 * Stop is a class that extends Generic and is used to submit a stop order.
 * It is used to represent the request that is about the order's data.
 * 
 * It contains the price.
 * 
 * @version 1.0
 * @see Generic
 * @see CROSS.Orders.Order
 */
public class Stop extends Generic {
    
    private Integer price;

    /**
     * Constructor of the Stop class.
     * 
     * @param order The stop order that is going to be submitted.
     */
    public <O extends CROSS.Orders.Order> Stop(O order) {
        super(order);

        super.setOperation(ResponseType.INSERT_STOP_ORDER);
        
        this.price = order.getPrice().getValue();
    }

    /**
     * Getter of the price.
     * 
     * @return The price of the order.
     */
    public Integer getPrice() {
        return Integer.valueOf(this.price);
    }
    
    @Override
    public String toString() {
        return String.format("Price [%s] - %s", this.getPrice(), super.toString());
    }

}
