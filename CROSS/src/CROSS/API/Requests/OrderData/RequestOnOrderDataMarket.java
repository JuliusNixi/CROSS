package CROSS.API.Requests.OrderData;

import CROSS.Enums.PriceType;
import CROSS.Types.Quantity;

/**
 * RequestOnOrderDataMarket is a class that extends RequestOnOrderData and is used to request orders data.
 * It is used to represent the request that is about the order's data.
 * It contains the type and the size of the order.
 * 
 * @version 1.0
 * @see RequestOnOrderData
 */
public class RequestOnOrderDataMarket extends RequestOnOrderData {

    /**
     * Constructor of the RequestOnOrderDataMarket class.
     * 
     * @param type The type of the order.
     * @param size The size of the order.
     */
    public RequestOnOrderDataMarket(PriceType type, Quantity size) {
        super(type, size);
    }

}
