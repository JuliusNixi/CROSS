package CROSS.Orders;

import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;
import CROSS.Utils.UniqueNumber;

/**
 * Abstract class for Order, an order without a type is not allowed.
 * It's extended by StopMarketOrder, LimitOrder, MarketOrder.
 * 
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
public abstract class Order implements Comparable<Order> {

    private SpecificPrice price;
    private Quantity quantity;
    private Market market;
    private User user;
    // Integer because could be -1 in the API.
    private Integer id;

    /**
     * Constructor for Order class.
     * 
     * @param market The market where the order is placed.
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param user The user who placed the order.
     * 
     * @throws NullPointerException if market, price, quantity or user are null.
     * @throws IllegalArgumentException If the price's market doesn't match with the given market.
     */
    public Order(Market market, SpecificPrice price, Quantity quantity, User user) throws NullPointerException, IllegalArgumentException {
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

        // Market check.
        if (!price.getMarket().equals(market)) {
            throw new IllegalArgumentException("The price's market doesn't match with the given market.");
        }

        this.price = price;
        this.quantity = quantity;
        this.market = market;
        this.user = user;

        Long id = new UniqueNumber().getNumber();
        this.id = id.intValue();

    }

    // GETTERS
    /**
     * Getters for price.
     * @return The price of the order.
     */
    public SpecificPrice getPrice() {
        return new SpecificPrice(this.price.getValue(), this.price.getType(), this.price.getMarket());
    }
    /**
     * Getters for quantity.
     * @return The quantity of the order.
     */
    public Quantity getQuantity() {
        return new Quantity(this.quantity.getQuantity());
    }
    /**
     * Getters for market.
     * @return The market of the order.
     */
    public Market getMarket() {
        return this.market;
    }
    /**
     * Getters for user.
     * @return The user of the order.
     */
    public User getUser() {
        return new User(this.user.getUsername(), this.user.getPassword());
    }
    /**
     * Getters for id.
     * @return The id of the order.
     */
    public Integer getId() {
        return Integer.valueOf(this.id);
    }

    // SETTERS
    /**
     * Sets the quantity of the order.
     * Using this method during the market orders execution.
     * 
     * @param quantity The new quantity of the order.
     * @throws NullPointerException If the quantity is null.
     * */
    public void setQuantity(Quantity quantity) throws NullPointerException {
        if (quantity == null) {
            throw new NullPointerException("Quantity cannot be null.");
        }
        
        this.quantity = new Quantity(quantity.getQuantity());
    }
    /**
     * Sets the price of the order.
     * Used in the MarketOrder class to update the price of the order to the current market price.
     * 
     * @param price The new price of the order.
     * @throws NullPointerException If the price is null.
     * */
    public void setPrice(SpecificPrice price) throws NullPointerException {
        if (price == null) {
            throw new NullPointerException("Price cannot be null.");
        }

        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("Type [%s] - ID [%s] - User [%s] - Price [%s] - Quantity [%s] - Market [%s]", this.getClass().getSimpleName(), this.getId().toString(), this.getUser().toString(), this.getPrice().toString(), this.getQuantity().toString(), this.getMarket().toString());
    }

    /**
     * Short version of the toString method.
     * Used for the OrderBookLine class.
     * 
     * @return A string with the id and the quantity of the order.
     */
    public String toStringShort() {
        return String.format("ID [%s] - Quantity [%s]", this.getId().toString(), this.getQuantity().toString());
    }

    @Override
    public int compareTo(Order o) {
        return this.getId().compareTo(o.getId());
    }

}

