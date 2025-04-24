package CROSS.OrderBook;

import java.util.LinkedList;
import CROSS.Orders.LimitOrder;
import CROSS.Orders.MarketOrder;
import CROSS.Orders.Order;
import CROSS.Orders.StopMarketOrder;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;

/**
 * 
 * This class is used to represent a line in an order book.
 * 
 * It's used both by limit book and stop book. 
 * The line is omogeneus, this means all orders must have the same type (limit / stop).
 * This also means that all orders in the line must have the same direction (ASK / BID).
 * The line has a price value, the same for all orders in the line.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @param <GenericOrder> Order type, must be a subclass of Order, but it's intended to be either LimitOrder or StopMarketOrder.
 * 
 * @see Order
 * 
 * @see Quantity
 * @see SpecificPrice
 * 
 * @see OrderBook
 * 
 * @see LimitOrder
 * @see StopMarketOrder
 * @see MarketOrder
 * 
 */
public class OrderBookLine<GenericOrder extends Order> {
 
    // This quantity is the sum of all orders in this line.
    // It's useful to know the total quantity of orders in the line, without iterating over all orders each time to calculate it.
    // Used in market orders execution to check if an order is satisfabile.
    private Quantity totalQuantity = null;

    // LinkedList to keep the order in the same way they were added.
    // To execute the orders (matching algorithm) we use FIFO, so I give priority to insert and remove element from the head and tail.
    // Insert and execute orders from the head and tail is O(1).
    // These operations should be the most frequent.
    // Cancel an order is O(n) because we need to search for the order, but it's not a frequent operation (I hope).
    // Generic type order to handle both limit and stop orders.
    // All the orders in the line must have the same type (stop / limit).
    // All the orders in the line must have the same direction (ASK / BID).
    private LinkedList<GenericOrder> orders = null;

    // The price of the line.
    // All orders in this line must have the same price, corresponding to this line price.
    // This is also equal to the price key in the TreeMap of the order book.
    // This is also the price contained in the below lineType object, but restored for a quicker and mnemonic access.
    private final SpecificPrice linePrice;

    // To avoid confusion, I store the type of the line (stop / limit) and if this is an ASK or BID line (through the order price's type) when created.
    // All orders in the line must have all the same (these) types / attributes.
    // I will use this to check if the orders (to be handled by the methods) are coherent with the line attributes.
    // Instead of saving all the informations separately, I save the first order of the line, to have all the informations in one object.
    private final GenericOrder lineType;

    /**
     * 
     * Execute some coherence checks on a given order before handling it.
     * 
     * Coherence checks means that we ensure the order has the same attributes of the line.
     * 
     * Private method, since it's used only in the class.
     * 
     * Does not return anything, but throws an exception if the order has some problems.
     * 
     * Not synchronized on this object, since line price and line type cannot be changed after the line creation.
     * Synchronized on the order, since the order could be modified by other threads.
     * 
     * @param order The order to be checked.
     * 
     * @throws IllegalArgumentException If the order has some problems with the line attributes.
     * 
     */
    private void coherenceOrderChecks(GenericOrder order) throws IllegalArgumentException {

        synchronized (order) {

            // Checking order type, if allowed.
            if (order instanceof LimitOrder) {
                // OK, it's a limit order.
            }else if (order instanceof StopMarketOrder) {
                // OK, it's a stop order.
            } else {
                throw new IllegalArgumentException("Order, of an order book line, has a type not supported, use LimitOrder or StopMarketOrder.");
            }

            // THE ORDER HAS ONLY THE TYPE (LIMIT / STOP) TO CHECK, MADE BELOW.
            // THE ORDER HAS ALSO A PRICE, BUT THE PRICE'S ATTRIBUTES ARE CHECKED DIRECTLY WITH THE LINE PRICE BELOW.

            // Checking order class / line type class.
            // All orders on the line must have the same type, all stop or all limit.
            if (order.getClass() != this.lineType.getClass()) {
                throw new IllegalArgumentException("Order, of an order book line, has a class that doesn't match with the line type class.");
            }

            // PRICE ATTRIBUTES TO CHECK ARE MARKET, TYPE AND VALUE.
            // CHECKING THE GIVEN ORDER PRICE ATTRIBUTES WITH THE LINE PRICE.

            // Checking order price value / price line value match.
            // All orders must have the same price value corresponding to the line price value.
            if (linePrice.getValue().compareTo(order.getPrice().getValue()) != 0) {
                throw new IllegalArgumentException("Order, of an order book line, has a price with a value that doesn't match with line price value.");
            }

            // Checking order's market / market line price market match.
            // All orders must have the same market corresponding to the line price's market.
            if (order.getPrice().getMarket().compareTo(linePrice.getMarket()) != 0) {
                throw new IllegalArgumentException("Order, of an order book line, has a price with a market that doesn't match with line price market.");
            }

            // Checking order price type / price line type match.
            // All orders on the line must have the same price type, all ASK or all BID.
            if (order.getPrice().getType() != linePrice.getType()) {
                throw new IllegalArgumentException("Order, of an order book line, has a price with a type (ASK / BID) that doesn't match with line price type.");
            }

        }

    }

    /**
     * 
     * Constructor for the class.
     * 
     * The line is omogeneus, this means all orders must have the same type (stop / limit).
     * This also means that all orders in the line must have the same direction (ASK / BID).
     * The line has a price value, the same for all orders in the line.
     * 
     * The initial order is the first of the line.
     * It's used to save, memorize and initialize the attributes of the line.
     * These attribute cannot be changed after.
     * 
     * So, I assume that a line is created with at least one order.
     * 
     * Synchronized on the initial order to prevent modifications of it from other threads.
     * 
     * @param linePrice The price of the line.
     * @param initialOrder The first order to be added to the line.
     * 
     * @throws NullPointerException If the initial order or the line price are null.
     * @throws IllegalArgumentException If the initial order has some problems with the line attributes.
     * 
     */
    public OrderBookLine(SpecificPrice linePrice, GenericOrder initialOrder) throws NullPointerException, IllegalArgumentException {
        
        // Null checks.
        if (linePrice == null) {
            throw new NullPointerException("Line price of a new order book line cannot be null.");
        }
        if (initialOrder == null) {
            throw new NullPointerException("Initial order of a new order book line cannot be null.");
        }

        synchronized (initialOrder) {

            this.linePrice = new SpecificPrice(linePrice.getValue(), linePrice.getType(), linePrice.getMarket());
            // No copy, since we need to update the quantity during an order execution.
            this.lineType = initialOrder;

            // Coherece checks, after saving the line attributes.
            // Needed to check line price, line type (aka initial order) coherence.
            this.coherenceOrderChecks(initialOrder);

            // The total quantity is the quantity of the first order.
            this.totalQuantity = new Quantity(0);
            this.orders = new LinkedList<GenericOrder>();

            // Adding the initial order to the line.
            this.addOrder(initialOrder);

        }

    }

    // ORDERS MANAGEMENT
    /**
     * 
     * Add an order to its corresponding (this) line.
     * 
     * Could be a stop or a limit order.
     * The line is omogeneus, this means all orders must have the same type (stop / limit).
     * All the orders in the line must have the same direction (ASK / BID).
     * All the orders in the line must have the same price, corresponding to the line price.
     * 
     * The order is added at the beginning of the list, to follow a FIFO policy. E.g.:
     * NEW ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * Where ORDER X - N is the first added order and NEW ORDER X is the last added order.
     * 
     * A check if the order is already present in the list is omitted, because a O(n) operation would be needed, making useless the O(1) add operation.
     * 
     * Synchronized method to avoid concurrency problems, to protect the list of orders and the total quantity.
     * Synchronized on the order, since the order could be modified by other threads.
     * 
     * @param order The order to be added to the line.
     * 
     * @throws NullPointerException If the order to add to the line is null.
     * @throws IllegalArgumentException If the order has some problems with the line attributes.
     * 
     */
    public synchronized void addOrder(GenericOrder order) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (order == null)
            throw new NullPointerException("The order to add to an order book line cannot be null.");

        synchronized (order) {

            // Coherence checks.
            this.coherenceOrderChecks(order);

            // The order is added at the beginning of the list, to follow a FIFO policy.
            // A check if the order is already present in the list is omitted, because a O(n) operation would be needed, making useless the O(1) add operation.

            // No copy, since we need to update the quantity during an order execution.
            orders.addFirst(order);

            // Updating total quantity on this line.
            Quantity newQuantity = new Quantity(this.getTotalQuantity().getValue() + order.getQuantity().getValue());
            this.totalQuantity = newQuantity;

        }

    }
    /**
     * 
     * Extract the last order from the line (this one).
     * 
     * Could be a stop or a limit order.
     * The line is omogeneus, this means all orders must have the same type (stop / limit).
     * All the orders in the line must have the same direction (ASK / BID).
     * All the orders in the line must have the same price, corresponding to the line price.
     * 
     * Remove the order from the line if the remove it parameter is true, otherwise only return it.
     * 
     * The order is returned (and eventually removed) from the list, it's a O(1) operation.
     * 
     * Returns a REFERENCE, because is used to update the order's quantity in a matching operation.
     * 
     * This order is the first order added to the line to follow a FIFO policy, it's the one to be processed first.
     * E.g.: ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * The returned order is: X - N.
     * 
     * Synchronized method to avoid concurrency problems, to protect the list of orders.
     * 
      * NEED TO UPDATE MANUALLY THE LINE TOTAL QUANTITY AFTER THE REMOVAL OF THE ORDER.
     * 
     * @param removeIt If true, the order is removed from the line, otherwise it's only returned.
     * 
     * @return The last order in the line, the first added, or null if the line is empty. A reference to the order is returned needed to update the order's quantity in a matching operation.
     * 
     * @throws NullPointerException If the remove it parameter is null.
     * 
     */
    private synchronized GenericOrder extractLastOrder(Boolean removeIt) throws NullPointerException {

        // Null check.
        if (removeIt == null) {
            throw new NullPointerException("Remove it parameter, in the extraction of an order from an order book line, cannot be null.");
        }

        // Empty line check.
        if (orders.isEmpty()) {
            return null;
        }

        GenericOrder order = orders.getLast();
        if (removeIt) {

            // Remove the order from the line.
            orders.removeLast();
        
        }

        return order;

    }
    /**
     * 
     * Cancel an order to its corresponding (this) line.
     * 
     * Could be a stop or a limit order.
     * The line is omogeneus, this means all orders must have the same type (stop / limit).
     * All the orders in the line must have the same direction (ASK / BID).
     * All the orders in the line must have the same price, corresponding to the line price.
     * 
     * It's a O(n) operation.
     * 
     * Synchronized method to avoid concurrency problems, to protect the list of orders and the total quantity.
     * Synchronized on the order, since the order could be modified by other threads.
     * 
     * @param order The order to be cancelled in the line.
     * 
     * @throws NullPointerException If the order to cancel in the line is null.
     * @throws RuntimeException If the line is empty.
     * @throws IllegalArgumentException If the order to cancel is not present in the line.
     * 
     */
    public synchronized void cancelOrder(GenericOrder order) throws NullPointerException, RuntimeException, IllegalArgumentException {

        // Null check.
        if (order == null) {
            throw new NullPointerException("The order to cancel in an order book line cannot be null.");
        }

        // Empty line check.
        if (orders.isEmpty()) {
            throw new RuntimeException("Cancelling an order in an order book line with no orders.");
        }

        synchronized (order) {

            // Searching for the order in the list.
            for (GenericOrder o : orders) {
                if (o.compareTo(order) == 0) {
                    // Found the order, remove it.
                    orders.remove(o);
                    // Update the total quantity on this line.
                    Quantity newQuantity = new Quantity(this.getTotalQuantity().getValue() - order.getQuantity().getValue());
                    this.totalQuantity = newQuantity;
                    return;
                }
            }

            // Order not found in the list.
            throw new IllegalArgumentException("The order to cancel in an order book line is not present in the line.");

        }

    }

    // ORDERS EXECUTION
    /**
     * 
     * Execute a LIMIT order PRESENT ON THIS LINE, matching it with a given market order.
     * 
     * The limit order executed is the first added to the line, the last in the list, following a FIFO policy.
     * E.g.: ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * The executed order is X - N.
     * 
     * This method matches ONLY THE LAST IN THE LIST LIMIT order ON THIS LINE with the given market order.
     * So this method MAY HAVE TO BE EXECUTED MULTIPLE TIMES from the book (the caller) until the market order is fully filled.
     * ALSO NOTE THE NEED TO CHECK BEFORE IF THE QUANTITY OF ALL ORDERS ON ALL LINES (IN THE ORDERBOOK) IS ENOUGH TO FILL THE WHOLE MARKET ORDER, OTHERWISE THE MARKET ORDER WILL BE PARTIALLY FILLED AND WE CANNOT "GO BACK IN TIME" TO CANCEL THE FILLED LIMIT ORDERS BY THE CALLS TO THIS METHOD.
     * 
     * Synchronized method to avoid concurrency problems.
     * Synchronized on the line, but also on the market order, to avoid order modifications from other threads.
     * 
     * If this method is called on a stop line, it will throw a RuntimeException.
     * 
     * @param marketOrder The market order to be executed.
     * 
     * @return 0 if the market order is fully filled, but the limit order not. 1 if the market order is partially filled, but the limit order is fully filled. 2 if both orders are fully filled.
     * 
     * @throws NullPointerException If the market order is null.
     * @throws RuntimeException If the line type doesn't match with LimitOrder or if the line is empty.
     * 
     */
    public synchronized Integer executeMarketOrderOnLimitLine(MarketOrder marketOrder) throws NullPointerException, RuntimeException {

        // Null checks.
        if (marketOrder == null) {
            throw new NullPointerException("The market order to execute in an order book line cannot be null.");
        }

        // No copy, since we need to update the quantity during an order execution.

        // Maybe not needed, since the method setQuantity() is synchronized, but better to be sure (perhaps for other operations that could be added in the future).
        synchronized (marketOrder) {

            // Check if this line is a limit line.
            if (this.lineType.getClass() != LimitOrder.class) {
                throw new RuntimeException("Line type where execute the market order doesn't match with LimitOrder class.");
            }

            // CANNOT USE THE COHERENCE CHECKS METHOD BECAUSE THE ORDER IS A MARKET ORDER, NOT A LIMIT OR STOP ORDER TO COMPARE WITH THE LINE.

            // Line price checks.
            // Price value check.
            if (marketOrder.getPrice().getValue().compareTo(this.linePrice.getValue()) != 0)
                throw new IllegalArgumentException("The market order to execute in an order book line has a price value that doesn't match with line price value.");
            // Type check is after, is reversed for market orders.
            // Price market check.
            if (marketOrder.getPrice().getMarket().compareTo(this.linePrice.getMarket()) != 0)
                throw new IllegalArgumentException("The market order to execute in an order book line has a price market that doesn't match with line price market.");

            // Reversing line type for market orders.
            PriceType linePriceTypeReversed = this.linePrice.getType();
            if (linePriceTypeReversed == PriceType.ASK)
                linePriceTypeReversed = PriceType.BID;
            else if (linePriceTypeReversed == PriceType.BID)
                linePriceTypeReversed = PriceType.ASK;
            // Reverse logic for market orders.
            // Checking order's price type match with reversed line price type.
            if (marketOrder.getPrice().getType() != linePriceTypeReversed) {
                throw new IllegalArgumentException("The market order to execute in an order book line has a price type (ASK / BID) that doesn't match with line price type.");
            }
            
            LimitOrder currentMatchedOrder = null;
            // This casting is safe because we checked the line type before.
            // No remove it, since there could be still available quantity to be filled after the matching with the market order.
            currentMatchedOrder = (LimitOrder) this.extractLastOrder(false);
            if (currentMatchedOrder == null) {
                // A line with no orders, MUST NOT EXIST.
                throw new RuntimeException("Executing a market order on a order book line with no orders.");
            }

            Quantity updatedQuantity = null;
            Integer resultCode = -1;
            if (currentMatchedOrder.getQuantity().getValue() > marketOrder.getQuantity().getValue()) {
                // The LIMIT order is partially filled, the MARKET order is fully filled.

                // Updating limit order.
                updatedQuantity = new Quantity(currentMatchedOrder.getQuantity().getValue() - marketOrder.getQuantity().getValue());
                currentMatchedOrder.setQuantity(updatedQuantity);

                // Updating market order.
                marketOrder.setQuantity(new Quantity(0));

                resultCode = 0;

            } else if (currentMatchedOrder.getQuantity().getValue() < marketOrder.getQuantity().getValue()) {
                // The LIMIT order is fully filled, the MARKET order is partially filled.

                // Updating limit order.
                currentMatchedOrder.setQuantity(new Quantity(0));

                // Updating market order.
                updatedQuantity = new Quantity(marketOrder.getQuantity().getValue() - currentMatchedOrder.getQuantity().getValue());
                marketOrder.setQuantity(updatedQuantity);

                resultCode = 1;

            } else {
                // The LIMIT order is fully filled, the MARKET order is fully filled.

                // Updating limit order.
                currentMatchedOrder.setQuantity(new Quantity(0));

                // Updating market order.
                marketOrder.setQuantity(new Quantity(0));

                resultCode = 2;

            }

            // Updating total quantity on this line.
            this.totalQuantity = new Quantity(this.getTotalQuantity().getValue() - currentMatchedOrder.getQuantity().getValue());

            if (currentMatchedOrder.getQuantity().getValue() == 0) {
                // The order is fully filled, remove it from the line.
                this.extractLastOrder(true);
            }

            return resultCode;

        }

    }
    /**
     * 
     * Execute a STOP order PRESENT ON THIS LINE.
     * 
     * The stop order executed is the first added to the line, the last in the list, following a FIFO policy.
     * E.g.: ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * The executed order is X - N.
     * 
     * This method EXECUTE ONLY THE LAST IN THE LIST STOP order ON THIS LINE.
     * So this method MAY HAVE TO BE EXECUTED MULTIPLE TIMES from the book (the caller) until all stop orders are executed or the current price in the market is different from the stop order price.
     * 
     * The execution is done only if the current price in the market is equal the stop order price.
     * 
     * The execution consist in creating a market order with the same type, quantity, price and user of the stop order executed and returning it.
     * This must be executed by the caller.
     * 
     * Synchronized method to avoid concurrency problems.
     * Synchronized on the line, but also on the order book, to avoid order modifications from other threads, order book extends the market class, so it has the actual price that could be modified by other threads.
     * 
     * If this method is called on a limit line, it will throw a RuntimeException.
     * 
     * @param orderBook The order book to use to execute a stop order in an order book line. The stop order from this line (and thus the line itself) must be present in the order book.
     * 
     * @return The market order created from the stop order executed.
     * 
     * @throws NullPointerException If the order book to use to execute a stop order in an order book line is null.
     * @throws RuntimeException If the line type doesn't match with StopMarketOrder, if the order book market doesn't match with the line price market or if the current market price doesn't match with the stop order price.
     * 
     */
    public synchronized MarketOrder executeStopOrderFromStopLine(OrderBook orderBook) throws RuntimeException, NullPointerException {

        // Null check.
        if (orderBook == null) {
            throw new NullPointerException("The order book, to be used to execute a stop order in an order book line, cannot be null.");
        }

        synchronized (orderBook) {

            // Check if this line is a stop line.
            if (this.lineType.getClass() != StopMarketOrder.class) {
                throw new RuntimeException("Line type where execute a stop order doesn't match with StopMarketOrder class.");
            }

            // OrderBook market / line price market check.
            // Checked in this way since the order book extends the market class.
            if (orderBook.getPrimaryCurrency() != this.linePrice.getMarket().getPrimaryCurrency() || orderBook.getSecondaryCurrency() != this.linePrice.getMarket().getSecondaryCurrency()) {
                throw new RuntimeException("The order book, to use to execute a stop order in an order book line, has a market that doesn't match with the line price market.");
            }

            // Checking if the current market price is equal to the stop order price.
            if ((this.linePrice.getValue() != orderBook.getActualPriceAsk().getValue() && this.linePrice.getType() == PriceType.ASK) || (this.linePrice.getValue() != orderBook.getActualPriceBid().getValue() && this.linePrice.getType() == PriceType.BID)) {
                throw new RuntimeException("The current market price doesn't match with the stop order price.");
            }

            // Safe cast because we checked the line type before.
            // Removing the stop order from the line ALWAYS, since it's executed.
            StopMarketOrder toProcess = (StopMarketOrder) this.extractLastOrder(true);
            if (toProcess == null) {
                // A line with no orders, MUST NOT EXIST.
                throw new RuntimeException("Executing a stop order on a order book line with no orders.");
            }

            // Update total quantity on this line.
            this.totalQuantity = new Quantity(this.getTotalQuantity().getValue() - toProcess.getQuantity().getValue());

            // Coherence checks executed when the stop order was added to the line in the addOrder() method.

            // Converting the stop order to a market order.
            MarketOrder order = new MarketOrder(toProcess.getMarket(), toProcess.getPrice().getType(), toProcess.getQuantity(), toProcess.getUser());

            // Using a new id for the market order, since mantaining the old id generates problems in the database.

            // Stop order removed from the line (already) before above.

            return order;

        }

    }

    // GETTERS
    /**
     * 
     * Getter for the total number of orders on this line.
     * 
     * @return The number of orders on this line as an Integer.
     * 
     */
    public Integer getOrdersNumber() {

        return orders.size();

    }
    /**
     * 
     * Getter for the total quantity of all the orders on this line.
     * 
     * @return The total quantity (sum of all quantity of each order) of orders on this line.
     * 
     */
    public Quantity getTotalQuantity() {

        return new Quantity(this.totalQuantity.getValue());

    }
    /**
     * 
     * Getter for the line's price.
     * 
     * @return The line's price as a SpecificPrice object.
     * 
     */
    public SpecificPrice getLinePrice() {

        return new SpecificPrice(this.linePrice.getValue(), this.linePrice.getType(), this.linePrice.getMarket());
        
    }

    // TOSTRING METHODS
    @Override
    public synchronized String toString() {

        // Synchronized method to avoid concurrency problems with the total quantity.
        return String.format("Line Type [%s|%s] - Price [%s] - Line Size [%s] - Total [%d]", this.lineType.getClass().getSimpleName().toString(), this.linePrice.getType().name().toUpperCase(), this.linePrice.getValue().toString(), this.getTotalQuantity().toString(), this.getTotalQuantity().getValue() * this.getLinePrice().getValue());

    }
    /**
     * 
     * A to string method with all the orders contained in the line list.
     * The order are displayed in the short format.
     * 
     * Synchronized method to avoid concurrency problems.
     * 
     * @return A string with all the orders contained in the line list in the short format.
     * 
     */
    public synchronized String toStringWithOrders() {

        String lineStr = this.toString();
        lineStr += "\n\tOrders: -> ";
        String spaces = "           ";
        Boolean start = true;
        for (GenericOrder order : orders) {
            // First tab to align with the beginning of "Orders: -> ".
            // Spaces to align with the end of "Orders: -> ".
            // The toStringShort() method is synchronized on the order itself.
            if (!start) lineStr += "\t" + spaces + order.toStringShort() + "\n";
            else {
                lineStr += order.toStringShort() + "\n";
                start = false;
            }
        }

        return lineStr;
        
    }
    

}
