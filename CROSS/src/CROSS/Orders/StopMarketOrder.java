package CROSS.Orders;

import CROSS.Enums.Direction;
import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;

// A stop market order is converted into a market order when the stop price is reached.
public class StopMarketOrder extends Order {

    public StopMarketOrder(Market market, SpecificPrice price, Direction direction, Quantity quantity) {
        if (direction == Direction.BUY && price.getValue() < market.getActualPriceBid().getValue()) {
            throw new IllegalArgumentException("Buy stop order price is lower than the market bid price.");
        }
        if (direction == Direction.SELL && price.getValue() > market.getActualPriceAsk().getValue()) {
            throw new IllegalArgumentException("Sell stop order price is higher than the market ask price.");
        }
        super(market, price, direction, quantity);
    }

    public String toString() {
        return String.format("Order Type [%s] - Order [%s]", "Stop", super.toString());
    }
    
}
