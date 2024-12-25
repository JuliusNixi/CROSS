package CROSS.Order;

import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;

/**
 * MarketOrder class represents a market order in the system.
 * It extends the Order class.
 * @version 1.0
 * @see Order
 * @see Market
 * @see PriceType
 * @see Quantity
 * @see User
 * */
public class MarketOrder extends Order {

    /**
     * Constructor for a Market Order.
     * ATTENTION: Here the logic is different from the LimitOrder.
     * Is reversed because if it's a buy order it will HIT the sell orders on the book (red ones) and viceversa.
     * @param market Market where the order is placed.
     * @param type Type of the order (ASK or BID).
     * @param quantity Quantity of the order.
     * @param user User that placed the order.
     * @throws NullPointerException If the PriceType is null.
     */
    public MarketOrder(Market market, PriceType type, Quantity quantity, User user) throws NullPointerException {
        if (type == null) {
            throw new NullPointerException("PriceType cannot be null");
        }
        SpecificPrice price;
        if (type == PriceType.ASK) {
            price = market.getActualPriceAsk();
        } else {
            price = market.getActualPriceBid();
        }
        super(market, price, quantity, user);
    }

}
