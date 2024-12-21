package CROSS.API.Requests.OrderData;

import CROSS.Enums.PriceType;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;

/**
 * RequestOnOrderDataStop is a class that extends RequestOnOrderData and is used to request orders data.
 * It is used to represent the request that is about the order's data.
 * It contains the price.
 * 
 * @version 1.0
 * @see RequestOnOrderData
 */
public class RequestOnOrderDataStop extends RequestOnOrderData {
    
    private Integer price;

    /**
     * Constructor of the RequestOnOrderDataStop class.
     * 
     * @param type The type of the order.
     * @param size The size of the order.
     * @param price The price of the order.
     * @throws IllegalArgumentException If the price is null or if the price type is different from the order type.
     */
    public RequestOnOrderDataStop(PriceType type, Quantity size, SpecificPrice price) throws IllegalArgumentException {
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
