package cross.orders;

/**
 * 
 * Enum that represents the type of an order.
 * 
 * Used in the ClientActionsUtils class to convert the order type from a string to an enum.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see ClientActionsUtils
 * 
 */
public enum OrderType {

    LIMIT,
    MARKET,
    STOP
    
}
