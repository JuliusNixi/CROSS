package CROSS.OrderBook;

import java.util.LinkedList;
import CROSS.Orders.LimitOrder;
import CROSS.Orders.Order;
import CROSS.Orders.StopMarketOrder;
import CROSS.Types.Quantity;
import CROSS.Types.Price.SpecificPrice;

/**
 * This class is used to represent a line in an order book.
 * It's used both by limit orders book and stop orders book. 
 * The line is omogeneus, all orders must have the same type.
 * @version 1.0
 * @param <O> Order type, must be a subclass of Order, but it's intended to be either LimitOrder or StopMarketOrder.
 * @see Order
 * @see SpecificPrice
 * @see Quantity
 */
public class OrderBookLine<O extends Order> {
 
    // This quantity is the sum of all orders in this line.
    // It's useful to know the total quantity of orders in the line,
    // without iterating over all orders each time.
    private Quantity totalQuantity;

    // LinkedList to keep the order in the same way they were added.
    // To execute the orders (matching algorithm) we use FIFO, so i give priority
    // to insert and remove element from the head and tail.
    // Insert and execute from the head and tail is O(1). These operations should be the most frequent.
    // Cancel an order is O(n) because we need to search for the order, but it's not a frequent operation (I hope).
    // Generic type order to handle both limit and stop orders.
    private LinkedList<O> orders;

    private SpecificPrice linePrice;

    // To avoid confusion, i store the type of the line (stop/limit) when created.
    // All orders in the line must have the same type.
    // I will use this to check if the order (to be handled) type match with the line type.
    // It's also used to check if OI == O type in the constructor.
    private O lineType = null;

    // TODO: Generate Javadoc everywhere.
    public <OI extends Order> OrderBookLine(SpecificPrice linePrice, OI initialOrder) throws IllegalArgumentException, NullPointerException {
        if (linePrice == null) {
            throw new NullPointerException("linePrice cannot be null.");
        }
        if (initialOrder == null) {
            throw new NullPointerException("initialOrder cannot be null.");
        }

        if (initialOrder instanceof LimitOrder) {
            // OK, it's a limit order.
        }else if (initialOrder instanceof StopMarketOrder) {
            // OK, it's a stop order.
        } else {
            throw new IllegalArgumentException("initialOrder type not supported.");
        }

        // Checking class match internal method generic type with class generic type.
        if (initialOrder.getClass() != lineType.getClass()) {
            throw new IllegalArgumentException("initialOrder type doesn't match with class type.");
        }

        this.totalQuantity = new Quantity(initialOrder.getQuantity().getQuantity());
        this.orders = new LinkedList<O>();
        this.linePrice = linePrice;
        this.lineType = null;

    }

    /**
     * Add an order to its corresponding line (same type stop/limit, same price).
     * Could be a stop or a limit order.
     * The line is omogeneus, all orders must have the same type (stop/limit).
     * The order is added at the beginning of the list, to follow a FIFO policy.
     * A check if the order is already present in the list is omitted, because a O(n) operation would be needed.
     * 
     * @param order The order to be added.
     * @throws IllegalArgumentException If the order type doesn't match with the line type.
     * @throws NullPointerException If the order is null.
     */
    public void addOrder(O order) throws IllegalArgumentException, NullPointerException {
        if (order == null)
            throw new NullPointerException("Order cannot be null.");

        if (order.getClass() == this.lineType.getClass()) {
            // The order is added at the beginning of the list, to follow a FIFO policy.
            orders.addFirst(order);
            // Updating total quantity on this line.
            Quantity newQuantity = new Quantity(this.getTotalQuantity().getQuantity() + order.getQuantity().getQuantity());
            this.totalQuantity = newQuantity;
        } else {
            throw new IllegalArgumentException("Order type doesn't match with line type.");
        }
    }
    /**
     * Remove/cancel an order to its corresponding line (same type stop/limit, same price).
     * Could be a stop or a limit order.
     * @param order The order to be removed.
     * @throws IllegalArgumentException If the order type doesn't match with the line type or if the order doesn't exist in the line.
     * @throws NullPointerException If the order is null.
     */
    public void cancelOrder(O order) throws IllegalArgumentException, NullPointerException {
        if (order == null)
            throw new NullPointerException("Order cannot be null.");

        if (order.getClass() != this.lineType.getClass())
            throw new IllegalArgumentException("Order type doesn't match with line type.");

        // Search for the order in the line.
        // O(n) needed.
        for (O lineOrder : orders) {
            if (lineOrder.getId() == order.getId()) {
                // Remove the order from the line.
                orders.remove(lineOrder);
                // Updating total quantity on this line.
                Quantity newQuantity = new Quantity(this.getTotalQuantity().getQuantity() - order.getQuantity().getQuantity());
                this.totalQuantity = newQuantity;
                return;
            }
        }
        throw new IllegalArgumentException("Order not found in the line.");
    }

    /**
     * Getter for the total number of orders on this line.
     * @return The number of orders on this line.
     */
    public Integer getOrdersNumber() {
        return orders.size();
    }
    /**
     * Getter for the total quantity of orders on this line.
     * @return The total quantity (sum of all quantity of each order) of orders on this line.
     */
    public Quantity getTotalQuantity() {
        return this.totalQuantity;
    }
    /**
     * Getter for the line's price.
     * @return The line's price.
     */
    public SpecificPrice getLinePrice() {
        return this.linePrice;
    }

    @Override
    public String toString() {
        return String.format("Price [%s] - Line Size [%s] - Total [%d]\n", this.linePrice.toString(), this.getTotalQuantity().toString(), this.getTotalQuantity().getQuantity() * this.linePrice.getValue());
    }
    /**
     * A to string method with all the orders contained in the line list.
     * The order are displayed in the short format.
     * @return A string with all the orders contained in the line list.
     */
    public String toStringWithOrders() {
        String lineStr = this.toString();
        lineStr += "\tOrders: -> ";
        for (O order : orders) {
            lineStr += order.toStringShort() + "\n";
        }
        return lineStr;
    }

}
