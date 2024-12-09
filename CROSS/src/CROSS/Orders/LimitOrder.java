package CROSS.Orders;

import CROSS.Book.Market;
import CROSS.Enums.Direction;
import CROSS.Types.Price;
import CROSS.Types.Quantity;

public class LimitOrder extends Order {
    public LimitOrder(Market market, Price price, Direction direction, Quantity quantity) {
        if (direction == Direction.BUY && price.getAsk() > market.getPrice().getAsk()) {
            throw new IllegalArgumentException("Buy limit order price is higher than the market ask price.");
        }
        if (direction == Direction.SELL && price.getBid() < market.getPrice().getBid()) {
            throw new IllegalArgumentException("Sell limit order price is lower than the market bid price.");
        }
        super(market, price, direction, quantity);
    }   
}
