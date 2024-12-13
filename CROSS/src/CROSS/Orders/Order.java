package CROSS.Orders;

import CROSS.Enums.Direction;
import CROSS.Enums.PriceType;
import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;
import CROSS.Users.User;
import CROSS.Users.Users;

// Abstract class for Order, a order without a type is not allowed.
public abstract class Order {

    private SpecificPrice price;
    private Direction direction;
    private Quantity quantity;
    private Market market;
    private User user;

    private Long id;

    public Order(Market market, SpecificPrice price, Direction direction, Quantity quantity, String userUsername) throws IllegalArgumentException {
        if (price.getValue() <= 0) {
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

        this.id = System.currentTimeMillis();
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        User u = Users.getUser(userUsername);
        if (u == null) {
            throw new IllegalArgumentException("User not found.");
        }
        this.user = u;
    }

    public SpecificPrice getPrice() {
        return price;
    }
    public Direction getDirection() {
        return direction;
    }
    public Quantity getQuantity() {
        return this.quantity;
    }
    public Market getMarket() {
        return market;
    }
    public User getUser() {
        return user;
    }

    public void setQuantity(Quantity quantity) throws IllegalArgumentException {
        if (quantity.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity cannot be negative or 0.");
        }
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return String.format("ID [%s] - User [%s] - Price [%s] - Direction [%s] - Quantity [%s] - Market [%s]", this.getId().toString(), this.getUser().toString(), this.getPrice().toString(), this.getDirection().name(), this.getQuantity().toString(), this.getMarket().toString());
    }

    public String toStringShort() {
        return String.format("ID [%s] - Quantity [%s]", this.getId().toString(), this.getQuantity().toString());
    }

}

