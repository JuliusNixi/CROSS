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
    // Used in market orders execution.
    private Quantity totalQuantity;

    // LinkedList to keep the order in the same way they were added.
    // To execute the orders (matching algorithm) we use FIFO, so i give priority
    // to insert and remove element from the head and tail.
    // Insert and execute orders from the head and tail is O(1). These operations should be the most frequent.
    // Cancel an order is O(n) because we need to search for the order, but it's not a frequent operation (I hope).
    // Generic type order to handle both limit and stop orders.
    private LinkedList<O> orders;

    // The price of the line.
    private SpecificPrice linePrice;

    // To avoid confusion, I store the type of the line (stop/limit) when created.
    // All orders in the line must have the same type.
    // I will use this to check if the order (to be handled by the methods) type match with the line type.
    // It's also used to check if OI == O type in the constructor.
    private final O lineType = null;

    /**
     * Constructor for a new line in the order book.
     * 
     * The line is omogeneus, all orders must have the same type (stop/limit).
     * The initialOrder is added at the beginning of the list, to follow a FIFO policy. E.g.:
     * NEW ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * 
     * @param <OI> The type of the order to be added to the line.
     * @param linePrice The price of the line.
     * @param initialOrder The first order to be added to the line.
     * 
     * @throws IllegalArgumentException If the order type doesn't match with the line type or if the order price doesn't match with the line price or if the price type doesn't match with the line price type or if the price market doesn't match with the line price market.
     * @throws NullPointerException If the order or the line price are null.
     */
    public <OI extends Order> OrderBookLine(SpecificPrice linePrice, OI initialOrder) throws IllegalArgumentException, NullPointerException {
        if (linePrice == null) {
            throw new NullPointerException("Line Price cannot be null.");
        }
        if (initialOrder == null) {
            throw new NullPointerException("Initial order cannot be null.");
        }

        // Checking order type.
        if (initialOrder instanceof LimitOrder) {
            // OK, it's a limit order.
        }else if (initialOrder instanceof StopMarketOrder) {
            // OK, it's a stop order.
        } else {
            throw new IllegalArgumentException("Initial order type not supported, use LimitOrder or StopMarketOrder.");
        }

        // Checking class match internal method generic type with class generic type.
        if (initialOrder.getClass() != lineType.getClass()) {
            throw new IllegalArgumentException("Initial order type doesn't match with class generic type.");
        }

        // Checking price match.
        if (!initialOrder.getPrice().equals(initialOrder.getPrice())) {
            throw new IllegalArgumentException("Initial order price not match with line price.");
        }
        if (initialOrder.getPrice().getType() != linePrice.getType()) {
            throw new IllegalArgumentException("Initial order price type not match with line price type.");
        }

        // Checking market match.
        if (!initialOrder.getPrice().getMarket().equals(linePrice.getMarket())) {
            throw new IllegalArgumentException("Initial order price market not match with line price market.");
        }

        this.totalQuantity = new Quantity(initialOrder.getQuantity().getQuantity());
        this.orders = new LinkedList<O>();
        this.linePrice = linePrice;

    }

    // ORDERS MANAGEMENT
    /**
     * Add an order to its corresponding line.
     * Could be a stop or a limit order.
     * The line is omogeneus, all orders must have the same type (stop/limit).
     * 
     * The order is added at the beginning of the list, to follow a FIFO policy. E.g.:
     * NEW ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * A check if the order is already present in the list is omitted, because a O(n) operation would be needed.
     * 
     * @param order The order to be added.
     * @throws IllegalArgumentException If the order type doesn't match with the line type or if the order price doesn't match with the line price.
     * @throws NullPointerException If the order is null.
     */
    public void addOrder(O order) throws IllegalArgumentException, NullPointerException {
        if (order == null)
            throw new NullPointerException("Order cannot be null.");

        // Price checks.
        if (!order.getPrice().equals(this.linePrice))
            throw new IllegalArgumentException("Order price not match with line price.");
        if (order.getPrice().getType() != this.linePrice.getType())
            throw new IllegalArgumentException("Order price type not match with line price type.");

        // Market check.
        if (!order.getMarket().equals(this.linePrice.getMarket()))
            throw new IllegalArgumentException("Order market not match with line market.");

        if (order.getClass() == this.lineType.getClass()) {
            // The order is added at the beginning of the list, to follow a FIFO policy.
            // A check if the order is already present in the list is omitted, because a O(n) operation would be needed.
            orders.addFirst(order);

            // Updating total quantity on this line.
            Quantity newQuantity = new Quantity(this.getTotalQuantity().getQuantity() + order.getQuantity().getQuantity());
            this.totalQuantity = newQuantity;
        } else {
            throw new IllegalArgumentException("Order type doesn't match with line type.");
        }
    }
    /**
     * Remove/cancel an order to its corresponding line.
     * Could be a stop or a limit order.
     * The line is omogeneus, all orders must have the same type (stop/limit).
     * 
     * The order is removed at the end of the list, to follow a FIFO policy.
     * 
     * The order is removed from the list, it's a O(n) operation.
     * 
     * @param order The order to be removed.
     * @throws IllegalArgumentException If the order type doesn't match with the line type or if the order price doesn't match with the line price or if the price type doesn't match with the line price type or if the price market doesn't match with the line price market or if the order is not found in the line.
     * @throws NullPointerException If the order is null.
     */
    public void cancelOrder(O order) throws IllegalArgumentException, NullPointerException {
        if (order == null)
            throw new NullPointerException("Order cannot be null.");

        if (order.getClass() != this.lineType.getClass())
            throw new IllegalArgumentException("Order type doesn't match with line type.");

        // Price check.
        if (!order.getPrice().equals(this.linePrice))
            throw new IllegalArgumentException("Order price not match with line price.");
        if (order.getPrice().getType() != this.linePrice.getType())
            throw new IllegalArgumentException("Order price type not match with line price type.");

        // Market check.
        if (!order.getMarket().equals(this.linePrice.getMarket()))
            throw new IllegalArgumentException("Order market not match with line market.");

        // Search for the order in the line.
        // O(n) needed.
        for (O lineOrder : orders) {
            if (lineOrder.equals(order)) {
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
     * Extract the last order from the line.
     * Could be a stop or a limit order.
     * The line is omogeneus, all orders must have the same type (stop/limit).
     * 
     * Remove it from the line if the removeIt parameter is true.
     * 
     * The order is removed from the list, it's a O(1) operation.
     * This order is the first order added to the line to follow a FIFO policy, it's the one to be processed first.
     * E.g.: ORDER X -> ORDER X - 1 -> ORDER X - 2 -> ... -> ORDER X - N
     * The removed order is X - N.
     * 
     * @param removeIt If true, the order is removed from the line.
     * @return The last order in the line, the first added, or null if the line is empty.
     * @throws NullPointerException If the removeIt parameter is null.
     */
    public O extractLastOrder(Boolean removeIt) throws NullPointerException {

        if (removeIt == null) {
            throw new NullPointerException("Remove it cannot be null.");
        }

        if (orders.isEmpty()) {
            return null;
        }

        O order = orders.getLast();
        if (removeIt) {

            // Remove the order from the line.
            orders.removeLast();

            // Updating total quantity on this line.
            this.totalQuantity = new Quantity(this.getTotalQuantity().getQuantity() - order.getQuantity().getQuantity());
        
        }

        return order;
    }
    
    // ORDER EXECUTION
    /**
     * Execute a limit order on this line with a market order.
     * This method matches the ONLY THE LAST limit order with the market order.
     * So this method is executed multiples times from the book until the market order is fully filled.
     * 
     * @param order The market order to be executed.
     * @param book The order book where the order is placed used to update a limit order if partially filled.
     * 
     * @return The SAME market order reference if on the current line there are more than one limit order or the only limit order present has more avaible quantity. null otherwise, so when the line is empty or the only limit order present is fully filled. Note that this returned value depends on the line state after the execution, not on the input order.
     * 
     * @throws NullPointerException If the market order or the order book are null.
     * @throws IllegalArgumentException If there are some inconsistencies between the order and the line or the book (a lot of cases are possible).
     */
    public MarketOrder executeMarketOrder(MarketOrder order, OrderBook book) throws NullPointerException, IllegalArgumentException, RuntimeException {
        if (order == null) {
            throw new NullPointerException("Market order cannot be null.");
        }
        if (book == null) {
            throw new NullPointerException("Order book cannot be null.");
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
