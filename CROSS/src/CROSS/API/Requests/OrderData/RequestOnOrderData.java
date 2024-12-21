package CROSS.API.Requests.OrderData;

import CROSS.Enums.PriceType;
import CROSS.Types.Quantity;

/**
 * RequestOnOrderData is an abstract class.
 * It is used to represent the request that is about the order's data.
 * This class is not a concrete request, but an abstract one, used to represent the common data of the requests.
 * 
 * @version 1.0
 */
public abstract class RequestOnOrderData {
    
    private String type;
    private Integer size;

    /**
     * Constructor of the RequestOnOrderData class.
     * 
     * @param type The type of the order.
     * @param size The size of the order.
     * @throws NullPointerException If the type or the size is null.
     */
    public RequestOnOrderData(PriceType type, Quantity size) throws NullPointerException {
        if (type == null) {
            throw new NullPointerException("The type of the order cannot be null.");
        }
        // The check for the size > 0 is done in the Quantity class.
        if (size == null) {
            throw new NullPointerException("The size of the order cannot be null.");
        }
        // To serialize the type and the size in the JSON format, we need to convert them to the corresponding JSON format.
        this.size = size.getQuantity();
        this.type = type.name().toLowerCase();
    }

    /**
     * Getter for the type of the order.
     * The getter wraps back the raw JSON data to the corresponding Java object.
     * 
     * @return The type of the order.
     */
    public PriceType getType() {
        return PriceType.valueOf(type.toUpperCase());
    }
    /**
     * Getter for the size of the order.
     * The getter wraps back the raw JSON data to the corresponding Java object.
     * 
     * @return The size of the order.
     */
    public Quantity getSize() {
        return new Quantity(size);
    }

}
