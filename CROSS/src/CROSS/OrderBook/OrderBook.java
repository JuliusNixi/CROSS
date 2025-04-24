package CROSS.OrderBook;

import java.util.TreeMap;
import CROSS.Orders.LimitOrder;
import CROSS.Orders.MarketOrder;
import CROSS.Orders.Order;
import CROSS.Orders.StopMarketOrder;
import CROSS.Types.Currency;
import CROSS.Types.Quantity;
import CROSS.Types.Price.GenericPrice;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Utils.Separator;

/**
 * 
 * The order book is the core of a market.
 * 
 * It extends the Market class.
 * 
 * It contains all the orders of a market, both limit and stop.
 * 
 * It's used to match and execute the orders of the corresponding market.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Market
 * 
 * @see OrderBookLine
 * 
 * @see LimitOrder
 * @see StopMarketOrder
 * @see MarketOrder
 * 
 * @see SpecificPrice
 * 
 */
public class OrderBook extends Market {

    // An order book is basically a TreeMap with as key the price level and as value an OrderBookLine.

    // By using a TreeMap, the order book is always sorted by price.
    private TreeMap<SpecificPrice, OrderBookLine<LimitOrder>> limitBook = null;

    // Technically the order book contains only the limit orders.
    // The majority of the brokers not show the stop orders in the order book.
    // The stop orders are hidden and are only executed when the current market price hits the stop price transforming it in market order.
    // So I will follow this philosophy.

    // I will use the same data structure of the limit book because I think that it fits well also for the stop orders.
    // But, the OFFICIAL order book is the limit orders book, that contains only the limit orders.
    // So the stop orders book is "opaque".
    private TreeMap<SpecificPrice, OrderBookLine<StopMarketOrder>> stopBook = null;

    // CONSTRUCTORS
    /**
     * 
     * Constructor of the class.
     * 
     * @param primary_currency The primary currency of the market.
     * @param secondary_currency The secondary currency of the market.
     * @param increment The price increment of the market.
     * 
     * @throws NullPointerException If the primary currency, the secondary currency or the increment are null.
     * @throws IllegalArgumentException If the primary and secondary currencies are the same.
     * 
     */
    public OrderBook(Currency primary_currency, Currency secondary_currency, GenericPrice increment) throws NullPointerException, IllegalArgumentException {
        
        // Increment is copied in the super constructor.
        super(primary_currency, secondary_currency, increment);

        limitBook = new TreeMap<SpecificPrice, OrderBookLine<LimitOrder>>();
        stopBook = new TreeMap<SpecificPrice, OrderBookLine<StopMarketOrder>>();

    }
    /**
     * 
     * Alternative constructor of the class.
     * Create an order book from a market object.
     * 
     * Synchronized on market to avoid changes in the market during the creation of the order book.
     * 
     * @param market The market object to use to create the order book.
     * 
     * @throws NullPointerException If the primary currency, the secondary currency or the increment are null.
     * @throws IllegalArgumentException If the primary and secondary currencies are the same.
     * 
     */ 
    public OrderBook(Market market) throws NullPointerException, IllegalArgumentException {
        
        super(market.getPrimaryCurrency(), market.getSecondaryCurrency(), market.getIncrement());

        synchronized (market) {

            SpecificPrice actualPriceAsk = null;
            SpecificPrice actualPriceBid = null;
            
            actualPriceAsk = market.getActualPriceAsk();
            actualPriceBid = market.getActualPriceBid();

            if (actualPriceAsk != null)
                super.setActualPrices(actualPriceAsk, actualPriceBid);
            if (actualPriceBid != null)
                super.setActualPrices(actualPriceAsk, actualPriceBid);

        }

        limitBook = new TreeMap<SpecificPrice, OrderBookLine<LimitOrder>>();
        stopBook = new TreeMap<SpecificPrice, OrderBookLine<StopMarketOrder>>();

    }

    // LINES MANAGEMENT
    /**
     * 
     * It's private because it's used only by the class.
     * 
     * Add a line to the order book.
     * The line is added to the limit book or to the stop book based on the initial order.
     * The initial (first) order is used to detect the correct book to use and so the line's type.
     * 
     * This method is intended to create a NEW LINE, if the line with the specified price value already exists, an exception will be throwed.
     * 
     * Synchronized to avoid concurrency problems, to protect the limit book and the stop book.
     * Synchronized on the initial order to avoid modifications of it during the execution.
     * 
     * @param <GenericOrder> Order type, could be LimitOrder or StopMarketOrder.
     * @param linePrice The price of the line.
     * @param initialOrder The first order to add to the line.
     * 
     * @throws NullPointerException If the initial order or the price line are null.
     * @throws IllegalArgumentException  If the initial order has some problems with the line attributes. If the line with the specified price already exists in the book. If the line price market not match with order book market.
     * 
     */
    private synchronized <GenericOrder extends Order> void addLine(SpecificPrice linePrice, GenericOrder initialOrder) throws NullPointerException {
        
        // Null checks.
        if (initialOrder == null) {
            throw new NullPointerException("Initial order to be used to add an order book line cannot be null.");
        }
        if (linePrice == null) {
            throw new NullPointerException("Line price to be used to add an order book line cannot be null.");
        }

        // Market checks.
        // Checked in this way since the order book extends the market class.
        // Price has no setters, no synchronization needed.
        if (linePrice.getMarket().getPrimaryCurrency() != this.getPrimaryCurrency() || linePrice.getMarket().getSecondaryCurrency() != this.getSecondaryCurrency()) {
            throw new IllegalArgumentException("Line price market, to be used to add an order book line, not match with order book market.");
        }
        
        // All coherence checks (linePrice and initialOrder) are done in the OrderBookLine constructor.

        // No copy, since we need to update the quantity during an order execution.

        synchronized (initialOrder) {

            if (initialOrder instanceof LimitOrder) {

                if (this.limitBook.containsKey(initialOrder.getPrice()))
                    throw new IllegalArgumentException("An order book line with this price already exists in the limit book.");

                LimitOrder order = (LimitOrder) initialOrder;

                OrderBookLine<LimitOrder> line = new OrderBookLine<LimitOrder>(linePrice, order);

                this.limitBook.put(linePrice, line);

            } else if (initialOrder instanceof StopMarketOrder) {
                
                if (this.stopBook.containsKey(initialOrder.getPrice()))
                    throw new IllegalArgumentException("An order book line with this price already exists in the stop book.");

                StopMarketOrder order = (StopMarketOrder) initialOrder;

                OrderBookLine<StopMarketOrder> line = new OrderBookLine<StopMarketOrder>(linePrice, order);

                this.stopBook.put(linePrice, line);

            } else {
                throw new IllegalArgumentException("The initial order to be used to create a new order book line must be a LimitOrder or a StopMarketOrder.");
            }

            this.updateActualPricesAdd(linePrice);

        }

    }
    /**
     * 
     * It's private because it's used only by the class.
     * 
     * Remove a line from the LIMIT order book.
     * 
     * If the line with the specified price not exists, an exception will be throwed.
     * An exception will be throwed also if the line contains more than zero orders, so if it's not empty.
     * 
     * Synchronized to avoid concurrency problems, to protect the limit book.
     * 
     * @param linePrice The price of the line to remove.
     * 
     * @throws NullPointerException If the line price is null.
     * @throws IllegalArgumentException If the line price market not match with order book market. If the line price to remove with this price not exists in the limit book. If the line price to remove with this price contains more than zero orders.
     * 
     */
    private synchronized void removeLimitLine(SpecificPrice linePrice) throws IllegalArgumentException, NullPointerException {
    
        // Null check.
        if (linePrice == null) {
            throw new NullPointerException("Line price to be used to remove an order book line cannot be null.");
        }

        // Price has no setters, no synchronization needed.

        // Market checks.
        // Checked in this way since the order book extends the market class.
        if (linePrice.getMarket().getPrimaryCurrency() != this.getPrimaryCurrency() || linePrice.getMarket().getSecondaryCurrency() != this.getSecondaryCurrency()) {
            throw new IllegalArgumentException("Line price market, to be used to remove an order book line, not match with order book market.");
        }

        // Checking if the line exists.
        if (!this.limitBook.containsKey(linePrice))
            throw new IllegalArgumentException("Line price to remove with this price not exists in the limit book.");
            
        // Preventing the removal of a line with more than zero order.
        if (this.limitBook.get(linePrice).getOrdersNumber() != 0)
            throw new IllegalArgumentException("Line price to remove with this price contains more than zero orders.");

        // Removing the line.
        this.limitBook.remove(linePrice);

        this.updateActualPricesRemove(linePrice);
            
    }
    /**
     * 
     * It's private because it's used only by the class.
     * 
     * Remove a line from the STOP order book.
     * 
     * If the line with the specified price not exists, an exception will be throwed.
     * An exception will be throwed also if the line contains more than zero orders, so if it's not empty.
     * 
     * Synchronized to avoid concurrency problems, to protect the stop book.
     * 
     * @param linePrice The price of the line to remove.
     * 
     * @throws NullPointerException If the line price is null.
     * @throws IllegalArgumentException If the line price market not match with order book market. If the line price to remove with this price not exists in the stop book. If the line price to remove with this price contains more than zero orders.
     * 
     */
    private synchronized void removeStopLine(SpecificPrice linePrice) throws IllegalArgumentException, NullPointerException {
    
        // Null check.
        if (linePrice == null) {
            throw new NullPointerException("Line price to be used to remove an order book line cannot be null.");
        }

        // Price has no setters, no synchronization needed.

        // Market checks.
        // Checked in this way since the order book extends the market class.
        if (linePrice.getMarket().getPrimaryCurrency() != this.getPrimaryCurrency() || linePrice.getMarket().getSecondaryCurrency() != this.getSecondaryCurrency()) {
            throw new IllegalArgumentException("Line price market, to be used to remove an order book line, not match with order book market.");
        }

        // Checking if the line exists.
        if (!this.stopBook.containsKey(linePrice))
            throw new IllegalArgumentException("Line price to remove with this price not exists in the stop book.");
            
        // Preventing the removal of a line with more than zero order.
        if (this.stopBook.get(linePrice).getOrdersNumber() != 0)
            throw new IllegalArgumentException("Line price to remove with this price contains more than zero orders.");

        // Removing the line.
        this.stopBook.remove(linePrice);

        this.updateActualPricesRemove(linePrice);
            
    }
    
    // Super info + limit book lines.
    @Override
    public synchronized String toString() {

        String superInfo = super.toString();
        String bestAsk = super.getActualPriceAsk() != null ? super.getActualPriceAsk().getValue().toString() : "null";
        String bestBid = super.getActualPriceBid() != null ? super.getActualPriceBid().getValue().toString() : "null";
        // Short super info.
        superInfo = String.format("Pair [%s/%s] - Best Ask: [%s] - Best Bid: [%s]", super.getPrimaryCurrency(), super.getSecondaryCurrency(), bestAsk, bestBid);

        // Length is good since the super.toString() is a one line string.
        String separator = new Separator("-", superInfo.length()).toString();

        // Adding the basic market info.
        String result = "\n" + separator + "\n" + superInfo + "\n" + separator;

        // I want to divide the best ask and the best bid.
        // From top to bottom: ask, ask, best ask, best bid, bid, bid.
        Boolean firstBid = true;
        String lineStr = "";
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
     * @throws IllegalArgumentException If the price line added market not match with order book market. If the price line added not exists in the limit book.
     * 
     */
    private synchronized void updateActualPricesAdd(SpecificPrice linePriceAdded) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (linePriceAdded == null) {
            throw new NullPointerException("Line price added, to be used to update the actuals (best) market prices, cannot be null.");
        }

        // Market checks.
        // Checked in this way since the order book extends the market class.
        if (linePriceAdded.getMarket().getPrimaryCurrency() != this.getPrimaryCurrency() || linePriceAdded.getMarket().getSecondaryCurrency() != this.getSecondaryCurrency()) {
            throw new IllegalArgumentException("Line price added, to be used to update the actuals (best) market prices, market not match with order book market.");
        }

        // Price has no setters, no synchronization needed.

        // Checking if the line exists.
        if (!this.limitBook.containsKey(linePriceAdded))
            throw new IllegalArgumentException("Line price added, to be used to update the actuals (best) market prices, not exists in the limit book.");

        // Iterating in ascending order price lines. E.g.:
        // ask, ask, best ask, best bid, bid, bid

        SpecificPrice bestAsk = super.getActualPriceAsk();
        SpecificPrice bestBid = super.getActualPriceBid();
        if (linePriceAdded.getType() == PriceType.ASK) {

            if (bestAsk == null) {
                // First ask line.
                // Both methods are synchronized, and of the same class.
                super.setActualPrices(linePriceAdded, super.getActualPriceBid());
                this.triggerStopOrders();
            } else {
                if (linePriceAdded.getValue() < bestAsk.getValue()) {
                    // Both methods are synchronized, and of the same class.
                    super.setActualPrices(linePriceAdded, super.getActualPriceBid());
                    this.triggerStopOrders();
                } else {
                    // The added price is not more convenient of the present one.
                }
            }

        } else if (linePriceAdded.getType() == PriceType.BID) {

            if (bestBid == null) {
                // First bid line.
                super.setActualPrices(super.getActualPriceAsk(), linePriceAdded);
                this.triggerStopOrders();
            } else {
                if (linePriceAdded.getValue() > bestBid.getValue()) {
                    super.setActualPrices(super.getActualPriceAsk(), linePriceAdded);
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
     * @throws IllegalArgumentException If the price line removed market not match with order book market.
     * 
     */
    private synchronized void updateActualPricesRemove(SpecificPrice linePriceRemoved) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (linePriceRemoved == null) {
            throw new NullPointerException("Line price removed, to be used to update the actuals (best) market prices, cannot be null.");
        }

        // Market checks.
        // Checked in this way since the order book extends the market class.
        if (linePriceRemoved.getMarket().getPrimaryCurrency() != this.getPrimaryCurrency() || linePriceRemoved.getMarket().getSecondaryCurrency() != this.getSecondaryCurrency()) {
            throw new IllegalArgumentException("Line price removed, to be used to update the actuals (best) market prices, market not match with order book market.");
        }

        // Price has no setters, no synchronization needed.

        // Iterating in ascending order. E.g.:
        // ask, ask, best ask, best bid, bid, bid

        SpecificPrice bestAsk = super.getActualPriceAsk();
        SpecificPrice bestBid = super.getActualPriceBid();
        if (linePriceRemoved.getType() == PriceType.ASK) {

            if (bestAsk.getValue() == linePriceRemoved.getValue()) {

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
                // Both methods are synchronized, and of the same class.
                super.setActualPrices(newBestAsk, super.getActualPriceBid());
                this.triggerStopOrders();

            }

        } else if (linePriceRemoved.getType() == PriceType.BID) {

            if (bestBid.getValue() == linePriceRemoved.getValue()) {

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
                super.setActualPrices(super.getActualPriceAsk(), newBestBid);
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

            SpecificPrice bestAsk = super.getActualPriceAsk();
            SpecificPrice bestBid = super.getActualPriceBid();
    
            OrderBookLine<StopMarketOrder> lineAsk = null;
            OrderBookLine<StopMarketOrder> lineBid = null;
    
            // Remember that best ask and best bid can be null.
            if (bestAsk != null)
                lineAsk = stopBook.get(bestAsk);
            if (bestBid != null)
                lineBid = stopBook.get(bestBid);

            MarketOrder marketOrder = null;
            Boolean executedMarketOrder = null;
            if (lineAsk != null) {
                marketOrder = lineAsk.executeStopOrderFromStopLine(this);

                if (lineAsk.getOrdersNumber() == 0) {
                    // The line is empty, must be removed.
                    this.removeStopLine(lineAsk.getLinePrice());
                    break;
                }

                // Executing the market order getted from the stop order.
                executedMarketOrder = this.executeOrder(marketOrder);

                if (executedMarketOrder) {
                    // TODO: Here, executed market order.
                }
            
            }

            if (lineBid != null) {

                marketOrder = lineBid.executeStopOrderFromStopLine(this);

                if (lineBid.getOrdersNumber() == 0) {
                    // The line is empty, must be removed.
                    this.removeStopLine(lineBid.getLinePrice());
                    break;
                }

                // Executing the market order getted from the stop order.
                executedMarketOrder = this.executeOrder(marketOrder);

                if (executedMarketOrder) {
                    // TODO: Here, executed market order.
                }          
            
            }

            if (lineAsk == null && lineBid == null) {
                // No stop orders to execute.
                break;
            }

        }

    }

    // ORDERS EXECUTION
    // Overloading the method to handle the different types of orders in different ways.
    // The market order is the most complex to handle.
    /**
     * 
     * Execute a market order.
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
     * @throws IllegalArgumentException If the order's market not match with order book market.
     * 
     */
    public synchronized Boolean executeOrder(MarketOrder order) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (order == null) {
            throw new NullPointerException("A market order, to be executed in a order book, cannot be null.");
        }

        synchronized (order) {

            // Market checks.
            if (order.getMarket().compareTo(this) != 0) {
                throw new IllegalArgumentException("A market order's market, to be executed in a order book, not match with order book market.");
            }

            // Checking satisfability.
            Boolean satisfiable = false;
            Quantity totalQuantity = new Quantity(0);
            for (SpecificPrice price : limitBook.keySet()) {

                OrderBookLine<LimitOrder> line = limitBook.get(price);

                // Calculating the total quantity.
                if (order.getPrice().getType() == PriceType.BID) {
                    // We can exit before the last line, at the first ask line found, thanks to the sorted order book.
                    if (line.getLinePrice().getType() == PriceType.ASK) {
                        break;
                    }
                    totalQuantity =  new Quantity(totalQuantity.getValue() + line.getTotalQuantity().getValue());
                }else if (order.getPrice().getType() == PriceType.ASK) {
                    if (line.getLinePrice().getType() == PriceType.BID) {
                        continue;
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
                while (true) {

                    // Getting the best price.
                    // In the getter of the market order, the price is setted to the actual price of the market.
                    // No need to update it manually.
                    SpecificPrice bestPrice = order.getPrice();
                    OrderBookLine<LimitOrder> bestLine = limitBook.get(bestPrice);

                    // Executing the order.
                    Integer executed = -1;
                    executed = bestLine.executeMarketOrderOnLimitLine(order);

                    if (bestLine.getOrdersNumber() == 0) {
                        // The line is empty, must be removed.
                        this.removeLimitLine(bestLine.getLinePrice());
                    }

                    // Market order fullfilled.
                    if (executed == 0 || executed == 2) {
                        // TODO: Here, executed market order, pay attention to not process 2 times, here and in the triggerStopOrders method.
                        break;
                    }

                }
            }else{
                // The order is not satisfiable.
                return false;
            }

            return true;

        }

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
     * @throws IllegalArgumentException If the order market not match with order book market.
     * 
     */
    public synchronized void executeOrder(LimitOrder order) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (order == null) {
            throw new NullPointerException("A limit order, to be executed in a order book, cannot be null.");
        }

        // No copy, since we need to update the quantity during an order execution.

        SpecificPrice price = null;
        synchronized (order) {

            // Market checks.
            if (order.getMarket().compareTo(this) != 0) {
                throw new IllegalArgumentException("A limit order's market, to be executed in a order book, not match with order book market.");
            }

            price = order.getPrice();

            // Safe because synchronized.
            OrderBookLine<LimitOrder> limitLine = limitBook.get(price);

            // New price line creation.
            if (limitLine == null) {
                this.addLine(price, order);
                limitLine = limitBook.get(price);
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
     * Execute a stop order.
     * The order is added to the stop book.
     * 
     * A check if the order is already present in the list is omitted, because a O(n) operation would be needed, and the O(1) operation speed given by the list would be lost.
     * 
     * Synchronized to avoid concurrency problems, to protect the stop book.
     * Synchonized also on the order to avoid modifications of it during the execution.
     * 
     * @param order The stop order to execute.
     * 
     * @throws NullPointerException If the order is null.
     * @throws IllegalArgumentException If the order market not match with order book market.
     * 
     */
    public synchronized void executeOrder(StopMarketOrder order) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (order == null) {
            throw new NullPointerException("A stop order, to be executed in a order book, cannot be null.");
        }

        // No copy, since we need to update the quantity during an order execution.

        synchronized (order) {

            // Market checks.
            if (order.getMarket().compareTo(this) != 0) {
                throw new IllegalArgumentException("A stop order's market, to be executed in a order book, not match with order book market.");
            }

            SpecificPrice price = order.getPrice();
            OrderBookLine<StopMarketOrder> stopLine = stopBook.get(price);

            // New price line creation.
            if (stopLine == null) {
                this.addLine(price, order);
                // Best prices updated in the addLine method.
                // Order added in the constructor of the new line.
                return;
            }

            // A check if the order is already present in the list is omitted, because a O(n) operation would be needed, and the O(1) operation speed given by the list would be lost.
            // Adding the order to the line.
            stopLine.addOrder(order);


        }

    }

    // GETTERS
    /**
     * 
     * Get the market object associated with the order book.
     * Could be needed in future implementations that need a market object but not an order book.
     * 
     * Synchronized to copy the actual prices in a safe way.
     * 
     * @return The market object associated with the order book as a new object.
     * 
     */
    public synchronized Market getMarket() {

        Market newMarket = new Market(super.getPrimaryCurrency(), super.getSecondaryCurrency(), super.getIncrement());
        if (super.getActualPriceAsk() != null)
            newMarket.setActualPrices(super.getActualPriceAsk(), super.getActualPriceBid());
        if (super.getActualPriceBid() != null)
            newMarket.setActualPrices(super.getActualPriceAsk(), super.getActualPriceBid());

        return newMarket;

    }


}
