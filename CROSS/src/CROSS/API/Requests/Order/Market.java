package CROSS.API.Requests.Order;

import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;

/**
 * Market is a class that extends Generic and is used to request orders data.
 * It is used to represent the request that is about the order's data.
 * It contains the type and the size of the order.
 * 
 * @version 1.0
 * @see Generic
 * @see PriceType
 * @see Quantity
 */
public class Market extends Generic {

    /**
     * Constructor of the Market class.
     * 
     * @param type The type of the order.
     * @param size The size of the order.
     */
    public Market(PriceType type, Quantity size) {
        super(type, size);
    }

    // Getters defined in the super class.

}
