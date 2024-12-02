package CROSS.Orders;

import CROSS.Market;
import CROSS.Price;
import CROSS.Quantity;
import CROSS.Enums.Direction;

public class MarketOrder extends Order {
    public MarketOrder(Market market, Direction direction, Quantity quantity) {
        Price price = market.getPrice();
        super(market, price, direction, quantity);
    }
}
