package CROSS.Orders;

import CROSS.Market;
import CROSS.Price;
import CROSS.Quantity;
import CROSS.Enums.Direction;

public class StopOrder extends Order {

    public StopOrder(Market market, Price price, Direction direction, Quantity quantity) {
        if (direction == Direction.BUY) {
            if (price.getAsk().compareTo(market.getPrice().getAsk()) > 0) {
                throw new IllegalArgumentException("Buy stop order price is higher than best ask.");
            }
        } else {
            if (price.getBid().compareTo(market.getPrice().getBid()) < 0) {
                throw new IllegalArgumentException("Sell stop order price is lower than best bid.");
            }
        }
        super(market, price, direction, quantity);
    }
    
}
