package CROSS.Orders;

import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
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
     * It also sets the id of the order to a unique number.
     * 
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param user The user who placed the order.
     * 
     * @throws NullPointerException If the price, quantity or user are null.
     * @throws RuntimeException If the market has a null actual price ask or bid and the order is an ask or bid respectively.
     * 
     */
    public Order(SpecificPrice price, Quantity quantity, User user) throws NullPointerException, RuntimeException {
       
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

        this.price = price;
        this.quantity = quantity;
        this.user = user;

        Long id = new UniqueNumber().getNumber();
        // This conversion is not beautiful, but it's necessary, since the UniqueNumber class returns a Long.
        // I cannot use a Long for the id of the order, because the API could return -1.
        this.orderId = id.intValue();

        Market market = price.getMarket();
        // Market with null actual prices check.
        if (market.getActualPriceAsk() == null && this.price.getType() == PriceType.ASK) {
            throw new RuntimeException("Cannot create an order in a market with a null actual price ask.");
        }
        if (market.getActualPriceBid() == null && this.price.getType() == PriceType.BID) {
            throw new RuntimeException("Cannot create an order in a market with a null actual price bid.");
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

        return new SpecificPrice(this.price.getValue(), this.price.getType(), Market.copyMarket(this.price.getMarket()));

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
     * Getter for a COPY of the market.
     * 
     * @return The order's market as a Market object as a copy.
     * 
     */
    public Market getMarket() {

        return Market.copyMarket(this.price.getMarket());

    }
    /**
     * 
     * Getter for user.
     * 
     * @return The user of the order, so its creator, as an User object.
     * 
     */
    public User getUser() {

        User user = new User(this.user.getUsername(), this.user.getPassword());
        if (this.user.getFileLineId() != null) {
            user.setFileLineId(this.user.getFileLineId());
        }
        return user;

    }
    /**
     * 
     * Getter for the order's id.
     * 
     * @return The id of the order as an Integer.
     * 
     */
    public Integer getId() {

        return Integer.valueOf(this.orderId);

    }
    /**
     * 
     * Getter for the order's timestamp.
     * 
     * @return The timestamp of the order as Long.
     * 
     */
    public Long getTimestamp() {

        return Long.valueOf(this.timestamp);

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
        
        this.quantity = new Quantity(quantity.getValue());

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
    public String toString() {

        String timestamp = this.timestamp == null ? "null" : this.timestamp.toString();

        return String.format("Order's Type [%s] - ID [%s] - User [%s] - Price [%s] - Quantity [%s] - Market [%s] - Timestamp [%s]", this.getClass().getSimpleName(), this.getId().toString(), this.getUser().toString(), this.getPrice().toString(), this.getQuantity().toString(), this.price.getMarket().toString(), timestamp);

    }
    /**
     * 
     * Short version of the toString() method.
     * Used for the OrderBookLine class.
     * 
     * @return A String with the order's id and the order's quantity only.
     * 
     */
    public String toStringShort() {

        return String.format("ID [%s] - Quantity [%s]", this.getId().toString(), this.getQuantity().toString());

    }

    @Override
    public int compareTo(Order otherOrder) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (otherOrder == null) {
            throw new NullPointerException("Order to compare to cannot be null.");
        }

        // Different markets check.
        if (otherOrder.getMarket().compareTo(this.getMarket()) != 0) {
            throw new IllegalArgumentException("Cannot compare orders from different markets.");
        }

        return this.getId().compareTo(otherOrder.getId());

    }

}

