package CROSS.Orders;

import CROSS.Types.Quantity;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;
import CROSS.Utils.UniqueNumber;
import CROSS.OrderBook.Market;

/**
 * 
 * Abstract class for Order, an order without a type is not allowed and cannot be instantiated.
 * It's extended by StopMarketOrder, LimitOrder, MarketOrder.
 * 
 * It contains the price and the quantity.
 * It also contains the user who submitted the order.
 * It also contains an id, which is a unique number that identifies the order.
 * 
 * It implements the Comparable interface to compare orders by id.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see SpecificPrice
 * @see Quantity
 * 
 * @see User
 * 
 * @see MarketOrder
 * @see LimitOrder
 * @see StopMarketOrder
 * 
 * @see UniqueNumber
 * 
 * @see Market
 * 
 */
public abstract class Order implements Comparable<Order> {

    // Each order has a price.
    // The price of a market order can be updated to the current market price.
    private SpecificPrice price = null;

    // Each order has a quantity.
    private Quantity quantity = null;

    // Each order has an associated user, its creator.
    private final User user;

    // Integer and not Long because could be -1 in the API.
    private Integer orderId = null;

    // Timestamp of the order EXECUTION.
    private Long timestamp = null;

    /**
     * 
     * Constructor for the class.
     * 
     * It sets the id of the order to a unique number. Can be changed with the below setter.
     * 
     * Synchronization on the price and quantity objects not needed, since they cannot be changed (have no setters).
     * Synchronization on the user object to safely set it.
     * 
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param user The user who placed the order.
     * 
     * @throws NullPointerException If the price, quantity or user are null.
     * 
     */
    public Order(SpecificPrice price, Quantity quantity, User user) throws NullPointerException {

        // Null checks.
        if (price == null) {
            throw new NullPointerException("Order's price cannot be null.");
        }
        if (quantity == null) {
            throw new NullPointerException("Order's quantity cannot be null.");
        }
        if (user == null) {
            throw new NullPointerException("Order's user cannot be null.");
        }

        Long id = new UniqueNumber().getNumber();
        // This conversion is not beautiful, but it's necessary, since the UniqueNumber class returns a Long.
        // I cannot use a Long for the id of the order, because the API could return -1.
        this.orderId = id.intValue();
        if (this.orderId < 0) {
            this.orderId = -this.orderId;
        }

        // Synchronization on price and quantity objects not needed, since they cannot be changed (have no setters).
        this.price = price;
        this.quantity = quantity;
        // Synchronized on the user object to safely set it.
        synchronized (user) {
            this.user = user;
        }

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

        return this.price;

    }
    /**
     * 
     * Getter for quantity.
     * 
     * @return The quantity of the order as a Quantity object.
     * 
     */
    public Quantity getQuantity() {

        return this.quantity;

    }
    /**
     * 
     * Getter for the market.
     * 
     * @return The order's market as a Market object.
     * 
     */
    public Market getMarket() {

        return this.price.getMarket();

    }
    /**
     * 
     * Getter for user.
     * 
     * @return The user of the order, so its creator, as an User object.
     * 
     */
    public User getUser() {

        // Synchronized on the user object to safely get it.
        synchronized (this.user) {
            return this.user;
        }

    }
    /**
     * 
     * Getter for the order's id.
     * 
     * @return The id of the order as an Integer.
     * 
     */
    public Integer getId() {

        return this.orderId;

    }
    /**
     * 
     * Getter for the order's timestamp.
     * 
     * @return The timestamp of the order as Long.
     * 
     */
    public Long getTimestamp() {

        return this.timestamp;

    }

    // SETTERS
    /**
     * 
     * Sets the quantity of the order.
     * 
     * Using this method during the market orders execution to update the quantity of the order.
     * 
     * Synchronized to avoid multi-threads problems.
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
        
        // Synchronized on the quantity object is not needed, since it cannot be changed (has no setters).
        this.quantity = quantity;

    }
    /**
     * 
     * Sets the price of the order.
     * 
     * Used in the MarketOrder class to update the price of the order to the current market price.
     * 
     * Synchronized to avoid multi-threads problems.
     * 
     * @param price The new price of the order.
     * 
     * @throws NullPointerException If the new price of the order is null.
     * 
     * */
    public synchronized void setPrice(SpecificPrice price) throws NullPointerException {
        
        // Null check.
        if (price == null) {
            throw new NullPointerException("Price to set on an order cannot be null.");
        }

        // Synchronized on the price object is not needed, since it cannot be changed (has no setters).
        this.price = price;

    }
    /**
     * 
     * Sets the id of the order.
     * 
     * NB: The id is normally set in the constructor automatically to a new unique number.
     * This method is used in the OrderBook class to set the id of a market order to the corresponding stop order.
     * 
     * Synchronized to avoid multi-threads problems.
     * 
     * @param id The new id of the order as an Integer.
     * 
     * @throws NullPointerException If the order's id is null.
     * 
     */
    public synchronized void setId(Integer id) throws NullPointerException {
        
        // Null check.
        if (id == null) {
            throw new NullPointerException("ID to set on an order cannot be null.");
        }

        this.orderId = id;

    }
    /**
     * 
     * Sets the timestamp of the order.
     * 
     * NB: The timestamp is normally set on the order execution.
     * 
     * Synchronized to avoid multi-threads problems.
     * 
     * @param timestamp The new timestamp of the order as Long.
     * 
     * @throws NullPointerException If the order's timestamp is null.
     * 
     */
    public synchronized void setTimestamp(Long timestamp) throws NullPointerException {
        
        // Null check.
        if (timestamp == null) {
            throw new NullPointerException("Timestamp to set on an order cannot be null.");
        }

        this.timestamp = timestamp;

    }

    // TOSTRING METHODS
    @Override
    public synchronized String toString() {

        String timestamp = this.timestamp == null ? "null" : this.timestamp.toString();

        return String.format("Order's Type [%s] - ID [%s] - User [%s] - Price [%s] - Quantity [%s] - Market [%s] - Timestamp [%s]", this.getClass().getSimpleName(), this.getId().toString(), this.getUser().toString(), this.getPrice().toString(), this.getQuantity().toString(), this.price.getMarket().toString(), timestamp);

    }
    /**
     * 
     * Short version of the toString() method.
     * Used for the OrderBookLine class.
     * 
     * Synnchronized to avoid multi-threads problems.
     * 
     * @return A String with the order's most important information only.
     * 
     */
    public synchronized String toStringShort() {

        return String.format("ID [%s] - Quantity [%s] - Username [%s] - Price Value [%s]", this.getId().toString(), this.getQuantity().toString(), this.getUser().getUsername(), this.getPrice().getValue().toString());

    }

    @Override
    public synchronized int compareTo(Order otherOrder) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (otherOrder == null) {
            throw new NullPointerException("Order to compare to cannot be null.");
        }

        synchronized (otherOrder) {

            // Different markets check.
            if (otherOrder.getMarket().compareTo(this.getMarket()) != 0) {
                throw new IllegalArgumentException("Cannot compare orders from different markets.");
            }

            return this.getId().compareTo(otherOrder.getId());

        }

    }

}

