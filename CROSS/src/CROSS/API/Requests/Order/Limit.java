package CROSS.API.Requests.Order;

import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;

/**
 * Limit is a class that extends Generic and is used to request orders data.
 * It is used to represent the request that is about the order's data.
 * It contains the price.
 * 
 * @version 1.0
 * @see Generic
 * @see PriceType
 * @see Quantity
 * @see SpecificPrice
 */
public class Limit extends Generic {

    private Integer price;

    /**
     * Constructor of the Limit class.
     * 
     * @param type The type of the order.
     * @param size The size of the order.
     * @param price The price of the order.
     * @throws IllegalArgumentException If the price is null or if the price type is different from the order type.
     */
    public Limit(PriceType type, Quantity size, SpecificPrice price) throws IllegalArgumentException {
        super(type, size);
        if (price == null) {
            throw new IllegalArgumentException("The price cannot be null.");
        }
        if (price.getType() != type) {
            throw new IllegalArgumentException("The price type must be the same as the order type.");
        }
        this.price = price.getValue();
    }

    /**
     * Getter of the price.
     * 
     * @return The price of the order.
     */
    public SpecificPrice getPrice() {
        return new SpecificPrice(price, super.getType());
    }
    
}
