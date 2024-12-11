package CROSS.Orders;

import CROSS.Enums.Direction;
import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;

public class MarketOrder extends Order {
    public MarketOrder(Market market, Direction direction, Quantity quantity) {
        SpecificPrice price = market.getActualPrice();
        // TODO: If it's a sell order, the actualPrice is not the correct one.
        super(market, price, direction, quantity);
    }
}
