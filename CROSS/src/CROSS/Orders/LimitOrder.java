package CROSS.Orders;

import CROSS.Enums.Direction;
import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;

public class LimitOrder extends Order {

    public LimitOrder(Market market, SpecificPrice price, Direction direction, Quantity quantity) {
        if (direction == Direction.BUY && price.getValue() > market.getActualPriceAsk().getValue()) {
            throw new IllegalArgumentException("Buy limit order price is higher than the market ask price.");
        }
        if (direction == Direction.SELL && price.getValue() < market.getActualPriceBid().getValue()) {
            throw new IllegalArgumentException("Sell limit order price is lower than the market bid price.");
        }
        super(market, price, direction, quantity);
    } 

    public String toString() {
        return String.format("Order Type [%s] - Order [%s]", "Limit", super.toString());
    }

}
