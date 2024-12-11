package CROSS.Orders;

import CROSS.Enums.Direction;
import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;

// Abstract class for Order, a order without a type is not allowed.
public abstract class Order {
    SpecificPrice price;
    Direction direction;
    Quantity quantity;
    Market market;
    public Order(Market market, SpecificPrice price, Direction direction, Quantity quantity) {
        if ((direction == Direction.BUY && price.getValue() <= 0) || (direction == Direction.SELL && price.getValue() <= 0)) {
            throw new IllegalArgumentException("Price cannot be negative or 0.");
        }
        if (quantity.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity cannot be negative or 0.");
        }
        this.direction = direction;
        this.price = price;
        this.quantity = quantity;
        this.market = market;
    }
}

