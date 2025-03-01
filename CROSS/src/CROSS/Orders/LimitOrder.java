package CROSS.Orders; 

import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;

/**
 * 
 * LimitOrder class represents a limit order in the system.
 * 
 * It extends the Order class.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Order
 * 
 * @see Market
 * @see SpecificPrice
 * @see Quantity
 * @see User
 * 
 * */
public class LimitOrder extends Order {

    /**
     * 
     * Constructor for the class.
     * It creates a new limit order with the given price, quantity and user.
     * 
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param user The user who placed the order.
     * 
     * @throws IllegalArgumentException If the price is not valid for a limit order.
     * @throws NullPointerException If any of the parameters are null.
     * @throws RuntimeException If the market has a null actual price ask or bid and the order is an ask or bid respectively.
     * 
     * */
    public LimitOrder(SpecificPrice price, Quantity quantity, User user) throws IllegalArgumentException, NullPointerException, RuntimeException {
        
        super(price, quantity, user);
        
        Market market = price.getMarket();
        // Price coherence / order type checks.
        if (market.getActualPriceAsk() != null && price.getType() == PriceType.ASK && price.getValue() > market.getActualPriceAsk().getValue()) {
            throw new IllegalArgumentException("Buy limit order price is higher than the market ask price.");
        }
        if (market.getActualPriceBid() != null && price.getType() == PriceType.BID && price.getValue() < market.getActualPriceBid().getValue()) {
            throw new IllegalArgumentException("Sell limit order price is lower than the market bid price.");
        }

    } 

}
