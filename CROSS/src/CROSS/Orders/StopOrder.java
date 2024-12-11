package CROSS.Orders;

import CROSS.Enums.Direction;
import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;

public class StopOrder extends Order {

    public StopOrder(Market market, SpecificPrice price, Direction direction, Quantity quantity) {
        // TODO: Implement this constructor.
        super(market, price, direction, quantity);
    }

    public String toString() {
        return String.format("Order Type [%s] - Order [%s]", "Stop", super.toString());
    }
    
}
