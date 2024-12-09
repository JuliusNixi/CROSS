package CROSS.Orders;

// Abstract class for Order, a order without a type is not allowed.
import CROSS.*;
import CROSS.Enums.Direction;
import CROSS.Types.Price;
import CROSS.Types.Quantity;

public abstract class Order {
    Price price;
    Direction direction;
    Quantity quantity;
    Market market;
    public Order(Market market, Price price, Direction direction, Quantity quantity) {
        if ((direction == Direction.BUY && price.getAsk() <= 0) || (direction == Direction.SELL && price.getBid() <= 0)) {
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

