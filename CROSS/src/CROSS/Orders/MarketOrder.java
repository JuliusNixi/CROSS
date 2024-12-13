package CROSS.Orders;

import CROSS.Enums.Direction;
import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;

public class MarketOrder extends Order {

    public MarketOrder(Market market, Direction direction, Quantity quantity, String userUsername) {
        // ATTENTION: Here the logic is different from the LimitOrder.
        // Is reversed because if it's a buy order it will HIT the sell orders on the book (red ones) and viceversa.
        SpecificPrice price;
        if (direction == Direction.BUY) {
            price = market.getActualPriceAsk();
        } else {
            price = market.getActualPriceBid();
        }
        super(market, price, direction, quantity, userUsername);
    }

    public String toString() {
        return String.format("Order Type [%s] - Order [%s]", "Market", super.toString());
    }

}
