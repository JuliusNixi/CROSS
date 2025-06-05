package cross.orders;

import cross.types.Quantity;
import cross.types.price.SpecificPrice;
import cross.users.User;
import cross.utils.UniqueNumber;

/**
 *
 * Abstract class for Order, an order without a type is not allowed and cannot be instantiated.
 * It's extended by StopOrder, LimitOrder, MarketOrder.
 *
 * It contains the price and the quantity.
 * It also contains the user who submitted the order.
 * It also contains an id, which is a unique number that identifies the order normally, but can be set to a negative value to indicate an error.
 * It also contains a timestamp, which is the time when the order was executed.
 * 
 * It implements the Comparable interface to compare orders by id.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see SpecificPrice
 * @see Quantity
 * @see OrderType
 *
 * @see User
 *
 * @see MarketOrder
 * @see LimitOrder
 * @see StopOrder
 *
 * @see UniqueNumber
 *
 */
public abstract class Order implements Comparable<Order> {

    // USED IN THE DB AND APIS

    // Each order has a type, given by the subclass.
    // Since there are the subclasses for the different types of orders it's not necessary. Added for convenience.
    private final OrderType orderType;

    // Each order has a size / quantity.
    // Size can be modified during order's execution.
    private Quantity size = null;
    private final Quantity initialFixedSize;

    // Each order has a price.
    // The price of a market order can be updated to the current market price.
    // Price has inside 'price' and 'type' attributes.
    private SpecificPrice price = null;

    // Long and also Integer IDs because orderId could be -1 in the APIs, so must be Integer.
    // Long is needed since the UniqueNumber class that I use to set unique ids returns a Long.
    // When is setted a normal (positive) order's id, its value is stored in the Long orderIdL and orderId is set to null.
    // When is setted -1 (API's response for an error) orderIdL is set to null and orderId is set to -1.
    // This works since the null fields are omitted by Gson in the JSON representation of the object.
    // The only drawback is renaming the orderIdL in the orderId field is needed when the object is serialized in JSON and orderIdL is not null.
    // This latter step is done in the JSONAPIMessage class.
    private Long orderIdL = null;
    private Integer orderId = null;

    // Timestamp of the order EXECUTION, setted when the order is executed.
    private Long timestamp = null;

    // ADDED BY ME

    // Each order has an associated user, its creator.
    private transient User user = null;

    /**
     *
     * Constructor for the class.
     *
     * It sets the id of the order to a unique number. Can be changed with the below setter if needed.
     *
     * Synchronization on the price and quantity objects not needed, since they cannot be changed (have no setters).
     *
     * @param orderType The type of the order.
     * @param quantity The quantity of the order.
     * @param price The price of the order.
     *
     * @throws NullPointerException If the order type, price or quantity are null.
     *
     */
    public Order(OrderType orderType, Quantity quantity, SpecificPrice price) throws NullPointerException {

        // Null checks.
        if (orderType == null) {
            throw new NullPointerException("Order's type cannot be null.");
        }
        if (price == null) {
            throw new NullPointerException("Order's specific price cannot be null.");
        }
        if (quantity == null) {
            throw new NullPointerException("Order's quantity cannot be null.");
        }

        // Setting the order's id.
        this.orderIdL = new UniqueNumber().getNumber();
        this.orderId = null;

        // Synchronization on price and quantity objects not needed, since they cannot be changed (have no setters).
        this.orderType = orderType;
        this.price = price;
        this.size = quantity;
        this.initialFixedSize = quantity;

    }

    // GETTERS
    /**
     *
     * Getter for the order type.
     *
     * @return The type of the order as an OrderType enum.
     *
     */
    public OrderType getOrderType() {

        return this.orderType;

    }
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

        return this.size;

    }
    /**
     *
     * Getter for the order's id.
     * 
     * If the order has a valid positive id contained in orderIdL, it is returned as a Long.
     * Otherwise, if the order has -1 contained in orderId, it is returned as an Integer.
     *
     * @return The id of the order as a Number (Long or Integer).
     *
     */
    public Number getId() {

        if ((this.orderIdL == null && this.orderId == null) || (this.orderIdL != null && this.orderId != null)) {
            throw new IllegalStateException("Order's id in inconsistent state.");
        }

        if (this.orderIdL != null) {
            return this.orderIdL;
        }

        if (this.orderId != null) {
            return this.orderId;
        }

        throw new IllegalStateException("Order's id in inconsistent state.");

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
    /**
     *
     * Getter for user.
     *
     * @return The user of the order, so its creator, as an User object.
     *
     */
    public User getUser() {

        return this.user;

    }

    @Override
    public synchronized int compareTo(Order otherOrder) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (otherOrder == null) {
            throw new NullPointerException("Order to compare with cannot be null.");
        }

        synchronized (otherOrder) {

            Integer thisIdI = null;
            Long thisIdL = null;
            if (this.orderIdL == null) {
                thisIdI = (Integer) this.getId();
            }else {
                thisIdL = (Long) this.getId();
            }

            Integer otherIdI = null;
            Long otherIdL = null;
            if (otherOrder.orderIdL == null) {
                otherIdI = (Integer) otherOrder.getId();
            }else {
                otherIdL = (Long) otherOrder.getId();
            }

            // < 0 this is smaller than otherOrder
            // > 0 this is greater than otherOrder
            if (thisIdI != null) {

                if (otherIdI != null) {

                    return thisIdI.compareTo(otherIdI);

                }else {

                    return -1;

                }

            } else {

                if (otherIdI != null) {

                    // thisIdI is null, so thisIdL is not null.
                    return 1;

                }else {

                    // otherIdI is null, so otherIdL is not null.
                    return thisIdL.compareTo(otherIdL);

                }

            }


        }

    }

    // SETTERS
    /**
     *
     * Sets the quantity of the order.
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
            throw new NullPointerException("Quantity to set in an order cannot be null.");
        }

        // Synchronized on the quantity object is not needed, since it cannot be changed (has no setters).
        this.size = quantity;

    }

    /**
     *
     * Sets the price of the order.
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
        this.orderIdL = null;

    }
    /**
     *
     * Sets the id of the order.
     *
     * NB: The id is normally set in the constructor automatically to a new unique number.
     *
     * Synchronized to avoid multi-threads problems.
     *
     * @param id The new id of the order as a Long.
     *
     * @throws NullPointerException If the order's id is null.
     *
     */
    public synchronized void setId(Long id) throws NullPointerException {

        // Null check.
        if (id == null) {
            throw new NullPointerException("ID to set on an order cannot be null.");
        }

        this.orderIdL = id;
        this.orderId = null;

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

    /**
     *
     * Sets the user of the order, its creator / owner.
     * 
     * NB: Not requested in the constructor, since the order object can be used also in the APIs and by the client.
     *
     * Synchronized to avoid multi-threads problems.
     * Synchronized on the user object to avoid it being changed while the order is being set.
     *
     * @param user The new user of the order as an User object.
     *
     * @throws NullPointerException If the order's user is null.
     *
     */
    public synchronized void setUser(User user) throws NullPointerException {

        // Null check.
        if (user == null) {
            throw new NullPointerException("User to set on an order as owner cannot be null.");
        }

        synchronized (user) {

            this.user = user;

        }

    }

    // TOSTRING METHODS
    @Override
    public synchronized String toString() {

        String timestamp = this.timestamp == null ? "null" : this.timestamp.toString();
        String id = this.orderIdL == null ? this.orderId.toString() : this.orderIdL.toString();
        String user = this.user == null ? "null" : this.user.toString();

        return String.format("Order's Type [%s] - ID [%s] - User [%s] - Price [%s] - Quantity [%s] - Timestamp [%s] - Initial Size [%s]", this.getClass().getSimpleName(), id, user, this.getPrice().toString(), this.getQuantity().toString(), timestamp, this.initialFixedSize.toString());

    }

    /**
     *
     * Short version of the toString() method.
     *
     * Synnchronized to avoid multi-threads problems.
     *
     * @return A String with the order's most important information only.
     *
     */
    public synchronized String toStringShort() {

        String id = this.orderIdL == null ? this.orderId.toString() : this.orderIdL.toString();
        String username = this.user == null ? "null" : this.user.getUsername();
        return String.format("ID [%s] - Quantity [%s] - Username [%s] - Price Value [%s]", id, this.getQuantity().toString(), username, this.getPrice().getValue().toString());

    }

    public Quantity getInitialFixedSize() {

        return this.initialFixedSize;

    }
    


}
