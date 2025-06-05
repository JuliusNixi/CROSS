package cross.orderbook;

import cross.api.notifications.Notification;
import cross.exceptions.InvalidOrder;
import cross.orders.LimitOrder;
import cross.orders.MarketOrder;
import cross.orders.Order;
import cross.orders.StopOrder;
import cross.types.Currency;
import cross.types.Quantity;
import cross.types.price.GenericPrice;
import cross.types.price.PriceType;
import cross.types.price.SpecificPrice;
import cross.users.db.Users;
import cross.utils.Separator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 *
 * This class represents an order book in the system.
 * 
 * The order book is the core of a market.
 * It's used to match and execute the orders of the corresponding market.
 * It contains all the orders of a market, both limit and stop.
 *
 * An order book must be defined initially only by its increment of the price between two consecutive prices.
 * 
 * It has, after they have been set up, the actuals ask and bid prices.
 * They are the prices at which the order book is currently trading, so they are the best ask and the best bid prices.
 * 
 * Through these, an orderbook also acquires its primary and secondary currencies.
 * 
 * The order book is used to create orders to trade currencies.
 * 
 * All the order books are stored in a static field of the class.
 * So they can be retrieved by the getOrderBookByCurrencies method.
 * In this way, we can use a "main" order book, but also to remain generic for future implementations.
 * The project assignment specifies that there is only one market in the system.
 * This class (and the whole project) has been implemented to stay generic and to allow the creation of multiple markets.
 *
 * It also implements the Comparable interface to allow to check the equality of two order books.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Currency
 * @see SpecificPrice
 * @see GenericPrice
 * 
 * @see LimitOrder
 * @see StopOrder
 * @see MarketOrder
 *
 * @see Comparable
 * 
 * @see OrderBookLine
 * 
 * @see Separator
 *
 */
public class OrderBook implements Comparable<OrderBook> {

    // An order book is basically a TreeMap with as key the price level and as value an OrderBookLine.

    // By using a TreeMap, the order book is always sorted by price.
    private TreeMap<SpecificPrice, OrderBookLine<LimitOrder>> limitBook = null;
    // I will use the same data structure of the limit book because I think that it fits well also for the stop orders.
    // But, the OFFICIAL order book is the limit orders book, that contains only the limit orders.
    // So the stop orders book is "opaque".
    private TreeMap<SpecificPrice, OrderBookLine<StopOrder>> stopBook = null;

    private final LinkedList<MarketOrder> stopNowMarketOrdersToExecute = new LinkedList<>();

    // Technically the order book contains only the limit orders.
    // The majority of the brokers not show the stop orders in the order book.
    // The stop orders are hidden and are only executed when the current market price hits the stop price transforming it in market order.
    // So I will follow this philosophy.

    // The list of all the order books created.
    // Used server-side to get a "main" order book, but also to remain generic for future implementations.
    private static final LinkedList<OrderBook> orderBooks = new LinkedList<>();

    // The actuals (best) ask and bid prices of the market.
    private SpecificPrice actualPriceAsk = null;
    private SpecificPrice actualPriceBid = null;

    // Increment of the price between two consecutive prices.
    // So, for example, if the increment is 1, the prices could be 1, 2, 3, 4, 5, ...
    private final GenericPrice increment;

    /**
     * 
     * Initializes the order book by adding it to the list of all the order books created.
     * 
     * Private because it's used only by the class.
     * 
     */
    private void initializeOrderBook() {

        orderBooks.add(this);
        
    }

    /**
     *
     * Constructor of the class.
     *
     * Creates a new order book with the given price increment.
     *
     * The actuals (best) prices COULD BE NULL at the beginning, since they are updated by the order book of the market.
     *
     * @param increment The increment of the price between two consecutive prices.
     *
     * @throws NullPointerException If the increment is null.
     *
     */
    public OrderBook(GenericPrice increment) throws NullPointerException {

        // Null check.
        if (increment == null) {
            throw new NullPointerException("The increment price in an OrderBook creation cannot be null.");
        }

        // No synchronization needed for the increment, it has no setters.
        this.increment = increment;

        limitBook = new TreeMap<>();
        stopBook = new TreeMap<>();
        
        // External initialization needed to avoid leaking "this" in the constructor.
        initializeOrderBook();

    }

    // GETTERS
    /**
     *
     * Returns the actual (best) ask price of the order book.
     *
     * @return The actual ask price of the order book as a SpecificPrice object or null if not set.
     *
     */
    public SpecificPrice getActualPriceAsk() {

        return this.actualPriceAsk;

    }
    /**
     *
     * Returns the actual (best) bid price of the order book.
     *
     * @return The actual bid price of the order book as a SpecificPrice object or null if not set.
     *
     */
    public SpecificPrice getActualPriceBid() {

        return this.actualPriceBid;

    }
    /**
     *
     * Returns the primary currency of the order book.
     *
     * @return The primary currency of the order book as a Currency object.
     *
     */
    public Currency getPrimaryCurrency() {

        if (this.actualPriceAsk != null) {
            return this.actualPriceAsk.getPrimaryCurrency();
        }

        if (this.actualPriceBid != null) {
            return this.actualPriceBid.getPrimaryCurrency();
        }

        return null;

    }
    /**
     *
     * Returns the secondary currency of the order book.
     *
     * @return The secondary currency of the order book as a Currency object.
     *
     */
    public Currency getSecondaryCurrency() {

        if (this.actualPriceAsk != null) {
            return this.actualPriceAsk.getSecondaryCurrency();
        }

        if (this.actualPriceBid != null) {
            return this.actualPriceBid.getSecondaryCurrency();
        }

        return null;

    }
    /**
     *
     * Returns the increment of the price between two consecutive prices.
     *
     * @return The increment of the price between two consecutive prices as a GenericPrice object.
     *
     */
    public GenericPrice getIncrement() {

        return this.increment;

    }
    /**
     * 
     * Get an order book by its primary and secondary currencies.
     * 
     * Used server-side to get a "main" order book, but also to remain generic for future implementations.
     * 
     * @param primaryCurrency The primary currency of the order book.
     * @param secondaryCurrency The secondary currency of the order book.
     * 
     * @return The order book with the given primary and secondary currencies or null if not found.
     * 
     */
    public static OrderBook getOrderBookByCurrencies(Currency primaryCurrency, Currency secondaryCurrency) {

        for (OrderBook orderBook : orderBooks) {
            if (orderBook.getPrimaryCurrency() != null && orderBook.getSecondaryCurrency() != null && orderBook.getPrimaryCurrency().compareTo(primaryCurrency) == 0 && orderBook.getSecondaryCurrency().compareTo(secondaryCurrency) == 0) {
                return orderBook;
            }
        }
        return null;

    }
    /**
     * 
     * Checks if the limit or stop order book contains a limit or stop line.
     * 
     * @param line The limit or stop line to check if it exists in the limit or stop order book.
     * 
     * @return True if the line exists in the limit or stop order book, false otherwise.
     * 
     * @throws NullPointerException If the line is null.
     * 
     */
    public Boolean containsLine(OrderBookLine<?> line) throws NullPointerException {

        // Null checks.
        if (line == null) {
            throw new NullPointerException("The line to check if it exists in the order book cannot be null.");
        }

        if (line.getLineType() == LimitOrder.class) {
            return this.limitBook.containsKey(line.getLinePrice());
        }

        if (line.getLineType() == StopOrder.class) {
            return this.stopBook.containsKey(line.getLinePrice());
        }

        return false;

    }
    /**
     * 
     * Get a limit book line by its price.
     * 
     * @param price The price of the limit book line to get.
     * 
     * @return The limit book line with the specified price or null if not found.
     * 
     * @throws NullPointerException If the price is null.
     * 
     */
    public OrderBookLine<LimitOrder> getLimitBookLine(SpecificPrice price) throws NullPointerException {

        if (price == null) {
            throw new NullPointerException("The price of the limit book line to get cannot be null.");
        }

        return this.limitBook.get(price);

    }
    /**
     * 
     * Get a stop book line by its price.
     * 
     * @param price The price of the stop book line to get.
     * 
     * @return The stop book line with the specified price or null if not found.
     * 
     * @throws NullPointerException If the price is null.
     * 
     */
    public OrderBookLine<StopOrder> getStopBookLine(SpecificPrice price) throws NullPointerException {

        if (price == null) {
            throw new NullPointerException("The price of the stop book line to get cannot be null.");
        }

        return this.stopBook.get(price);
    }
    /**
     * 
     * Gets an order from the order book by its id.
     * 
     * @param orderId The id of the order to get from the order book.
     * 
     * @return The order with the given id, or null if the order is not present in the order book.
     * 
     * @throws NullPointerException If the order id is null.
     * 
     */
    public synchronized Order getOrderById(Long orderId) throws NullPointerException {

        // Null check.
        if (orderId == null) {
            throw new NullPointerException("The order id to be used to get an order from an order book cannot be null.");
        }

        for (OrderBookLine<LimitOrder> line : limitBook.values()) {
            LimitOrder order = line.getOrderById(orderId);
            if (order != null) {
                return order;
            }
        }

        for (OrderBookLine<StopOrder> line : stopBook.values()) {
            StopOrder order = line.getOrderById(orderId);
            if (order != null) {
                return order;
            }
        }

        return null;

    }

    // LINES MANAGEMENT
    /**
     *
     * It's private because it's used only by the class.
     *
     * Adds a line to the order book.
     * The line is added to the limit book or to the stop book based on the first order.
     * The first order is used to detect the correct book to use and so the line's attributes.
     *
     * This method is intended to create a NEW LINE, if the line with the specified price value already exists, an exception will be throwed.
     *
     * Synchronized to avoid concurrency problems, to protect the limit book and the stop book.
     * Synchronized on the first order to avoid modifications of it during the execution.
     *
     * @param <GenericOrder> Order type, could be LimitOrder or StopOrder.
     * @param firstOrder The first order to add to the line.
     *
     * @throws NullPointerException If the first order is null.
     * @throws IllegalArgumentException If the price line with extracted from the first order already exists in the book or if the first order's currencies don't match with the order book currencies.
     *
     */
    private synchronized <GenericOrder extends Order> void addLine(GenericOrder firstOrder) throws NullPointerException, IllegalArgumentException {

        // Null checks.
        if (firstOrder == null) {
            throw new NullPointerException("First order to be used to add an order book line cannot be null.");
        }

        synchronized (firstOrder) {

            // All coherence checks (linePrice and initialOrder) are done in the OrderBookLine constructor.

            // Currencies check.
            // Price has no setters, no synchronization needed.
            if (this.actualPriceAsk != null && firstOrder.getPrice().getType() == PriceType.ASK) {
                // If the order book has an actual ask price, it also has the currencies.
                // This compare throws an exception if the currencies don't match.
                firstOrder.getPrice().compareTo(this.actualPriceAsk);
            }
            if (this.actualPriceBid != null && firstOrder.getPrice().getType() == PriceType.BID) {
                // If the order book has an actual bid price, it also has the currencies.
                // This compare throws an exception if the currencies don't match.
                firstOrder.getPrice().compareTo(this.actualPriceBid);
            }

            switch (firstOrder) {
                case LimitOrder limitOrder -> {
                    
                    // Checking if the line already exists.
                    if (this.limitBook.containsKey(firstOrder.getPrice()))
                        throw new IllegalArgumentException("An order book limit line with this price already exists in the limit book.");
                    
                    OrderBookLine<LimitOrder> line = new OrderBookLine<>(limitOrder);
                    
                    this.limitBook.put(firstOrder.getPrice(), line);

                    this.updateActualPricesAdd(firstOrder.getPrice());
                    
                }
                case StopOrder stopOrder -> {
                    
                    if (this.stopBook.containsKey(firstOrder.getPrice()))
                        throw new IllegalArgumentException("An order book line with this price already exists in the stop book.");
                    
                    OrderBookLine<StopOrder> line = new OrderBookLine<>(stopOrder);
                    
                    this.stopBook.put(firstOrder.getPrice(), line);

                    // No need to update the actual prices, since the stop orders are not executed.
                    
                }
                default -> throw new IllegalArgumentException("The initial order to be used to create a new order book line must be a LimitOrder or a StopOrder.");
            }

        }

    }
    /**
     *
     * It's private because it's used only by the class.
     *
     * Removes a limit line from the limit order book.
     * The line is removed from the limit book.
     *
     * If the line with the specified price not exists, an exception will be throwed.
     * An exception will be throwed also if the line contains more than zero orders, so if it's not empty.
     *
     * Synchronized to avoid concurrency problems, to protect the limit book.
     *
     * @param linePrice The price of the line to remove.
     *
     * @throws NullPointerException If the line price is null.
     * @throws IllegalArgumentException If the line price to remove with this price not exists in the limit book or if the line price to remove with this price contains more than zero orders.
     *
     */
    private synchronized void removeLimitLine(SpecificPrice linePrice) throws IllegalArgumentException, NullPointerException {

        // Null check.
        if (linePrice == null) {
            throw new NullPointerException("Limit line price to be used to remove an order book limit line cannot be null.");
        }

        // Price has no setters, no synchronization needed.

        // Checking if the line exists.
        if (!this.limitBook.containsKey(linePrice))
            throw new IllegalArgumentException("Limit line price to remove with this price not exists in the limit book.");

        // Preventing the removal of a line with more than zero order.
        if (this.limitBook.get(linePrice).getOrdersNumber() != 0)
            throw new IllegalArgumentException("Limit line price to remove with this price contains more than zero orders.");

        // Removing the line.
        this.limitBook.remove(linePrice);

        this.updateActualPricesRemove(linePrice);

    }    
    /**
     *
     * It's private because it's used only by the class.
     *
     * Removes a stop line from the stop order book.
     * The line is removed from the stop book.
     *
     * If the line with the specified price not exists, an exception will be throwed.
     * An exception will be throwed also if the line contains more than zero orders, so if it's not empty.
     *
     * Synchronized to avoid concurrency problems, to protect the stop book.
     *
     * @param linePrice The price of the line to remove.
     *
     * @throws NullPointerException If the line price is null.
     * @throws IllegalArgumentException If the line price to remove with this price not exists in the stop book or if the line price to remove with this price contains more than zero orders.
     *
     */
    private synchronized void removeStopLine(SpecificPrice linePrice) throws IllegalArgumentException, NullPointerException {

        // Null check.
        if (linePrice == null) {
            throw new NullPointerException("Stop line price to be used to remove an order book stop line cannot be null.");
        }

        // Price has no setters, no synchronization needed.

        // Checking if the line exists.
        if (!this.stopBook.containsKey(linePrice))
            throw new IllegalArgumentException("Stop line price to remove with this price not exists in the stop book.");

        // Preventing the removal of a line with more than zero order.
        if (this.stopBook.get(linePrice).getOrdersNumber() != 0)
            throw new IllegalArgumentException("Stop line price to remove with this price contains more than zero orders.");

        // Removing the line.
        this.stopBook.remove(linePrice);

    }

    // ACTUAL PRICES MANAGEMENT & STOP ORDERS TRIGGER
    /**
     *
     * It's private because it's used only by the class.
     *
     * Update the actual prices of the market, both (i.e. one of the two) the best ask and the best bid.
     *
     * THIS MUST BE CALLED AT EACH LINE ADDED, AFTER.
     *
     * IT'S ITENDED TO WORKS ONLY WITH THE LIMIT BOOK.
     *
     * Synchronized to avoid concurrency problems.
     *
     * @param linePriceAdded The line price added to the limit book.
     *
     * @throws NullPointerException If the price line added is null.
     * @throws IllegalArgumentException If the price line added not exists in the limit book.
     *
     */
    private synchronized void updateActualPricesAdd(SpecificPrice linePriceAdded) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (linePriceAdded == null) {
            throw new NullPointerException("Line price added, to be used to update the actuals (best) order book prices, cannot be null.");
        }

        // Price has no setters, no synchronization needed.

        // Checking if the line exists.
        OrderBookLine<LimitOrder> limitLine = this.limitBook.get(linePriceAdded);
        if (limitLine == null) {
            throw new IllegalArgumentException("Line price added, to be used to update the actuals (best) order book prices, not exists in the limit book.");
        }
        if (!this.containsLine(limitLine))
            throw new IllegalArgumentException("Line price added, to be used to update the actuals (best) order book prices, not exists in the limit book.");

        // Iterating in ascending order price lines. E.g.:
        // ask, ask, best ask, best bid, bid, bid

        SpecificPrice bestAsk = this.actualPriceAsk;
        SpecificPrice bestBid = this.actualPriceBid;
        if (linePriceAdded.getType() == PriceType.ASK) {

            if (bestAsk == null) {
                // First ask line.
                // Both methods are synchronized, and of the same class.
                this.setActualPriceAsk(linePriceAdded);
                this.triggerStopOrders();
            } else {
                if (linePriceAdded.getValue() < bestAsk.getValue()) {
                    // Both methods are synchronized, and of the same class.
                    this.setActualPriceAsk(linePriceAdded);
                    this.triggerStopOrders();
                } else {
                    // The added price is not more convenient of the present one.
                }
            }

        } else if (linePriceAdded.getType() == PriceType.BID) {

            if (bestBid == null) {
                // First bid line.
                this.setActualPriceBid(linePriceAdded);
                this.triggerStopOrders();
            } else {
                if (linePriceAdded.getValue() > bestBid.getValue()) {
                    this.setActualPriceBid(linePriceAdded);
                    this.triggerStopOrders();
                } else {
                    // The added price is not more convenient of the present one.
                }
            }
        }

    }
    /**
     *
     * It's private because it's used only by the class.
     *
     * Update the actual prices of the market, both (i.e. one of the two) the best ask and the best bid.
     *
     * THIS MUST BE CALLED AT EACH LINE REMOVED, AFTER.
     *
     * IT'S ITENDED TO WORKS ONLY WITH THE LIMIT BOOK.
     *
     * Synchronized to avoid concurrency problems.
     *
     * @param linePriceRemoved The line price removed from the limit book.
     *
     * @throws NullPointerException If the price line removed is null.
     *
     */
    private synchronized void updateActualPricesRemove(SpecificPrice linePriceRemoved) throws NullPointerException {

        // Null check.
        if (linePriceRemoved == null) {
            throw new NullPointerException("Line price removed, to be used to update the actuals (best) order book prices, cannot be null.");
        }

        // Price has no setters, no synchronization needed.

        // Iterating in ascending order. E.g.:
        // ask, ask, best ask, best bid, bid, bid

        SpecificPrice bestAsk = this.actualPriceAsk;
        SpecificPrice bestBid = this.actualPriceBid;
        if (linePriceRemoved.getType() == PriceType.ASK) {

            if (bestAsk.getValue().compareTo(linePriceRemoved.getValue()) == 0) {

                // The removed line is the best ask.
                // I need to find the new best ask.
                SpecificPrice newBestAsk = null;
                SpecificPrice previousPrice = null;
                for (SpecificPrice price : limitBook.keySet()) {

                    if (price.getType() == PriceType.ASK) {
                        previousPrice = price;
                    }else {
                        // First bid line, the best ask is the previous one.
                        newBestAsk = previousPrice;
                        break;
                    }

                }
                if (newBestAsk == null) newBestAsk = previousPrice;
                // Both methods are synchronized, and of the same class.
                this.setActualPriceAsk(newBestAsk);
                this.triggerStopOrders();

            }

        } else if (linePriceRemoved.getType() == PriceType.BID) {

            if (bestBid.getValue().compareTo(linePriceRemoved.getValue()) == 0) {

                // The removed line is the best bid.
                // I need to find the new best bid.
                SpecificPrice newBestBid = null;
                for (SpecificPrice price : limitBook.keySet()) {
                    // The first bid line found is the new best bid.
                    if (price.getType() == PriceType.BID) {
                        newBestBid = price;
                        break;
                    }
                }

                // Both methods are synchronized, and of the same class.
                this.setActualPriceBid(newBestBid);
                this.triggerStopOrders();

            }

        }

    }
    /**
     *
     * It's private because it's used only by the class.
     *
     * THIS METHOD MUST BE CALLED AFTER THE ACTUAL PRICES ARE UPDATED. IT'S CALLED IN THE UPDATE ACTUAL PRICES METHODS ABOVE.
     *
     * It executes all the stop orders (if no fail occurs) that are in the stop book on the both (i.e. one of the two) NEW ACTUAL (BEST) PRICES lines.
     *
     * Synchronized to avoid concurrency problems.
     *
     */
    private synchronized void triggerStopOrders() {

        while (true) {

            SpecificPrice bestAsk = this.actualPriceAsk;
            SpecificPrice bestBid = this.actualPriceBid;

            LinkedList<SpecificPrice> stopLinesTriggered = new LinkedList<>();
            if (bestAsk != null) {
                for (SpecificPrice price : stopBook.descendingKeySet()) {
                    if (price.getType() == PriceType.BID && price.getValue() <= bestAsk.getValue()) {
                        stopLinesTriggered.add(price);
                    }
                }
            }
            if (bestBid != null) {
                for (SpecificPrice price : stopBook.keySet()) {
                    if (price.getType() == PriceType.ASK && price.getValue() >= bestBid.getValue()) {
                        stopLinesTriggered.add(price);
                    }
                }
            }

            if (stopLinesTriggered.isEmpty()) break;

            while (true) {

                if (stopLinesTriggered.isEmpty()){
                    break;
                }

                SpecificPrice priceLine = stopLinesTriggered.poll();
                OrderBookLine<StopOrder> triggeredAskLine = priceLine.getType() == PriceType.ASK ? stopBook.get(priceLine) : null;
                OrderBookLine<StopOrder> triggeredBidLine = priceLine.getType() == PriceType.BID ? stopBook.get(priceLine) : null;

                MarketOrder marketOrder;
                if (triggeredAskLine != null) {


                    marketOrder = triggeredAskLine.executeStopOrderFromStopLine(this);

                    if (triggeredAskLine.getOrdersNumber() == 0) {
                        // The line is empty, must be removed.
                        this.removeStopLine(triggeredAskLine.getLinePrice());
                    }

                    synchronized (this.stopNowMarketOrdersToExecute) {
                        this.stopNowMarketOrdersToExecute.add(marketOrder);
                    }

                }

                if (triggeredBidLine != null) {

                    marketOrder = triggeredBidLine.executeStopOrderFromStopLine(this);

                    if (triggeredBidLine.getOrdersNumber() == 0) {
                        // The line is empty, must be removed.
                        this.removeStopLine(triggeredBidLine.getLinePrice());
                    }

                    synchronized (this.stopNowMarketOrdersToExecute) {
                        this.stopNowMarketOrdersToExecute.add(marketOrder);
                    }

                }

            }

        }

    }

    // ORDERS EXECUTION
    // Overloading the method to handle the different types of orders in different ways.
    // The market order is the most complex to handle.
    /**
     *
     * Executes a market order.
     * The order is executed against the limit book.
     *
     * If the order is satisfiable, the order is executed.
     * An order is considered satisfiable if the total quantity of the order is less than or equal to the total quantity of the limit book (sum of quantity of each line).
     * So a market order can be executed at different prices for different quantities.
     *
     * The order is updated with the actual price of the market.
     *
     * Synchronized to avoid concurrency problems, to protect the limit book.
     * Synchronized also on the order to avoid modifications of it during the execution.
     *
     * @param order The market order to execute.
     *
     * @return True if the order is satisfiable and executed, false otherwise.
     *
     * @throws NullPointerException If the order is null.
     * @throws IllegalArgumentException If the order's currencies not match with order book currencies.
     * @throws IllegalStateException If the order book has no actual prices set or no limit orders.
     * @throws InvalidOrder If an error occurs while adding the order to the database.
     *
     */
    public Boolean executeOrder(MarketOrder order) throws NullPointerException, IllegalArgumentException, IllegalStateException, InvalidOrder {

        // Null check.
        if (order == null) {
            throw new NullPointerException("A market order, to be executed in a order book, cannot be null.");
        }

        synchronized (order) {
            // Synchronized in this way, since we do later a wait(), and the wait() does not release the lock on the MAIN THIS object, if we use public synchronized executeOrder(MarketOrder order) DEADLOCK!
            synchronized (this) {

                // Void limit book check.
                if (limitBook.isEmpty()) {
                    // do not throw an exception, but return false, otherwise the order is not created, and cannot send -1.
                    return false;
                }

                // Currencies checks.
                if (this.getPrimaryCurrency() != null && this.getSecondaryCurrency() != null && (order.getMarketOrderPrimaryCurrency().compareTo(this.getPrimaryCurrency()) != 0 || order.getMarketOrderSecondaryCurrency().compareTo(this.getSecondaryCurrency()) != 0)) {
                    throw new IllegalArgumentException("A market order's currencies, to be executed in a order book, not match with order book currencies.");
                }

                // Update market order execution price.
                try {
                    order.setUpdatedExecutionPrice();
                } catch (IllegalStateException ex) {
                    // No actual prices set, the order is not satisfiable.
                    if (this.verboseLogging) {
                        System.out.println("\n\n\n\n\n\n\n\n\n");
                        System.out.println("DEBUG: INSATISFIABLE MARKET ORDER: " + order.toString());
                        System.out.println("\n\n\n\n\n\n\n\n\n");
                    }
                    order.setId(-1);
                    return false;
                }

                // Checking satisfability.
                Boolean satisfiable = false;
                Quantity totalQuantity = new Quantity(0);
                for (SpecificPrice price : limitBook.keySet()) {

                    OrderBookLine<LimitOrder> line = limitBook.get(price);

                    // Calculating the total quantity.
                    if (order.getExecutionPrice().getType() == PriceType.BID) {
                        if (line.getLinePrice().getType() == PriceType.ASK) {
                            continue;
                        }
                        totalQuantity =  new Quantity(totalQuantity.getValue() + line.getTotalQuantity().getValue());
                    }else if (order.getExecutionPrice().getType() == PriceType.ASK) {
                        // We can exit before the last line, at the first ask line found, thanks to the sorted order book.
                        if (line.getLinePrice().getType() == PriceType.BID) {
                            break;
                        }
                        totalQuantity =  new Quantity(totalQuantity.getValue() + line.getTotalQuantity().getValue());
                    }

                    // Satisability check.
                    if (totalQuantity.getValue() >= order.getQuantity().getValue()) {
                        satisfiable = true;
                        break;
                    }

                }

                if (satisfiable) {
                    // Execute the order.
                    if (this.verboseLogging) {
                        System.out.println("\n\n\n\n\n\n\n\n\n");
                        System.out.println("DEBUG: Executing a SATISFIABLE MARKET ORDER: " + order.toString());
                        System.out.println("DEBUG: LIMIT BOOK BEFORE MARKET order execution: " + this.toStringWithLimitBook());
                        System.out.println("DEBUG: STOP BOOK BEFORE MARKET order execution: " + this.toStringWithStopBook());
                    }
                    while (true) {

                        Notification notification = new Notification();
                        // Update market order execution price.
                        try {
                            order.setUpdatedExecutionPrice();
                        } catch (IllegalStateException ex) {
                            // This should never happen, because the order is satisfiable.
                            throw new IllegalStateException("The order book to be used to execute a market order, must have actual prices set.");
                        }

                        // Getting the best price.
                        SpecificPrice bestPrice = order.getExecutionPrice();
                        OrderBookLine<LimitOrder> bestLine = limitBook.get(bestPrice);

                        // Executing the order.
                        Integer executed;
                        executed = bestLine.executeMarketOrderOnLimitLine(order, notification);
                        Users.notifyUsers(notification);

                        if (bestLine.getOrdersNumber() == 0) {
                            // The line is empty, must be removed.
                            this.removeLimitLine(bestLine.getLinePrice());
                        }

                        // Market order fullfilled.
                        if (executed == 0 || executed == 2) {
                            // TODO: Here, executed market order or stop now market.
                            break;
                        }else {
                            // Market order not fullfilled, continue.
                        }

                    }
                }else{
                    // The order is not satisfiable.
                    if (this.verboseLogging) {
                        System.out.println("\n\n\n\n\n\n\n\n\n");
                        System.out.println("DEBUG: INSATISFIABLE MARKET ORDER: " + order.toString());
                        System.out.println("\n\n\n\n\n\n\n\n\n");
                    }
                    order.setId(-1);
                    return false;
                }

                if (this.verboseLogging) {
                    System.out.println("DEBUG: LIMIT BOOK AFTER MARKET order execution: " + this.toStringWithLimitBook());
                    System.out.println("DEBUG: STOP BOOK AFTER MARKET order execution: " + this.toStringWithStopBook());
                    System.out.println("\n\n\n\n\n\n\n\n\n");
                }
            }
        }

        // The ClientThread threads must synchronize with the StopOrdersExecutorThread AND WAITS FOR IT, to avoid concurrency problems with ORDER EXECUTION (ARRIVAL PRIORITY) of the book orders.
        // Initially I forgot to wait here, I notified the StopOrdersExecutorThread, but I did not wait for it, since I tought it was not needed, and it would have been nice to execute more orders together.
        // This is a bit sneaky problem, because does not cause operating problems and does not happen often.
        // In fact, initally, I didn't execute a lot of market orders, and the execution of orders in a different priority didn't happen, so the problem was not evident.
        // But later, when I executed a lot of market orders, the problem has emerged.
        // I could have a lot of stop orders to execute by the StopOrdersExecutorThread, and when executing market orders from the ClientThread thread, these latter were being executed earlier, moving ahead of the stop orders and this although not serious, is not fair!
        // This check is needed because the StopOrdersExecutorThread also use this method to execute the stop orders, so we must distinguish between the calls from the ClientThread threads and the calls from the StopOrdersExecutorThread.
        if (!Thread.currentThread().getName().equalsIgnoreCase(StopOrdersExecutorThread.class.getSimpleName())) {

            synchronized (this.stopNowMarketOrdersToExecute) {
                while (!this.stopNowMarketOrdersToExecute.isEmpty()) {
                    // this wakes up the StopOrdersExecutorThread, to execute the stop orders.
                    // but also could wakes up others ClientThread threads, that are waiting for the StopOrdersExecutorThread to finish.
                    // these ClientThread threads will wait in the while below.
                    this.stopNowMarketOrdersToExecute.notifyAll();
                    try {
                        while (!this.stopNowMarketOrdersToExecute.isEmpty()) this.stopNowMarketOrdersToExecute.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
            
        }

        return true;

    }
    /**
     *
     * Execute a limit order.
     * The order is added to the limit book.
     *
     * A check if the order is already present in the list is omitted, because a O(n) operation would be needed, and the O(1) operation speed given by the list would be lost.
     *
     * Synchronized to avoid concurrency problems, to protect the limit book.
     * Synchronized also on the order to avoid modifications of it during the execution.
     *
     * @param order The limit order to execute.
     *
     * @throws NullPointerException If the order is null.
     * @throws IllegalArgumentException If the order's currencies not match with order book currencies.
     *
     */
    public synchronized void executeOrder(LimitOrder order) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (order == null) {
            throw new NullPointerException("A limit order, to be executed in a order book, cannot be null.");
        }

        SpecificPrice price;
        synchronized (order) {

            // Currencies checks.
            if (this.getPrimaryCurrency() != null && this.getSecondaryCurrency() != null && (order.getPrice().getPrimaryCurrency().compareTo(this.getPrimaryCurrency()) != 0 || order.getPrice().getSecondaryCurrency().compareTo(this.getSecondaryCurrency()) != 0)) {
                throw new IllegalArgumentException("A limit order's currencies, to be executed in a order book, not match with order book currencies.");
            }   

            price = order.getPrice();

            // Safe because synchronized.
            OrderBookLine<LimitOrder> limitLine = limitBook.get(price);

            // New price line creation.
            if (limitLine == null) {
                this.addLine(order);
                // Order added in the constructor of the new line.
                return;
                // Best prices updated in the addLine method.
            }

            // A check if the order is already present in the list is omitted, because a O(n) operation would be needed, and the O(1) operation speed given by the list would be lost.
            // Adding the order to the line.
            limitLine.addOrder(order);

        }

    }
    /**
     *
     * Executes a stop order.
     * The order is added to the stop book.
     *
     * A check if the order is already present in the list is omitted, because a O(n) operation would be needed, and the O(1) operation speed given by the list would be lost.
     *
     * Synchronized to avoid concurrency problems, to protect the stop book.
     * Synchronized also on the order to avoid modifications of it during the execution.
     *
     * @param order The stop order to execute.
     *
     * @throws NullPointerException If the order is null.
     * @throws IllegalArgumentException If the order's currencies not match with order book currencies.
     *
     */
    public synchronized void executeOrder(StopOrder order) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (order == null) {
            throw new NullPointerException("A stop order, to be executed in a order book, cannot be null.");
        }

        SpecificPrice price;
        synchronized (order) {

            // Currencies checks.
            if (this.getPrimaryCurrency() != null && this.getSecondaryCurrency() != null && (order.getPrice().getPrimaryCurrency().compareTo(this.getPrimaryCurrency()) != 0 || order.getPrice().getSecondaryCurrency().compareTo(this.getSecondaryCurrency()) != 0)) {
                throw new IllegalArgumentException("A stop order's currencies, to be executed in a order book, not match with order book currencies.");
            }   

            price = order.getPrice();

            // Safe because synchronized.
            OrderBookLine<StopOrder> stopLine = stopBook.get(price);

            // New price line creation.
            if (stopLine == null) {
                this.addLine(order);
                // Order added in the constructor of the new line.
                return;
                // Best prices updated in the addLine method.
            }

            // A check if the order is already present in the list is omitted, because a O(n) operation would be needed, and the O(1) operation speed given by the list would be lost.
            // Adding the order to the line.
            stopLine.addOrder(order);

        }

    }

    // CANCELLATION METHODS
    /**
     *
     * Cancels a limit order from the limit orders book.
     *
     * It's a O(n) operation.
     *
     * Synchronized method to avoid concurrency problems, to protect the limit book.
     * Synchronized on the order, since the order could be modified by other threads.
     *
     * @param order The limit order to be cancelled from the limit orders book.
     *
     * @throws NullPointerException If the limit order to cancel from the limit orders book is null.
     * @throws IllegalArgumentException If the limit order to cancel is not present in the limit orders book.
     *
     */
    public synchronized void cancelOrder(LimitOrder order) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (order == null) {
            throw new NullPointerException("The limit order to cancel from the limit orders book cannot be null.");
        }

        synchronized (order) {

            // Searching for the line in the limit book.
            OrderBookLine<LimitOrder> line = limitBook.get(order.getPrice());
            if (line == null) {
                throw new IllegalArgumentException("The limit order to cancel from the limit orders book is not present in the limit orders book.");
            }

            // Cancelling the order from the line.
            line.cancelOrder(order);

            if (line.getOrdersNumber() == 0) {
                this.removeLimitLine(order.getPrice());
            }

        }

    }
    /**
     *
     * Cancels a stop order from the stop orders book.
     *
     * It's a O(n) operation.
     *
     * Synchronized method to avoid concurrency problems, to protect the stop book.
     * Synchronized on the order, since the order could be modified by other threads.
     *
     * @param order The stop order to be cancelled from the stop orders book.
     *
     * @throws NullPointerException If the stop order to cancel from the stop orders book is null.
     * @throws IllegalArgumentException If the stop order to cancel is not present in the stop orders book.
     *
     */
    public synchronized void cancelOrder(StopOrder order) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (order == null) {
            throw new NullPointerException("The stop order to cancel from the stop orders book cannot be null.");
        }

        synchronized (order) {

            // Searching for the line in the stop book.
            OrderBookLine<StopOrder> line = stopBook.get(order.getPrice());
            if (line == null) {
                throw new IllegalArgumentException("The stop order to cancel from the stop orders book is not present in the stop orders book.");
            }

            // Cancelling the order from the line.
            line.cancelOrder(order);

            if (line.getOrdersNumber() == 0) {
                this.removeStopLine(order.getPrice());
            }

        }

    }
    /**
     *
     * Cancels an order from the order book by its id.
     *
     * Synchronized method to avoid concurrency problems, to protect the order book.
     *
     * @param orderId The id of the order to be cancelled from the order book.
     *
     * @throws NullPointerException If the order id to cancel from the order book is null.
     *
     * @return True if the order was cancelled, false otherwise.
     *
     */
    public synchronized Boolean cancelOrder(Long orderId) throws NullPointerException {

        // Null check.
        if (orderId == null) {
            throw new NullPointerException("The order id to be used to cancel an order from the order book cannot be null.");
        }

        for (OrderBookLine<LimitOrder> line : limitBook.values()) {
            LimitOrder order = (LimitOrder) line.getOrderById(orderId);
            if (order != null) {
                line.cancelOrder(order);
                if (line.getOrdersNumber() == 0) {
                    this.removeLimitLine(order.getPrice());
                }
                return true;
            }
        }

        for (OrderBookLine<StopOrder> line : stopBook.values()) {
            StopOrder order = (StopOrder) line.getOrderById(orderId);
            if (order != null) {
                line.cancelOrder(order);
                if (line.getOrdersNumber() == 0) {
                    this.removeStopLine(order.getPrice());
                }
                return true;
            }
        }

        return false;

    }

    // SETTERS
    /**
     *
     * Sets the actual (best) ask price of the order book.
     *
     * Synchronized method to avoid multiple threads to set the actual ask price at the same time.
     *
     * @param actualPriceAskP The new actual (best) ask price of the order book.
     * 
     * @throws IllegalArgumentException If the given new actual (best) price ask is not an ask price. If it has different primary and secondary currencies as the actual (best) price bid or as the actual (best) price ask. If it is not greater than the actual (best) price bid.
     * @throws IllegalStateException If an order book with the same currencies already exists.
     *
     */
    private synchronized void setActualPriceAsk(SpecificPrice actualPriceAsk) throws IllegalArgumentException, IllegalStateException {

        // Null check.
        if (actualPriceAsk == null) {
            this.actualPriceAsk = null;
            return;
        }

        // Prices cannot change, have no setters, no synchronization needed.

        // The actual price ask must be an ask price.
        if (actualPriceAsk.getType() != PriceType.ASK) {
            throw new IllegalArgumentException("The actual (best) price ask to set as actual (best) price ask must be an ask price.");
        }

        if (this.actualPriceBid != null) {
            // The new actual ask price must be GREATER than the actual bid price.
            if (actualPriceAsk.getValue() < this.actualPriceBid.getValue()) {
                throw new IllegalArgumentException("The actual (best) price ask to set as actual (best) price ask must be GREATER than the actual (best) price bid.");
            }
            // Currencies check between the actual bid and the new actual ask.
            if (actualPriceAsk.getPrimaryCurrency().compareTo(this.actualPriceBid.getPrimaryCurrency()) != 0 || actualPriceAsk.getSecondaryCurrency().compareTo(this.actualPriceBid.getSecondaryCurrency()) != 0) {
                throw new IllegalArgumentException("The actual (best) price ask to set as actual (best) price ask must have the same primary and secondary currencies as the actual (best) price bid.");
            }
        }

        if (this.actualPriceAsk != null) {
            // Currencies check between the actual ask and the new actual ask.
            if (actualPriceAsk.getPrimaryCurrency().compareTo(this.actualPriceAsk.getPrimaryCurrency()) != 0 || actualPriceAsk.getSecondaryCurrency().compareTo(this.actualPriceAsk.getSecondaryCurrency()) != 0) {
                throw new IllegalArgumentException("The actual (best) price ask to set as actual (best) price ask must have the same primary and secondary currencies as the actual (best) price ask.");
            }
        }

        // Prevent the creation of a new order book with the same currencies.
        if (this.actualPriceAsk == null && this.actualPriceBid == null) {
            // Check only on the first set of a best ask or a best bid.
            Currency primaryCurrency = actualPriceAsk.getPrimaryCurrency();
            Currency secondaryCurrency = actualPriceAsk.getSecondaryCurrency();
            for (OrderBook orderBook : orderBooks) {
                if (orderBook != this && orderBook.getPrimaryCurrency() == primaryCurrency && orderBook.getSecondaryCurrency() == secondaryCurrency) {
                    throw new IllegalStateException("The order book with the same currencies already exists.");
                }
            }
        }

        this.actualPriceAsk = actualPriceAsk;

    }
    /**
     *
     * Sets the actual (best) bid price of the order book.
     *
     * Synchronized method to avoid multiple threads to set the actual bid price at the same time.
     *
     * @param actualPriceBidP The new actual (best) bid price of the order book.
     * 
     * @throws IllegalArgumentException If the given new actual (best) price bid is not a bid price. If it has different primary and secondary currencies as the actual (best) price bid or as the actual (best) price ask. If it is not less than the actual (best) price ask.
     * @throws IllegalStateException If an order book with the same currencies already exists.
     *
     */
    private synchronized void setActualPriceBid(SpecificPrice actualPriceBid) throws IllegalArgumentException, IllegalStateException {

        // Null check.
        if (actualPriceBid == null) {
            this.actualPriceBid = null;
            return;
        }

        // Prices cannot change, have no setters, no synchronization needed.

        // The actual price bid must be a bid price.
        if (actualPriceBid.getType() != PriceType.BID) {
            throw new IllegalArgumentException("The actual (best) price bid to set as actual (best) price bid must be a bid price.");
        }

        if (this.actualPriceBid != null) {
            // Currency check.
            if (actualPriceBid.getPrimaryCurrency().compareTo(this.actualPriceBid.getPrimaryCurrency()) != 0 || actualPriceBid.getSecondaryCurrency().compareTo(this.actualPriceBid.getSecondaryCurrency()) != 0) {
                throw new IllegalArgumentException("The actual (best) price bid to set as actual (best) price bid must have the same primary and secondary currencies as the actual (best) price bid.");
            }
        }

        if (this.actualPriceAsk != null) {
            // The new actual bid price must be LESS than the actual ask price.
            if (actualPriceBid.getValue() > this.actualPriceAsk.getValue()) {
                throw new IllegalArgumentException("The actual (best) price bid to set as actual (best) price bid must be LESS than the actual (best) price ask.");
            }
            // Currencies check between the actual ask and the new actual bid.
            if (actualPriceBid.getPrimaryCurrency().compareTo(this.actualPriceAsk.getPrimaryCurrency()) != 0 || actualPriceBid.getSecondaryCurrency().compareTo(this.actualPriceAsk.getSecondaryCurrency()) != 0) {
                throw new IllegalArgumentException("The actual (best) price bid to set as actual (best) price bid must have the same primary and secondary currencies as the actual (best) price ask.");
            }
        }

        // Prevent the creation of a new order book with the same currencies.
        if (this.actualPriceAsk == null && this.actualPriceBid == null) {
            // Check only on the first set of a best ask or a best bid.
            Currency primaryCurrency = actualPriceBid.getPrimaryCurrency();
            Currency secondaryCurrency = actualPriceBid.getSecondaryCurrency();
            for (OrderBook orderBook : orderBooks) {
                if (orderBook != this && orderBook.getPrimaryCurrency() == primaryCurrency && orderBook.getSecondaryCurrency() == secondaryCurrency) {
                    throw new IllegalStateException("The order book with the same currencies already exists.");
                }
            }
        }

        this.actualPriceBid = actualPriceBid;

    }

    @Override
    public int compareTo(OrderBook otherOrderBook) throws NullPointerException, IllegalArgumentException {

        // No synchronization needed, no changes (no setters) IN THESE (CURRENCIES) market's properties.

        // Null check.
        if (otherOrderBook == null)
            throw new NullPointerException("The order book to compare to cannot be null.");

        // Equals order books.
        if (this.getPrimaryCurrency() == otherOrderBook.getPrimaryCurrency() && this.getSecondaryCurrency() == otherOrderBook.getSecondaryCurrency())
            return 0;

        throw new IllegalArgumentException("Cannot compare different order books.");

    }

    // TOSTRING METHODS
    @Override
    public synchronized String toString() {

        String bestAsk = this.getActualPriceAsk() == null ? "null" : this.getActualPriceAsk().toString();
        String bestBid = this.getActualPriceBid() == null ? "null" : this.getActualPriceBid().toString();
        String primaryCurrency = this.getPrimaryCurrency() == null ? "null" : this.getPrimaryCurrency().name();
        String secondaryCurrency = this.getSecondaryCurrency() == null ? "null" : this.getSecondaryCurrency().name();

        return String.format("Pair [%s/%s] - Actual Ask [%s] - Actual Bid [%s] - Price Increment [%s]", primaryCurrency, secondaryCurrency, bestAsk, bestBid, this.getIncrement().toString());

    }
    /**
     *
     * Returns a short string representation of the order book.
     *
     * @return A short string representation of the order book.
     *
     */
    public synchronized String toStringShort() {

        String bestAskValue = this.getActualPriceAsk() == null ? "null" : this.getActualPriceAsk().getValue().toString();
        String bestBidValue = this.getActualPriceBid() == null ? "null" : this.getActualPriceBid().getValue().toString();
        String primaryCurrency = this.getPrimaryCurrency() == null ? "null" : this.getPrimaryCurrency().name();
        String secondaryCurrency = this.getSecondaryCurrency() == null ? "null" : this.getSecondaryCurrency().name();

        return String.format("Pair [%s/%s] - Actual Ask Value [%s] - Actual Bid Value [%s] - Price Increment [%s]", primaryCurrency, secondaryCurrency, bestAskValue, bestBidValue, this.getIncrement().toString());

    }
    /**
     *
     * Returns a string representation of the order book with the limit book.
     *
     * @return A string representation of the order book with the limit book.
     *
     */
    public synchronized String toStringWithLimitBook() {

        String info = this.toStringShort();

        // Length is good since the super.toString() is a one line string.
        String separator = new Separator("-", info.length() + "LIMIT BOOK ".length()).toString();

        // Adding the basic market info.
        String result = "\n" + separator + "\n" + "LIMIT BOOK " + info + "\n" + separator;

        // I want to divide the best ask and the best bid.
        // From top to bottom: ask, ask, best ask, best bid, bid, bid.
        Boolean firstBid = true;
        String lineStr;
        for (SpecificPrice price : limitBook.keySet()) {

            OrderBookLine<LimitOrder> line = limitBook.get(price);
            // Removing additionals infos.
            lineStr = line.toString().split("Type")[1].trim();

            if (price.getType() == PriceType.ASK) {
                result += "\n" + lineStr;
            } else {
                if (firstBid) {
                    String separator2 = new Separator("*", lineStr.length()).toString();
                    result += "\n" + separator2;
                    firstBid = false;
                }
                result += "\n" + lineStr;
            }

        }

        result += "\n" + separator + "\n";
        return result;

    }
    /**
     *
     * Returns a string representation of the order book with the stop book.
     *
     * @return A string representation of the order book with the stop book.
     *
     */
    public synchronized String toStringWithStopBook() {

        String info = this.toStringShort();

        String askBidView = "STOP BOOK (ASK/BID REVERSED) - ";

        // Length is good since the super.toString() is a one line string.
        String separator = new Separator("-", info.length() + askBidView.length()).toString();

        // Adding the basic market info.
        String result = "\n" + separator + "\n" + askBidView + info + "\n" + separator;

        // I want to divide the best ask and the best bid.
        // From top to bottom: ask, ask, best ask, best bid, bid, bid.
        Boolean firstAsk = true;
        String lineStr;
        Iterator<SpecificPrice> iterator = stopBook.keySet().iterator();
        Iterator<SpecificPrice> iteratorNext = stopBook.keySet().iterator();
        SpecificPrice bestBid = this.getActualPriceBid();
        while (iterator.hasNext()) {
            SpecificPrice price = iterator.next();

            SpecificPrice priceNext = null;
            if (iteratorNext.hasNext()) {
                priceNext = iteratorNext.next();
            }

            OrderBookLine<StopOrder> line = stopBook.get(price);
            // Removing additionals infos.
            lineStr = line.toString().split("Type")[1].trim();

            if (priceNext != null && bestBid != null && priceNext.getValue() <= bestBid.getValue() && price.getValue() >= bestBid.getValue()) {
                String separator2 = new Separator("$", lineStr.length()).toString();
                result += "\n" + separator2;
            }

            if (price.getType() == PriceType.BID) {
                result += "\n" + lineStr;
            } else {
                if (firstAsk) {
                    String separator2 = new Separator("*", lineStr.length()).toString();
                    result += "\n" + separator2;
                    firstAsk = false;
                }
                result += "\n" + lineStr;
            }

        }

        result += "\n" + separator + "\n";
        return result;

    }









    private StopOrdersExecutorThread stopOrdersExecutorThread;
    public void startStopOrdersExecutorThread() {

        // Fill the list and process if there are some ready stop orders to execute.
        this.triggerStopOrders();
        synchronized (this.stopNowMarketOrdersToExecute) {

            stopOrdersExecutorThread = new StopOrdersExecutorThread(this);
            stopOrdersExecutorThread.start();

            // The other thread is just started, it will wait on the lock and then will wake up us (the main thread).

            try {
                this.stopNowMarketOrdersToExecute.wait();
            } catch (InterruptedException ex) {
            }
        
        }

    }
    public void stopStopOrdersExecutorThread() {

        stopOrdersExecutorThread.stopRunning();
        stopOrdersExecutorThread.interrupt();
        try {
            stopOrdersExecutorThread.join();
        } catch (InterruptedException ex) {
        }

    }
    protected LinkedList<MarketOrder> getStopNowMarketOrdersToExecute() {

        return this.stopNowMarketOrdersToExecute;

    }
    private Boolean verboseLogging = false;
    public void setVerboseLogging(Boolean verboseLogging) {

        this.verboseLogging = verboseLogging;

    }
    public Boolean getVerboseLogging() {

        return this.verboseLogging;

    }



    // MAIN ORDER BOOK MANAGEMENT
    private static OrderBook mainOrderBook = null;
    public static OrderBook getMainOrderBook() {

        return mainOrderBook;

    }
    public static void setMainOrderBook(OrderBook orderBook) {

        mainOrderBook = orderBook;

    }






}

