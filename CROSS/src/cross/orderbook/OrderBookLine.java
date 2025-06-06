package cross.orderbook;

import java.io.IOException;
import java.util.LinkedList;

import com.google.gson.JsonSyntaxException;

import cross.api.notifications.Notification;
import cross.api.notifications.Trade;
import cross.exceptions.InvalidOrder;
import cross.orders.LimitOrder;
import cross.orders.MarketOrder;
import cross.orders.Order;
import cross.orders.StopOrder;
import cross.orders.db.Orders;
import cross.types.Quantity;
import cross.types.price.PriceType;
import cross.types.price.SpecificPrice;
import cross.utils.Separator;

/**
 *
 * This class is used to represent a line in an order book.
 *
 * It's used both by limit book and stop book.
 * 
 * The line is omogeneus, this means all orders must have the same type (limit / stop).
 * This also means that all orders in the line must have the same direction (ASK / BID).
 * The line has a price value, the same for all orders in the line.
 * All these attributes are extracted from the first order added to the line and cannot be changed after.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @param <GenericOrder> Order type, must be a subclass of Order, but it's intended to be either LimitOrder or StopOrder, so not MarketOrder.
 *
 * @see Order
 *
 * @see Quantity
 * @see SpecificPrice
 *
 * @see OrderBook
 *
 * @see LimitOrder
 * @see StopOrder
 * @see MarketOrder
 * 
 * @see Separator
 *
 */
public final class OrderBookLine<GenericOrder extends Order> {
    
    // This quantity is the sum of all orders quantities in this line.
    // It's useful to know the total quantity of orders in the line, without iterating over all orders each time to calculate it.
    // Used in market orders execution to check if an order is satisfiable.
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

    // THIS IS THE MOST IMPORTANT ATTRIBUTE OF THE LINE AND CANNOT BE CHANGED AFTER THE LINE IS CREATED.
    // THIS IS ALSO NOT NON-EXCHANGEABLE WITH OTHERS ATTRIBUTES.
    // The type of the line.
    // All orders in this line must have the same type (stop / limit).
    private final GenericOrder lineType;

    // The price of the line.
    // All orders in this line must have the same price, corresponding to this line price.
    // With price we mean not only the price value, but also the price type, primary and secondary currencies.
    // This is also equal to the price key in the TreeMap of the order book.
    // This is also the price contained in the above lineType object, but restored for a quicker and mnemonic access.
    private final SpecificPrice linePrice;

    // To avoid confusion, I store the type of the line (stop / limit) and if this is an ASK or BID line when created, plus the price value and its currencies.
    // All orders in the line must have all the same (these) types / attributes.
    // I will use this to check if the orders (to be handled by the methods) are coherent with the line attributes.
    // These informations are saved from the first order added to the line.

    /*
     *
     * The approach of using the first order added to extract all the line informations is necessary. 
     * 
     * Using simply the generic class type is not possible, since this cannot be inferred / readed at runtime.
     * 
     * Using the lineType directly without the first order is not possible, since the lineType is initialized to null and its type is not set.
     * 
     * Using OrderType enum is possible, but then it's possible to do something like this:
     * OrderBookLine<LimitOrder> limitLine = new OrderBookLine<LimitOrder>(price, OrderType.STOP);
     * And this is misleading and orrible.
     * 
     * So is needed that the generic type must be present also in the constructor, and the only way to do this is to use the first order added to the line.
     * 
     */

    /**
     *
     * Constructor for the class.
     *
     * The line is omogeneus, this means all orders must have the same type (stop / limit).
     * This also means that all orders in the line must have the same direction (ASK / BID).
     * The line has a price value, the same for all orders in the line.
     * All these attributes are extracted from the first order added to the line and cannot be changed after.
     * 
     * The first order is the first of the line.
     * It's used to save, memorize and initialize the attributes of the line.
     * These attribute cannot be changed after.
     * 
     * So, I assume that a line is created with at least one order.
     * 
     * Synchronized on the first order to prevent modifications of it from other threads.
     *
     * @param firstOrder The first order to be added to the line, used to initialize the line attributes.
     *
     * @throws NullPointerException If first order is null.
     * @throws IllegalArgumentException If the order has some problems with the line attributes.
     *
     */
    public OrderBookLine(GenericOrder firstOrder) throws NullPointerException, IllegalArgumentException {

        // Null checks.
        if (firstOrder == null) {
            throw new NullPointerException("First order of a new order book line cannot be null.");
        }

        // Intializing the line attributes.
        this.totalQuantity = new Quantity(0);
        this.orders = new LinkedList<>();

        synchronized (firstOrder) {

            if (firstOrder instanceof MarketOrder == false)
                this.linePrice = firstOrder.getPrice(); 
            else
                this.linePrice = ((MarketOrder) firstOrder).getExecutionPrice();

            // Coherece checks, after saving the line price attribute.
            this.coherenceOrderChecks(firstOrder);

            this.lineType = firstOrder;

            // Adding the initial order to the line.
            this.addOrder(firstOrder);

        }

    }

    // SUPPORT METHODS
    /**
     *
     * Executes some coherence checks on a given order before handling it.
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
            }else if (order instanceof StopOrder) {
                // OK, it's a stop order.
            } else {
                throw new IllegalArgumentException("Order, of an order book line, has a type not supported, use LimitOrder or StopOrder.");
            }

            // Checking order class / line type class, if coherent with the line.
            // All orders on the line must have the same type, all stop or all limit.
            if (this.lineType != null && order.getClass() != this.lineType.getClass()) {
                throw new IllegalArgumentException("Order, of an order book line, has a class that doesn't match with the line type class.");
            }

            // Checking order price value, type, primary and secondary currencies / price line value, type, primary and secondary currencies match.
            // All orders must have the same price value, type, primary and secondary currencies corresponding to the line price value, type, primary and secondary currencies.
            if (order.getPrice().compareTo(linePrice) != 0) {
                throw new IllegalArgumentException("Order, of an order book line, has a price with a value or a type or a primary or secondary currency that doesn't match with line price value or type or primary or secondary currency.");
            }

        }

    }
    /**
     *
     * Extracts the last order from the line (this one).
     *
     * Could be a stop or a limit order.
     * The line is omogeneus, this means all orders must have the same type (stop / limit).
     * All the orders in the line must have the same direction (ASK / BID).
     * All the orders in the line must have the same price, corresponding to the line price.
     *
     * Removes the order from the line if the remove it parameter is true, otherwise only return it.
     *
     * The order is returned (and eventually removed) from the list in O(1) time.
     *
     * This order is the first order added to the line to follow a FIFO policy, it's the one to be processed first.
     * E.g.: ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * The returned order is: X - N.
     *
     * Synchronized method to avoid concurrency problems, to protect the list of orders.
     *
     * WARNING: NEED TO UPDATE MANUALLY THE LINE TOTAL QUANTITY AFTER THE REMOVAL OF THE ORDER.
     *
     * @param removeIt If true, the order is removed from the line, otherwise it's only returned.
     *
     * @return The last order in the line, the first added, or null if the line is empty.
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

    // ORDERS MANAGEMENT
    /**
     *
     * Adds an order to its corresponding (this) line.
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

            orders.addFirst(order);

            // Updating total quantity on this line.
            Quantity newQuantity = new Quantity(this.getTotalQuantity().getValue() + order.getQuantity().getValue());
            this.totalQuantity = newQuantity;

        }

    }
    /**
     *
     * Cancels an order from its corresponding (this) line.
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
     * @param order The order to be cancelled from the line.
     *
     * @throws NullPointerException If the order to cancel from the line is null.
     * @throws IllegalArgumentException If the order to cancel is not present in the line.
     *
     */
    public synchronized void cancelOrder(GenericOrder order) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (order == null) {
            throw new NullPointerException("The order to cancel from an order book line cannot be null.");
        }

        synchronized (order) {

            // Searching for the order in the list.
            for (GenericOrder o : orders) {
                if (o.compareTo(order) == 0) {
                    // Found the order, remove it.
                    int index = orders.indexOf(o);
                    o = orders.remove(index);
                    // Update the total quantity on this line.
                    Quantity newQuantity = new Quantity(this.getTotalQuantity().getValue() - order.getQuantity().getValue());
                    this.totalQuantity = newQuantity;
                    return;
                }
            }

            // Order not found in the list.
            throw new IllegalArgumentException("The order to cancel from an order book line is not present in the line.");

        }

    }
    /**
     * 
     * Gets an order from the line by its id.
     * 
     * @param orderId The id of the order to get from the line.
     * 
     * @return The order with the given id, or null if the order is not present in the line.
     * 
     * @throws NullPointerException If the order id is null.
     * 
     */
    public synchronized GenericOrder getOrderById(Long orderId) throws NullPointerException {

        // Null check.
        if (orderId == null) {
            throw new NullPointerException("The order id to be used to get an order from an order book line cannot be null.");
        }

        for (GenericOrder o : orders) {
            if (o.getId().equals(orderId)) {
                return o;
            }
        }

        return null;

    }

    // ORDERS EXECUTION
    /**
     *
     * Executes a LIMIT order PRESENT ON THIS LINE, matching it with a given market order.
     *
     * The limit order executed is the first added to the line, the last in the list, following a FIFO policy.
     * E.g.: ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * The executed order is X - N.
     *
     * This method matches ONLY THE LAST IN THE LIST LIMIT order ON THIS LINE with the given market order.
     * So this method MAY HAVE TO BE EXECUTED MULTIPLE TIMES from the book (the caller) until the market order is fully filled.
     * ALSO NOTE THE NEED TO CHECK BEFORE IF THE QUANTITY OF ALL ORDERS ON ALL LINES (IN THE ORDERBOOK) IS ENOUGH TO FILL THE WHOLE MARKET ORDER, OTHERWISE THE MARKET ORDER WILL BE PARTIALLY FILLED AND WE CANNOT "GO BACK IN TIME" TO CANCEL THE FILLED LIMIT ORDERS BY THE CALLS TO THIS METHOD.
     *
     * This method removes the executed limit order from the line, if it's fully filled.
     * 
     * If this method is called on a stop line, it will throw a RuntimeException.
     * 
     * The method modifies the quantities of the orders.
     *
     * Synchronized method to avoid concurrency problems on the line.
     * Synchronized also on the market order, to avoid order modifications from other threads.
     *
     * @param marketOrder The market order to be executed.
     *
     * @return An Integer with the following semantic values: 0 if the market order is fully filled, but the limit order only partially. 1 if the market order is partially filled, but the limit order is fully filled. 2 if both orders are fully filled.
     *
     * @throws NullPointerException If the market order is null.
     * @throws RuntimeException If the line type doesn't match with LimitOrder.
     * @throws IllegalStateException If the line is empty.
     * @throws IllegalArgumentException If the market order has some problems with the line attributes.
     * @throws InvalidOrder If an error occurs while adding the limit order to the database.
     * 
     * 
     */
    public synchronized Integer executeMarketOrderOnLimitLine(MarketOrder marketOrder, Notification notification) throws NullPointerException, RuntimeException, IllegalStateException, IllegalArgumentException, InvalidOrder {

        // Null checks.
        if (marketOrder == null) {
            throw new NullPointerException("The market order to execute in an order book line cannot be null.");
        }
        if (notification == null) {
            throw new NullPointerException("The notification to be used to execute a market order on a limit line cannot be null.");
        }

        synchronized (marketOrder) {

            // Check if this line is a limit line.
            if (this.getLineType() != LimitOrder.class) {
                throw new RuntimeException("Line type where execute the market order doesn't match with LimitOrder, it's not a limit line.");
            }

            // CANNOT USE THE COHERENCE CHECKS METHOD BECAUSE THE ORDER IS A MARKET ORDER, NOT A LIMIT OR STOP ORDER TO COMPARE WITH THE LINE.

            // Line price checks.
            // Price value check.
            if (marketOrder.getExecutionPrice().getValue().compareTo(this.linePrice.getValue()) != 0)
                throw new IllegalArgumentException("The market order to execute in an order book line has a price value that doesn't match with line price value.");
            // Type check is after, is reversed for market orders.
            // Currencies check.
            if (marketOrder.getMarketOrderPrimaryCurrency().compareTo(this.linePrice.getPrimaryCurrency()) != 0 || marketOrder.getMarketOrderSecondaryCurrency().compareTo(this.linePrice.getSecondaryCurrency()) != 0)
                throw new IllegalArgumentException("The market order to execute in an order book line has a price with a primary or secondary currencies that don't match with line price primary or secondary currencies.");

            // Reversing line type for market orders.
            PriceType linePriceTypeReversed = this.linePrice.getType();
            if (linePriceTypeReversed == PriceType.ASK)
                linePriceTypeReversed = PriceType.BID;
            else if (linePriceTypeReversed == PriceType.BID)
                linePriceTypeReversed = PriceType.ASK;
            // Reverse logic for market orders.
            // Checking order's price type match with reversed line price type.
            if (marketOrder.getMarketOrderPriceType() != linePriceTypeReversed) {
                throw new IllegalArgumentException("The market order to execute in an order book line has a price type (ASK / BID) that doesn't match with line price type.");
            }

            LimitOrder currentMatchedOrder;
            // This casting is safe because we checked the line type before.
            // No remove it, since there could be still available quantity to be filled after the matching with the market order.
            currentMatchedOrder = (LimitOrder) this.extractLastOrder(false);
            if (currentMatchedOrder == null) {
                // A line with no orders, MUST NOT EXIST.
                throw new IllegalStateException("Executing a market order on a order book line with no orders.");
            }


            Integer resultCode;
            Quantity updatedQuantity;
            Long timestamp = System.currentTimeMillis();
            if (currentMatchedOrder.getQuantity().getValue() > marketOrder.getQuantity().getValue()) {
                // The LIMIT order is partially filled, the MARKET order is fully filled.

                // Updating limit order.
                updatedQuantity = new Quantity(currentMatchedOrder.getQuantity().getValue() - marketOrder.getQuantity().getValue());
                try {
                    currentMatchedOrder.setTimestamp(timestamp);
                    currentMatchedOrder.setQuantity(new Quantity(marketOrder.getQuantity().getValue()));
                    Trade trade = new Trade(currentMatchedOrder);
                    notification.addTrade(trade);
                    Orders.addOrder(currentMatchedOrder, true, true);
                } catch (InvalidOrder | IOException | IllegalStateException | NoSuchMethodException | NullPointerException ex) {
                    throw new InvalidOrder("Error adding order to the database.");
                }
                currentMatchedOrder.setQuantity(updatedQuantity);

                this.totalQuantity = new Quantity(this.getTotalQuantity().getValue() - marketOrder.getQuantity().getValue());

                // Updating market order.
                try {
                    marketOrder.setTimestamp(timestamp);
                    Trade trade = new Trade(marketOrder);
                    notification.addTrade(trade);
                    Orders.addOrder(marketOrder, true, true);
                } catch (InvalidOrder | IOException | IllegalStateException | NoSuchMethodException | NullPointerException ex) {
                    throw new InvalidOrder("Error adding order to the database.");
                }
                marketOrder.setQuantity(new Quantity(0));

                resultCode = 0;

            } else if (currentMatchedOrder.getQuantity().getValue() < marketOrder.getQuantity().getValue()) {
                // The LIMIT order is fully filled, the MARKET order is partially filled.

                // Updating market order.
                updatedQuantity = new Quantity(marketOrder.getQuantity().getValue() - currentMatchedOrder.getQuantity().getValue());
                try {
                    marketOrder.setTimestamp(timestamp);
                    marketOrder.setQuantity(new Quantity(currentMatchedOrder.getQuantity().getValue()));
                    Trade trade = new Trade(marketOrder);
                    notification.addTrade(trade);
                    Orders.addOrder(marketOrder, true, true);
                } catch (InvalidOrder | IOException | IllegalStateException | NoSuchMethodException | NullPointerException ex) {
                    throw new InvalidOrder("Error adding order to the database.");
                }
                marketOrder.setQuantity(updatedQuantity);

                this.totalQuantity = new Quantity(this.getTotalQuantity().getValue() - currentMatchedOrder.getQuantity().getValue());

                // Updating limit order.
                try {
                    currentMatchedOrder.setTimestamp(timestamp);
                    Trade trade = new Trade(currentMatchedOrder);
                    notification.addTrade(trade);
                    Orders.addOrder(currentMatchedOrder, true, true);
                } catch (InvalidOrder | IOException | IllegalStateException | NoSuchMethodException | NullPointerException ex) {
                    throw new InvalidOrder("Error adding order to the database.");
                }
                currentMatchedOrder.setQuantity(new Quantity(0));

                resultCode = 1;

            } else {
                // The LIMIT order is fully filled, the MARKET order is fully filled.

                // Updating limit order.
                try {
                    currentMatchedOrder.setTimestamp(timestamp);
                    Trade trade = new Trade(currentMatchedOrder);
                    notification.addTrade(trade);
                    Orders.addOrder(currentMatchedOrder, true, true);
                } catch (InvalidOrder | IOException | IllegalStateException | NoSuchMethodException | NullPointerException ex) {
                    throw new InvalidOrder("Error adding order to the database.");
                }
                currentMatchedOrder.setQuantity(new Quantity(0));

                this.totalQuantity = new Quantity(this.getTotalQuantity().getValue() - marketOrder.getQuantity().getValue());

                // Updating market order.
                try {
                    marketOrder.setTimestamp(timestamp);
                    Trade trade = new Trade(marketOrder);
                    notification.addTrade(trade);
                    Orders.addOrder(marketOrder, true, true);
                } catch (InvalidOrder | IOException | IllegalStateException | NoSuchMethodException | NullPointerException ex) {
                    throw new InvalidOrder("Error adding order to the database.");
                }
                marketOrder.setQuantity(new Quantity(0));

                resultCode = 2;

            }


            if (currentMatchedOrder.getQuantity().getValue() == 0) {
                // The order is fully filled, remove it from the line.
                this.extractLastOrder(true);
            }

            return resultCode;

        }

    }
    /**
     *
     * Executes a STOP order PRESENT ON THIS LINE.
     *
     * The stop order executed is the first added to the line, the last in the list, following a FIFO policy.
     * E.g.: ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * The executed order is X - N.
     *
     * This method EXECUTES ONLY THE LAST IN THE LIST STOP order ON THIS LINE.
     * So this method MAY HAVE TO BE EXECUTED MULTIPLE TIMES from the book (the caller) until all stop orders are executed or the current price in the market is different from the stop order price.
     *
     * The execution is done only if the current price in the order book is equal the stop order price.
     * 
     * This method removes the executed stop order from the line.
     *
     * The execution consist in creating a market order with the same type, quantity, price and user of the stop order executed and returning it.
     * This must be executed by the caller.
     * 
     * If this method is called on a limit line, it will throw a RuntimeException.
     *
     * Synchronized method to avoid concurrency problems on the line.
     * Synchronized also on the order book to avoid modifications of the best prices from other threads.
     *
     * @param orderBook The order book to use to execute a stop order in an order book line. The stop order from this line (and thus the line itself) must be present in the order book.
     *
     * @return The market order created from the stop order executed.
     *
     * @throws NullPointerException If the order book to use to execute a stop order in an order book stop line is null.
     * @throws RuntimeException If the line type doesn't match with StopOrder.
     * @throws IllegalArgumentException If the order book doesn't contain this line.
     * @throws IllegalStateException If the order book is void, and the best prices are not set or the current market price doesn't match with the stop order price or the line is empty.
     * 
     */
    public synchronized MarketOrder executeStopOrderFromStopLine(OrderBook orderBook) throws RuntimeException, NullPointerException, IllegalArgumentException, IllegalStateException {

        // Null check.
        if (orderBook == null) {
            throw new NullPointerException("The order book, to be used to execute a stop order in an order book stop line, cannot be null.");
        }

        synchronized (orderBook) {

            // Check if this line is a stop line.
            if (this.lineType.getClass() != StopOrder.class) {
                throw new RuntimeException("Line type where execute a stop order doesn't match with StopOrder class.");
            }

            // CANNOT USE THE COHERENCE CHECKS METHOD BECAUSE THE ORDER IS A MARKET ORDER, NOT A LIMIT OR STOP ORDER TO COMPARE WITH THE LINE.

            // Check if the line exists in the order book.
            OrderBookLine<StopOrder> stopOrderLine = orderBook.getStopBookLine(this.linePrice);
            if (stopOrderLine == null || !orderBook.containsLine(stopOrderLine)) {
                throw new IllegalArgumentException("The order book to be used to execute a stop order in an order book stop line doesn't contain the line itself.");
            }

            // Safe cast because we checked the line type before.
            // Removing the stop order from the line ALWAYS, since it's executed.
            StopOrder toProcess = (StopOrder) this.extractLastOrder(true);
            if (toProcess == null) {
                // A line with no orders, MUST NOT EXIST.
                throw new IllegalStateException("Executing a stop order on a order book line with no orders.");
            }

            if (orderBook.getVerboseLogging()) {
                System.out.println("\n\n\n\n\n\n\n\n\n");
                System.out.println("DEBUG: PREPARING to execute a STOP order, ADDED to the list: " + toProcess.toString());
                System.out.println("\n\n\n\n\n\n\n\n\n");
            }

            // Update total quantity on this line.
            this.totalQuantity = new Quantity(this.getTotalQuantity().getValue() - toProcess.getQuantity().getValue());

            // Coherence checks executed when the stop order was added to the line in the addOrder() method.

            // Converting the stop order to a market order.
            MarketOrder order = new MarketOrder(toProcess.getPrice().getType(), toProcess.getPrice().getPrimaryCurrency(), toProcess.getPrice().getSecondaryCurrency(), toProcess.getQuantity());

            // Using a new id for the market order, since mantaining the old id generates problems in the database.
            order.setComingFromStopOrderId(toProcess.getId().longValue());
            if (toProcess.getUser() != null) {
                order.setUser(toProcess.getUser());
            }

            try {
                Orders.addOrder(toProcess, true, false);
            } catch (JsonSyntaxException | NullPointerException | NoSuchMethodException | IllegalStateException | InvalidOrder | IOException ex) {
            }
            
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
     * @return The total quantity (sum of all quantity of each order) of orders on this line as a Quantity object.
     *
     */
    public Quantity getTotalQuantity() {

        return this.totalQuantity;

    }
    /**
     *
     * Getter for the line's price.
     *
     * @return The line's price as a SpecificPrice object.
     *
     */
    public SpecificPrice getLinePrice() {

        return this.linePrice;

    }
    /**
     *
     * Getter for the line's type.
     *
     * @return The line's type as a Class object.
     *
     */
    public Class<?> getLineType() {

        return this.lineType.getClass();

    }

    // TOSTRING METHODS
    @Override
    public synchronized String toString() {

        // Synchronized method to avoid concurrency problems with the total quantity.
        return String.format("Line Type [%s|%s] - Price Value [%s] - Line Size [%s] - Total [%d]", this.getLineType().getSimpleName(), this.linePrice.getType().name().toUpperCase(), this.linePrice.getValue().toString(), this.getTotalQuantity().toString(), this.getTotalQuantity().getValue() * this.getLinePrice().getValue());

    }
    /**
     *
     * A to string method with all the orders contained in the line list.
     * The orders are displayed in the short format.
     *
     * Synchronized method to avoid concurrency problems.
     *
     * @return A string with all the orders contained in the line list in the short format.
     *
     */
    public synchronized String toStringWithOrders() {

        String lineStr = this.toString();

        Separator sep = new Separator("%", lineStr.length());

        lineStr = sep.toString() + "\n" + lineStr + "\n" + sep.toString() + "\n\tOrders: -> ";
        
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

        lineStr += sep.toString();

        return lineStr;

    }

}
