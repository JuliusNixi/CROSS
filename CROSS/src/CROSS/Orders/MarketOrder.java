package CROSS.Orders;

import CROSS.Market;
import CROSS.Enums.Direction;
import CROSS.Types.Price;
import CROSS.Types.Quantity;

public class MarketOrder extends Order {
    public MarketOrder(Market market, Direction direction, Quantity quantity) {
        Price price = market.getPrice();
        super(market, price, direction, quantity);
    }
}
