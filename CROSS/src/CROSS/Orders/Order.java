package CROSS.Orders;

import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;
import CROSS.Utils.UniqueNumber;

/**
 * 
 * Abstract class for Order, an order without a type is not allowed.
 * It's extended by StopMarketOrder, LimitOrder, MarketOrder.
 * 
 * It contains the price, the quantity, the market and the user of the order.
 * 
 * It also contains an id, which is a unique number.
 * 
 * It implements the Comparable interface to compare orders by id.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see SpecificPrice
 * @see Quantity
 * 
 * @see Market
 * @see User
 * 
 * @see MarketOrder
 * @see LimitOrder
 * @see StopMarketOrder
 * 
 * @see UniqueNumber
 * 
 */
public abstract class Order implements Comparable<Order> {

    private SpecificPrice price = null;
    private Quantity quantity = null;

    // Each order has an associated market.
    private final Market market;
    // Each order has an associated user, its creator.
    private final User user;

    // Integer because could be -1 in the API.
    private Integer id = null;

    /**
     * 
     * Constructor for the class.
     * 
     * It also sets the id of the order to a unique number.
     * 
     * @param market The market where the order is placed.
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param user The user who placed the order.
     * 
     * @throws NullPointerException if market, price, quantity or user are null.
     * @throws IllegalArgumentException If the price's market doesn't match with the given market.
     * 
     */
    public Order(Market market, SpecificPrice price, Quantity quantity, User user) throws NullPointerException, IllegalArgumentException {
       
        // Null checks.
        if (market == null) {
            throw new NullPointerException("Order's market cannot be null.");
        }
        if (price == null) {
            throw new NullPointerException("Order's price cannot be null.");
        }
        if (quantity == null) {
            throw new NullPointerException("Order's quantity cannot be null.");
        }
        if (user == null) {
            throw new NullPointerException("Order's user cannot be null.");
        }

        // Market / Price Market check.
        if (!price.getMarket().equals(market)) {
            throw new IllegalArgumentException("The price's market doesn't match with the given market.");
        }

        this.price = price;
        this.quantity = quantity;
        this.market = market;
        this.user = user;

        Long id = new UniqueNumber().getNumber();
        // This conversion is not beautiful, but it's necessary, since the UniqueNumber class returns a Long.
        this.id = id.intValue();

    }

    // GETTERS
    /**
     * 
     * Getter for the price.
     * 
     * @return The price of the order as a SpecificPrice object.
     * 
     */
    public SpecificPrice getPrice() {
        return new SpecificPrice(this.price.getValue(), this.price.getType(), this.price.getMarket());
    }
    /**
     * 
     * Getter for quantity.
     * 
     * @return The quantity of the order as a Quantity object.
     * 
     */
    public Quantity getQuantity() {
        return new Quantity(this.quantity.getValue());
    }
    /**
     * 
     * Getter for a copy market.
     * 
     * @return The market of the order as a Market object as a copy.
     * 
     */
    public Market getMarket() {
        return Market.copyMarket(market);
    }
    /**
     * 
     * Getter for user.
     * 
     * @return The user of the order as an User object.
     * 
     */
    public User getUser() {
        return new User(this.user.getUsername(), this.user.getPassword());
    }
    /**
     * 
     * Getter for the order's id.
     * 
     * @return The id of the order as Integer.
     * 
     */
    public Integer getId() {
        return Integer.valueOf(this.id);
    }

    // SETTERS
    /**
     * 
     * Sets the quantity of the order.
     * 
     * Using this method during the market orders execution.
     * 
     * Synchronized because to avoid multi-threads problems.
     * 
     * @param quantity The new quantity of the order.
     * 
     * @throws NullPointerException If the new quantity of the order is null.
     * 
     * */
    public synchronized void setQuantity(Quantity quantity) throws NullPointerException {

        // Null check.
        if (quantity == null) {
            throw new NullPointerException("Quantity to set cannot be null.");
        }
        
        this.quantity = new Quantity(quantity.getValue());

    }
    /**
     * 
     * Sets the price of the order.
     * 
     * Used in the MarketOrder class to update the price of the order to the current market price.
     * 
     * Synchronized because to avoid multi-threads problems.
     * 
     * @param price The new price of the order.
     * 
     * @throws NullPointerException If the new price of the order is null.
     * @throws IllegalArgumentException If the price's market doesn't match with the given market.
     * 
     * */
    public synchronized void setPrice(SpecificPrice price) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (price == null) {
            throw new NullPointerException("Price to set on an order cannot be null.");
        }

        // Market / Price Market check.
        if (!price.getMarket().equals(market)) {
            throw new IllegalArgumentException("The price's market doesn't match with the given market.");
        }

        this.price = price;

    }
    /**
     * 
     * Sets the id of the order.
     * 
     * NB: The id is normally set in the constructor automatically to an unique number.
     * This method is used in the OrderBook class to set the id of a market order to the corresponding stop order.
     * 
     * Synchronized because to avoid multi-threads problems.
     * 
     * @param id The new id of the order.
     * 
     * @throws NullPointerException If the order's id is null.
     * @throws IllegalArgumentException If the order's id is negative.
     * 
     */
    public synchronized void setId(Integer id) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (id == null) {
            throw new NullPointerException("ID to set on an order cannot be null.");
        }

        // Negative check.
        if (id < 0) {
            throw new IllegalArgumentException("ID to set on an order cannot be negative.");
        }

        this.id = id;

    }

    // TOSTRING METHODS
    @Override
    public String toString() {
        return String.format("Type [%s] - ID [%s] - User [%s] - Price [%s] - Quantity [%s] - Market [%s]", this.getClass().getSimpleName(), this.getId().toString(), this.getUser().toString(), this.getPrice().toString(), this.getQuantity().toString(), this.market.toString());
    }
    /**
     * 
     * Short version of the toString method.
     * Used for the OrderBookLine class.
     * 
     * @return A string with the order's id and the order's quantity.
     * 
     */
    public String toStringShort() {
        return String.format("ID [%s] - Quantity [%s]", this.getId().toString(), this.getQuantity().toString());
    }

    @Override
    public int compareTo(Order o) {
        return this.getId().compareTo(o.getId());
    }

}

