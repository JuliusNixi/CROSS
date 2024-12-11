package CROSS.Orders;

import java.time.Instant;

import CROSS.Enums.Direction;
import CROSS.Enums.PriceType;
import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;

// Abstract class for Order, a order without a type is not allowed.
public abstract class Order {

    private SpecificPrice price;
    private Direction direction;
    private Quantity quantity;
    private Market market;

    private Long id;

    public Order(Market market, SpecificPrice price, Direction direction, Quantity quantity) throws IllegalArgumentException {
        if ((direction == Direction.BUY && price.getValue() <= 0) || (direction == Direction.SELL && price.getValue() <= 0)) {
            throw new IllegalArgumentException("Price cannot be negative or 0.");
        }
        if (quantity.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity cannot be negative or 0.");
        }
        if ((price.getType() == PriceType.ASK && direction != Direction.BUY) || (price.getType() == PriceType.BID && direction != Direction.SELL)) {
            throw new IllegalArgumentException("Price type must be the same as the order direction.");
        }
        this.direction = direction;
        this.price = price;
        this.quantity = quantity;
        this.market = market;

        // Time in seconds since epoch used as id.
        this.id = Instant.now().getEpochSecond();
    }

    public SpecificPrice getPrice() {
        return price;
    }
    public Direction getDirection() {
        return direction;
    }
    public Quantity getQuantity() {
        return quantity;
    }
    public Market getMarket() {
        return market;
    }

    public Long getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return "Order [price=" + price + ", direction=" + direction + ", quantity=" + quantity + ", market=" + market + ", id=" + id + "]";
    }

}

