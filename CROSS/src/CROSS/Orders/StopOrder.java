package CROSS.Orders;

import CROSS.Enums.Direction;
import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;

public class StopOrder extends Order {

    public StopOrder(Market market, SpecificPrice price, Direction direction, Quantity quantity) {
        if (direction == Direction.BUY) {
            if (price.getValue().compareTo(market.getActualPrice().getValue()) > 0) {
                throw new IllegalArgumentException("Buy stop order price is higher than best ask.");
            }
        } else {
            if (price.getValue().compareTo(market.getActualPrice().getValue()) < 0) {
                throw new IllegalArgumentException("Sell stop order price is lower than best bid.");
            }
        }
        super(market, price, direction, quantity);
    }
    
}
