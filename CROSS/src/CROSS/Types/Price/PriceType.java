package CROSS.Types.Price; 

/**
 * 
 * Enumerates the price type (ask / bid) and so, the type of a trade / order (buy / sell).
 * 
 * ASK prices are used to SELL using limit orders.
 * BID prices are used to BUY using limit orders.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 */
public enum PriceType {
    BID,
    ASK
}
