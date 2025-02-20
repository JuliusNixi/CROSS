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
 * It's used both by limit orders book and stop orders book. 
 * The line is omogeneus, this means all orders must have the same type (limit / stop).
 * 
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
 */
public class OrderBookLine<GenericOrder extends Order> {
 
    // This quantity is the sum of all orders in this line.
    // It's useful to know the total quantity of orders in the line, without iterating over all orders each time to calculate it.
    // Used in market orders execution.
    private Quantity totalQuantity = null;

    // LinkedList to keep the order in the same way they were added.
    // To execute the orders (matching algorithm) we use FIFO, so I give priority to insert and remove element from the head and tail.
    // Insert and execute orders from the head and tail is O(1).
    // These operations should be the most frequent.
    // Cancel an order is O(n) because we need to search for the order, but it's not a frequent operation (I hope).
    // Generic type order to handle both limit and stop orders.
    private LinkedList<GenericOrder> orders = null;

    // The price of the line.
    private final SpecificPrice linePrice;

    // To avoid confusion, I store the type of the line (stop / limit) when created.
    // All orders in the line must have the same type.
    // I will use this to check if the order (to be handled by the methods) type match with the line type.
    private final GenericOrder lineType = null;

    /**
     * 
     * Execute some coherence checks on a given order before handling it.
     * 
     * Private method, since it's used only in the class.
     * 
     * Does not return anything, but throws an exception if the order has some problems.
     * 
     * @param order The order to be checked.
     * 
     * @throws IllegalArgumentException If the order has some problems.
     * 
     */
    private void coherenceOrderChecks(GenericOrder order) throws IllegalArgumentException {

        // Checking order type, if allowed.
        if (order instanceof LimitOrder) {
            // OK, it's a limit order.
        }else if (order instanceof StopMarketOrder) {
            // OK, it's a stop order.
        } else {
            throw new IllegalArgumentException("Order of an order book line type not supported, use LimitOrder or StopMarketOrder.");
        }

        // Checking price type / price line type match.
        if (order.getPrice().getType() != lineType.getPrice().getType()) {
            throw new IllegalArgumentException("Order of an order book line price type not match with line price type.");
        }

        // Checking price value / price line value match.
        if (linePrice.getValue() != order.getPrice().getValue()) {
            throw new IllegalArgumentException("Order of an order book line price value not match with line price value.");
        }

        // Checking market order / market line match.
        if (!order.getPrice().getMarket().equals(linePrice.getMarket())) {
            throw new IllegalArgumentException("Order of an order book line market not match with line market.");
        }

        // Checking order price type / price line type match.
        if (this.lineType.getPrice().getType() != order.getPrice().getType()) {
            throw new IllegalArgumentException("Order of an order book line price type not match with line price type.");
        }

        // Checking order class / line type class.
        if (order.getClass() != this.lineType.getClass()) {
            throw new IllegalArgumentException("Order of an order book line class doesn't match with line type class.");
        }

    }

    /**
     * 
     * Constructor for a new line in the order book.
     * 
     * The line is omogeneus, this means all orders must have the same type (stop / limit).
     * The initial order is added at the beginning of the list, to follow a FIFO policy. E.g.:
     * NEW ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * 
     * @param linePrice The price of the line.
     * @param initialOrder The first order to be added to the line.
     * 
     * @throws NullPointerException If the initial order or the line price are null.
     * 
     */
    public OrderBookLine(SpecificPrice linePrice, GenericOrder initialOrder) throws NullPointerException {
        
        // Null checks.
        if (linePrice == null) {
            throw new NullPointerException("Line price of an order book line cannot be null.");
        }
        if (initialOrder == null) {
            throw new NullPointerException("Initial order of an order book line cannot be null.");
        }

        // Coherece checks.
        coherenceOrderChecks(initialOrder);

        this.totalQuantity = new Quantity(initialOrder.getQuantity().getValue());
        this.orders = new LinkedList<GenericOrder>();
        this.linePrice = linePrice;

    }

    // ORDERS MANAGEMENT
    /**
     * 
     * Add an order to its corresponding (this) line.
     * 
     * Could be a stop or a limit order.
     * The line is omogeneus, this means all orders must have the same type (stop / limit).
     * 
     * The order is added at the beginning of the list, to follow a FIFO policy. E.g.:
     * NEW ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * 
     * A check if the order is already present in the list is omitted, because a O(n) operation would be needed.
     * 
     * Synchonized method to avoid concurrency problems.
     * 
     * @param order The order to be added to the line.
     * 
     * @throws NullPointerException If the order is null.
     * 
     */
    public synchronized void addOrder(GenericOrder order) throws NullPointerException {

        // Null check.
        if (order == null)
            throw new NullPointerException("The order to add to an order book line cannot be null.");

        // Coherence checks.
        coherenceOrderChecks(order);

        // The order is added at the beginning of the list, to follow a FIFO policy.
        // A check if the order is already present in the list is omitted, because a O(n) operation would be needed.
        orders.addFirst(order);

        // Updating total quantity on this line.
        Quantity newQuantity = new Quantity(this.getTotalQuantity().getValue() + order.getQuantity().getValue());
        this.totalQuantity = newQuantity;

    }
    /**
     * 
     * Remove / cancel an order to its corresponding line (this line).
     * 
     * Could be a stop or a limit order.
     * The line is omogeneus, this means all orders must have the same type (stop/limit).
     * 
     * The order is removed at the end of the list, to follow a FIFO policy.
     * 
     * The order is removed from the list, it's a O(n) operation.
     * 
     * Synchonized method to avoid concurrency problems.
     * 
     * @param order The order to be removed.
     * 
     * @throws NullPointerException If the order is null.
     * @throws IllegalArgumentException If the order is not found in the line.
     * 
     */
    public synchronized void cancelOrder(GenericOrder order) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (order == null)
            throw new NullPointerException("The order to remove in an order book line cannot be null.");

        // Coherence checks.
        coherenceOrderChecks(order);

        // Search for the order in the line.
        // O(n) needed.
        for (GenericOrder lineOrder : orders) {

            if (lineOrder.equals(order)) {

                // Remove the order from the line.
                orders.remove(lineOrder);

                // Updating total quantity on this line.
                Quantity newQuantity = new Quantity(this.getTotalQuantity().getValue() - order.getQuantity().getValue());
                this.totalQuantity = newQuantity;
                
                return;

            }

        }

        throw new IllegalArgumentException("The order to remove in an order book line not found.");

    }
    /**
     * 
     * Extract the last order from the line (this one).
     * 
     * Could be a stop or a limit order.
     * The line is omogeneus, this means all orders must have the same type (stop/limit).
     * 
     * Remove the order from the line if the remove it parameter is true.
     * 
     * The order is extracted (and eventually removed) from the list, it's a O(1) operation.
     * 
     * This order is the first order added to the line to follow a FIFO policy, it's the one to be processed first.
     * E.g.: ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * The removed order is: X - N.
     * 
     * Synchonized method to avoid concurrency problems.
     * 
     * @param removeIt If true, the order is removed from the line, otherwise it's only extracted and returned.
     * 
     * @return The last order in the line, the first added, or null if the line is empty.
     * 
     * @throws NullPointerException If the remove it parameter is null.
     * 
     */
    public synchronized GenericOrder extractLastOrder(Boolean removeIt) throws NullPointerException {

        // Null check.
        if (removeIt == null) {
            throw new NullPointerException("Remove it parameter in the extraction of an order from an order book line cannot be null.");
        }

        // Empty line check.
        if (orders.isEmpty()) {
            return null;
        }

        GenericOrder order = orders.getLast();
        if (removeIt) {

            // Remove the order from the line.
            orders.removeLast();

            // Updating total quantity on this line.
            this.totalQuantity = new Quantity(this.getTotalQuantity().getValue() - order.getQuantity().getValue());
        
        }

        return order;

    }
    




    // ORDER EXECUTION
    /**
     * 
     * Execute a limit order ON THIS LINE with a market order.
     * 
     * This method matches ONLY THE LAST limit order with the market order.
     * So this method MUST BE executed multiples times from the book (the caller) until the market order is fully filled.
     * 
     * @param order The market order to be executed.
     * @param book The order book where the market order is placed, used to update a limit order if partially filled.
     * 
     * @return The SAME market order REFERENCE if on the current line there are more than one limit order or the only limit order present has more avaible quantity. null otherwise, so when the line is empty or the only limit order present is fully filled. So note that this returned value depends on the line state after the execution, not on the input order.
     * 
     * @throws NullPointerException If the market order or the order book are null.
     * @throws IllegalArgumentException If there are some inconsistencies between the order and the line or the book (a lot of cases are possible).
     * 
     */
    public MarketOrder executeMarketOrder(MarketOrder order, OrderBook book) throws NullPointerException, IllegalArgumentException, RuntimeException {
        
        // Null checks.
        if (order == null) {
            throw new NullPointerException("The market order to execute in an order book line cannot be null.");
        }
        if (book == null) {
            throw new NullPointerException("The book used to execute a market order cannot be null.");
        }

        // Order-Line checks.
        // Price check.
        if (!order.getPrice().equals(this.linePrice))
            throw new IllegalArgumentException("Market order price match with line price.");
        // Type check is after, is reversed for market orders.
        // Market check.
        if (!order.getMarket().equals(this.linePrice.getMarket()))
            throw new IllegalArgumentException("Market order market not match with line market.");

        // Matching best price in the market with order price check.
        // Not really needed, because the order is already checked in the book.
        SpecificPrice bestPrice = null;
        if (order.getPrice().getType() == PriceType.ASK)
            bestPrice = order.getMarket().getActualPriceAsk();
        else if (order.getPrice().getType() == PriceType.BID)
            bestPrice = order.getMarket().getActualPriceBid();
        if (bestPrice == null)
            throw new RuntimeException("Market order price not found.");
        if (bestPrice.getValue() != order.getPrice().getValue())
            throw new RuntimeException("Market order price not match with best price in the market.");

        // Reversing line type for market orders.
        PriceType linePriceType = this.linePrice.getType();
        if (linePriceType == PriceType.ASK)
            linePriceType = PriceType.BID;
        else if (linePriceType == PriceType.BID)
            linePriceType = PriceType.ASK;
        // Reverse logic for market orders.
        // Checking order's price type with reversed line price type.
        if (order.getPrice().getType() == linePrice.getType()) {
            throw new RuntimeException("Market order price type not match with REVERSED line price type.");
        }

        // Blocking orders execution on stop lines.
        if (this.lineType.getClass() != LimitOrder.class) {
            throw new RuntimeException("Line type not match with LimitOrder.");
        }

        // Book - Line checks.
        // Market check.
        if (!book.equals(this.linePrice.getMarket())) {
            throw new IllegalArgumentException("Given market not match with the line market.");
        }

        // Book - Order checks.
        // Market check.
        if (!book.equals(order.getMarket())) {
            throw new IllegalArgumentException("Given market not match with the order market.");
        }

        // Final coherece check.
        if (!book.getLine(order.getPrice()).equals(this)) {
            throw new IllegalArgumentException("Given line not found in the given book.");
        }

        /*
         * It could have been done much more simply. 
         * In fact since everything is passed by reference just set the quantity without extracting and re-entering the limit order. 
         * When I wrote this piece (at Christmas) of code sparkling wine and pandoro did not help. 
         * It works though, so I am not changing it, in fact maybe in the future it can come in handy if one were to do special actions on the limit order. 
         */
        LimitOrder currentMatchedOrder = null;
        // This casting is safe because we checked the line type before.
        currentMatchedOrder = (LimitOrder) this.extractLastOrder(false);

        if (currentMatchedOrder == null) {
            // A line with no orders, MUST NOT EXIST.
            throw new RuntimeException("Executing on line with no orders.");
        }

        Quantity updatedQuantity;
        Boolean toAppend = false;
        if (currentMatchedOrder.getQuantity().getQuantity() > order.getQuantity().getQuantity()) {
            // The LIMIT order is partially filled, the MARKET order is fully filled.
            // Updating limit.
            updatedQuantity = new Quantity(currentMatchedOrder.getQuantity().getQuantity() - order.getQuantity().getQuantity());
            currentMatchedOrder.setQuantity(updatedQuantity);
            // To append after the extraction of the current order.
            toAppend = true;

            // Updating market.
            order.setQuantity(new Quantity(0));
        }else if (currentMatchedOrder.getQuantity().getQuantity() < order.getQuantity().getQuantity()) {
            // The LIMIT order is fully filled, the MARKET order is partially filled.
            // Updating limit.
            currentMatchedOrder.setQuantity(new Quantity(0));

            // Updating market.
            updatedQuantity = new Quantity(order.getQuantity().getQuantity() - currentMatchedOrder.getQuantity().getQuantity());
            order.setQuantity(updatedQuantity);
        }else {
            // The LIMIT order is fully filled, the MARKET order is fully filled.
            // Updating limit.
            currentMatchedOrder.setQuantity(new Quantity(0));

            // Updating market.
            order.setQuantity(new Quantity(0));
        }

        // Last order check to remove the line from the book.
        if (this.getOrdersNumber() == 1) {
            if (toAppend) {
                // The unique limit order on this line is still valid.
                // Since append is true, there are still quantities to be filled.
                this.extractLastOrder(true);
                book.executeOrder(currentMatchedOrder);
                return order;
            }
            // The unique limit order on this line is fully filled.
            // Returning null to notify the book to remove the line.
            return null;
        }else {
            // More orders on this line.
            this.extractLastOrder(true);
            if (toAppend)
                book.executeOrder(currentMatchedOrder);
            return order;
        }

    }
    /**
     * Execute a stop order on this line.
     * 
     * The stop order executed is the first added to the line, the last in the list, following a FIFO policy.
     * E.g.: ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * The executed order is X - N.
     * 
     * The execution is done only if the current price in the market is equal the stop order price.
     * 
     * The execution consist in creating a market order with the same quantity and price of the stop order executed and returning it. It's maintened also the original stop order id.
     * 
     * The stop order is NOT removed from the list, must be done by the caller.
     * 
     * It execute only the last stop order in the line.
     * So this method is executed multiple times from the book until there are no more stop orders in the line.
     * 
     * If the order is the last on the line, is returned null, the order will be managed by the book to remove the line after.
     * 
     * @return A market order with the same quantity and price of the stop order executed, MUST BE EXECUTED BY THE BOOK, or null if the the order is the last in the line.
     * @throws RuntimeException If the line type doesn't match with StopMarketOrder or if the line is empty or if the line market doesn't match with the stop order market.
     * @throws IllegalArgumentException If the stop order price doesn't match with the line price or if the stop order price type doesn't match with the line price type.
     */
    public MarketOrder executeStopOrder() throws RuntimeException, IllegalArgumentException {

        // Blocking orders execution on stop lines.
        if (this.lineType.getClass() != StopMarketOrder.class) {
            throw new RuntimeException("Line type not match with StopMarketOrder.");
        }    

        // Safe cast because we checked the line type before.
        StopMarketOrder toProcess = (StopMarketOrder) extractLastOrder(false);

        if (toProcess == null) {
            // A line with no orders, MUST NOT EXIST.
            throw new RuntimeException("Executing on line with no orders.");
        }

        // Price checks.
        if (!toProcess.getPrice().equals(this.linePrice))
            throw new IllegalArgumentException("Order price not match with line price.");
        if (toProcess.getPrice().getType() != this.linePrice.getType())
            throw new IllegalArgumentException("Order price type not match with line price type.");

        // Market check.
        if (!toProcess.getMarket().equals(this.linePrice.getMarket())) {
            throw new RuntimeException("Market not match with line market.");
        }

        if (this.getOrdersNumber() == 1)
            return null;

        MarketOrder order = new MarketOrder(toProcess.getMarket(), toProcess.getPrice().getType(), toProcess.getQuantity(), toProcess.getUser());

        order.setId(toProcess.getId());

        return order;
    }




    // GETTERS
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
        return new Quantity(this.totalQuantity.getQuantity());
    }
    /**
     * Getter for the line's price.
     * @return The line's price.
     */
    public SpecificPrice getLinePrice() {
        return new SpecificPrice(this.linePrice.getValue(), this.linePrice.getType(), this.linePrice.getMarket());
    }



    // TOSTRING METHODS
    @Override
    public String toString() {
        return String.format("Price [%s] - Line Size [%s] - Total [%d]\n", this.getLinePrice().toString(), this.getTotalQuantity().toString(), this.getTotalQuantity().getQuantity() * this.getLinePrice().getValue());
    }
    /**
     * A to string method with all the orders contained in the line list.
     * The order are displayed in the short format.
     * 
     * @return A string with all the orders contained in the line list in the short format.
     */
    public String toStringWithOrders() {
        String lineStr = this.toString();
        lineStr += "\tOrders: -> ";
        for (O order : orders) {
            lineStr += "\t" + order.toStringShort() + "\n";
        }
        return lineStr;
    }



}
