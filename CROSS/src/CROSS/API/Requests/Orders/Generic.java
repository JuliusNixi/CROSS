package CROSS.API.Requests.Orders;

import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;

/**
 * 
 * Generic is an abstract class used as base to be extended by the client's requests about orders.
 * 
 * It is used to represent the requests that are about the orders.
 * 
 * This class is not a concrete request, it's used to represent the common data of the requests about orders.
 * It's extended by other classes that represent the concrete requests.
 * 
 * It contains the type and the size of the order.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see CROSS.Orders.Order
 * @see PriceType
 * @see Quantity
 * 
 * @see Limit
 * @see Market
 * @see StopMarket
 * 
 */
public abstract class Generic {
    
    private final String type;
    private final Integer size;

    /**
     * 
     * Constructor of the class.
     * 
     * @param order The order to get the type and the size from.
     * 
     * @throws NullPointerException If the order is null.
     * 
     */
    public Generic(CROSS.Orders.Order order) throws NullPointerException {
        
        // Null check.
        if (order == null) {
            throw new NullPointerException("The order in the order request cannot be null.");
        }

        this.size = order.getQuantity().getValue();
        this.type = order.getPrice().getType().name().toLowerCase();

    }

    // GETTERS
    /**
     * 
     * Getter for the type of the order.
     * 
     * @return The type of the order as PriceType object.
     * 
     */
    public PriceType getType() {

        return PriceType.valueOf(this.type.toUpperCase());

    }
    /**
     * 
     * Getter for the size of the order.
     * 
     * @return The size of the order as Quantity object.
     * 
     */
    public Quantity getSize() {

        return new Quantity(this.size);

    }

}
