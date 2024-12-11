package CROSS.Orders;

import CROSS.Enums.Direction;
import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;

public class MarketOrder extends Order {

    public MarketOrder(Market market, Direction direction, Quantity quantity) {
        // ATTENTION: Here the logic is different from the LimitOrder.
        // Is reversed because if it's a buy order it will HIT the sell orders on the book (red ones) and viceversa.
        SpecificPrice price = market.getActualPrice();
        // TODO: If it's a sell order, the actualPrice is not the correct one.
        super(market, price, direction, quantity);
    }

}
