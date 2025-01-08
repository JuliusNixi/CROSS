package CROSS.Orders;

import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;

/**
 * A stop market class represents a stop order in the system.
 * A stop market order is converted into a market order when the stop price is reached.
 * It extends the Order class.
 * @version 1.0
 * @see Order
 * @see Market
 * @see SpecificPrice
 * @see Quantity
 * @see User
 * */
public class StopMarketOrder extends Order {

    /**
     * Constructor for the StopMarketOrder class.
     * 
     * @param market The market where the order is placed.
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param user The user who placed the order.
     * 
     * @throws IllegalArgumentException If the price of the order is not valid.
     * */
    public StopMarketOrder(Market market, SpecificPrice price, Quantity quantity, User user) throws IllegalArgumentException {
        super(market, price, quantity, user);

        if (price.getType() == PriceType.ASK && price.getValue() < market.getActualPriceBid().getValue()) {
            throw new IllegalArgumentException("Buy stop order price is lower than the market bid price.");
        }
        if (price.getType() == PriceType.BID && price.getValue() > market.getActualPriceAsk().getValue()) {
            throw new IllegalArgumentException("Sell stop order price is higher than the market ask price.");
        }
    }
    
}
