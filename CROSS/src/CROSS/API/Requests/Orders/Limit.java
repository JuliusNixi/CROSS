package CROSS.API.Requests.Orders;

/**
 * Limit is a class that extends Generic and is used to submit a limit order.
 * It is used to represent the request that is about the order's data.
 * 
 * It contains the order's price.
 * 
 * @version 1.0
 * @see Generic
 * @see CROSS.Orders.Order
 */
public class Limit extends Generic {

    private Integer price;

    /**
     * Constructor of the Limit class.
     * 
     * @param order The order to get the price from.
     */
    public <O extends CROSS.Orders.Order> Limit(O order) {
        super(order);
        
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
