package CROSS.API.Requests.Order;

import CROSS.API.JSON;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;

/**
 * Generic is an abstract class.
 * It is used to represent the request that is about the order's data.
 * This class is not a concrete request, but an abstract one, used to represent the common data of the requests.
 * It's extended by other classes that represent the concrete requests.
 * It contains the type and the size of the order.
 * 
 * @version 1.0
 * @see JSON
 * @see Limit
 * @see Market
 * @see Stop
 * @see Quantity
 * @see PriceType
 */
public abstract class Generic extends JSON {
    
    private String type;
    private Integer size;

    /**
     * Constructor of the Generic class.
     * 
     * @param type The type of the order.
     * @param size The size of the order.
     * @throws NullPointerException If the type or the size are null.
     */
    public Generic(PriceType type, Quantity size) throws NullPointerException {
        if (type == null) {
            throw new NullPointerException("The type of the order cannot be null.");
        }
        // The check for the size > 0 is done in the Quantity class.
        if (size == null) {
            throw new NullPointerException("The size of the order cannot be null.");
        }
        this.size = size.getQuantity();
        this.type = type.name().toLowerCase();
    }

    /**
     * Getter for the type of the order.
     * 
     * @return The type of the order.
     */
    public PriceType getType() {
        return PriceType.valueOf(type.toUpperCase());
    }
    /**
     * Getter for the size of the order.
     * 
     * @return The size of the order.
     */
    public Quantity getSize() {
        return new Quantity(size);
    }

}
