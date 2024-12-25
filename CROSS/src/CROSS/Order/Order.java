package CROSS.Order;

import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;
import CROSS.Users.Users;
import CROSS.Utils.UniqueNumber;

/**
 * Abstract class for Order, an order without a type is not allowed.
 * It's extended by StopMarketOrder, LimitOrder, MarketOrder.
 * @version 1.0
 * @see SpecificPrice
 * @see Quantity
 * @see Market
 * @see User
 * @see MarketOrder
 * @see LimitOrder
 * @see StopMarketOrder
 * @see UniqueNumber
 */
public abstract class Order {

    private SpecificPrice price;
    private Quantity quantity;
    private Market market;
    private User user;
    private Long id;

    /**
     * Constructor for Order.
     * @param market The market where the order is placed.
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param user The user who placed the order.
     * @throws NullPointerException if market, price, quantity or user are null.
     */
    public Order(Market market, SpecificPrice price, Quantity quantity, User user) throws NullPointerException {
        if (market == null) {
            throw new NullPointerException("Market cannot be null.");
        }
        if (price == null) {
            throw new NullPointerException("Price cannot be null.");
        }
        if (quantity == null) {
            throw new NullPointerException("Quantity cannot be null.");
        }
        if (user == null) {
            throw new NullPointerException("User cannot be null.");
        }

        this.price = price;
        this.quantity = quantity;
        this.market = market;
        this.id = new UniqueNumber().getNumber();

        // TODO: Check if user is in the Users list.

    }

    /**
     * Getters for price.
     * @return The price of the order.
     */
    public SpecificPrice getPrice() {
        return price;
    }
    /**
     * Getters for quantity.
     * @return The quantity of the order.
     */
    public Quantity getQuantity() {
        return this.quantity;
    }
    /**
     * Getters for market.
     * @return The market of the order.
     */
    public Market getMarket() {
        return market;
    }
    /**
     * Getters for user.
     * @return The user of the order.
     */
    public User getUser() {
        return user;
    }
    /**
     * Getters for id.
     * @return The id of the order.
     */
    public Long getId() {
        return id;
    }

    /**
     * Setter for the quantity.
     * Used to update the quantity during the execution of a market order.
     * @param quantity The quantity of the order.
     * @throws NullPointerException if the quantity is null.
     */
    public void setQuantity(Quantity quantity) throws NullPointerException {
        if (quantity == null) {
            throw new NullPointerException("Quantity cannot be null.");
        }
        this.quantity = quantity;
    }
    
    @Override
    public String toString() {
        return String.format("Type [%s] - ID [%s] - User [%s] - Price [%s] - Quantity [%s] - Market [%s]", this.getClass().getSimpleName(), this.getId().toString(), this.getUser().toString(), this.getPrice().toString(), this.getQuantity().toString(), this.getMarket().toString());
    }

    /**
     * Short version of the toString method.
     * Used for the OrderBookLine class.
     * @return A string with the id and the quantity of the order.
     */
    public String toStringShort() {
        return String.format("ID [%s] - Quantity [%s]", this.getId().toString(), this.getQuantity().toString());
    }

}

