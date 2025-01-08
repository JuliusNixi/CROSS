package CROSS.Orders;

import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;

/**
 * LimitOrder class represents a limit order in the system.
 * It extends the Order class.
 * @version 1.0
 * @see Order
 * @see Market
 * @see SpecificPrice
 * @see Quantity
 * @see User
 * */
public class LimitOrder extends Order {

    /**
     * Constructor for the LimitOrder class.
     * It creates a new limit order with the given market, price, quantity and user.
     * 
     * @param market The market where the order is placed.
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param user The user who placed the order.
     * 
     * @throws IllegalArgumentException If the price is not valid.
     * */
    public LimitOrder(Market market, SpecificPrice price, Quantity quantity, User user) throws IllegalArgumentException {
        super(market, price, quantity, user);
        
        if (price.getType() == PriceType.ASK && price.getValue() > market.getActualPriceAsk().getValue()) {
            throw new IllegalArgumentException("Buy limit order price is higher than the market ask price.");
        }
        if (price.getType() == PriceType.BID && price.getValue() < market.getActualPriceBid().getValue()) {
            throw new IllegalArgumentException("Sell limit order price is lower than the market bid price.");
        }
    } 

}
