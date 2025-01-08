package CROSS.API.Requests.Orders;

import CROSS.API.JSON;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;

/**
 * Generic is an abstract class.
 * 
 * It is used to represent the requests that are about the orders.
 * 
 * This class is not a concrete request, but an abstract one, used to represent the common data of the requests.
 * It's extended by other classes that represent the concrete requests.
 * 
 * It contains the type and the size of the order.
 * 
 * @version 1.0
 * @see JSON
 * @see CROSS.Orders.Order
 * 
 * @see Limit
 * @see Market
 * @see Stop
 */
public abstract class Generic extends JSON {
    
    private String type;
    private Integer size;

    /**
     * Constructor of the Generic class.
     * 
     * @param order The order to get the type and the size from.
     * @throws NullPointerException If the order is null.
     */
    public <O extends CROSS.Orders.Order> Generic(O order) throws NullPointerException {
        if (order == null) {
            throw new NullPointerException("The order cannot be null.");
        }
        
        this.size = order.getQuantity().getQuantity();
        this.type = order.getPrice().getType().name().toLowerCase();
    }

    /**
     * Getter for the type of the order.
     * 
     * @return The type of the order.
     */
    public PriceType getType() {
        return PriceType.valueOf(this.type.toUpperCase());
    }
    /**
     * Getter for the size of the order.
     * 
     * @return The size of the order.
     */
    public Quantity getSize() {
        return new Quantity(this.size);
    }

    @Override
    public String toString() {
        return String.format("Type [%s] - Size [%s]", this.getType().name(), this.getSize().getQuantity());
    }

}
