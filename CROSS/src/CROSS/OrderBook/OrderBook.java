package CROSS.OrderBook;

import java.util.LinkedList;
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
import CROSS.Users.User;
import CROSS.Utils.Separator;

/**
 * 
 * The order book is the core of a market.
 * 
 * It extends the Market class.
 * 
 * It contains all the orders both limit and stop.
 * 
 * It's used to match and execute the orders.
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
 * 
 * @see SpecificPrice
 * 
 * @see MarketOrder
 * @see Order
 * @see Currency
 * @see Quantity
 * @see GenericPrice
 * @see Separator
 * @see PriceType
 * 
 */
public class OrderBook extends Market {

    // By using a TreeMap, the order book is always sorted by price.
    private TreeMap<SpecificPrice, OrderBookLine<LimitOrder>> limitBook;

    // Technically the order book contains only the limit orders.
    // The majority of the brokers not show the stop orders in the order book.
    // The stop orders are hidden and are only executed when the price hits the stop price becoming a market order.
    // So I will follow this philosophy.

    // I will use the same data structure because I think that it fits well also for the stop orders.
    // But, the OFFICIAL order book is the limit orders book, that contains only the limit orders.
    // So the stop orders book is "opaque".
    private TreeMap<SpecificPrice, OrderBookLine<StopMarketOrder>> stopBook;

    /**
     * 
     * Constructor of the class.
     * 
     * @param primary_currency The primary currency of the market.
     * @param secondary_currency The secondary currency of the market.
     * @param actualPriceAsk The actual ask price.
     * @param actualPriceBid The actual bid price.
     * @param increment The price increment.
     * 
     */
    public OrderBook(Currency primary_currency, Currency secondary_currency, SpecificPrice actualPriceAsk, SpecificPrice actualPriceBid, GenericPrice increment) {
        
        super(primary_currency, secondary_currency, actualPriceAsk, actualPriceBid, increment);

        limitBook = new TreeMap<SpecificPrice, OrderBookLine<LimitOrder>>();
        stopBook = new TreeMap<SpecificPrice, OrderBookLine<StopMarketOrder>>();

    }

    // LINES MANAGEMENT
    /**
     * It's private because it's used only by the class.
     * 
     * Add a line to the order book.
     * The line is added to the limit book or to the stop book.
     * 
     * The first order is used to detect the correct book to use.
     * NB: The first order is NOT added to the line.
     * 
     * This method is intended to create a NEW LINE, if the line with the specified price already exists, an exception will be throwed.
     * 
     * @param <O> Order type, could be LimitOrder or StopMarketOrder.
     * @param linePrice The price of the line.
     * @param initialOrder The first order to add to the line.
     * 
     * @throws IllegalArgumentException If the initialOrder market not match with order book market. If the price value of the initialOrder not match with the line price value. If the price type of the initialOrder not match with the line price type. If the price market of the initialOrder not match with the line price market. If the line with the specified price already exists in the book. If the order type is not supported.
     * @throws NullPointerException If the initialOrder or the linePrice are null.
     */
    private <GenericOrder extends Order> void addLine(SpecificPrice linePrice, GenericOrder initialOrder) throws IllegalArgumentException, NullPointerException {
        if (initialOrder == null) {
            throw new NullPointerException("Initial order cannot be null.");
        }
        if (linePrice == null) {
            throw new NullPointerException("Line price cannot be null.");
        }
        
        // Market checks.
        if (!initialOrder.getMarket().equals(this)) {
            throw new IllegalArgumentException("Initial order market not match with order book market.");
        }

        // Price checks.
        if (!initialOrder.getPrice().equals(linePrice)) {
            throw new IllegalArgumentException("Initial order price not match with line price.");
        }
        if (initialOrder.getPrice().getType() != linePrice.getType()) {
            throw new IllegalArgumentException("Initial order price type not match with line price type.");
        }

        if (initialOrder instanceof LimitOrder) {
            if (this.limitBook.containsKey(initialOrder.getPrice()))
                throw new IllegalArgumentException("Line with this price already exists in the limit book.");
            LimitOrder order = (LimitOrder) initialOrder;
            OrderBookLine<LimitOrder> line = new OrderBookLine<LimitOrder>(linePrice, order);
            this.limitBook.put(linePrice, line);
        } else if (initialOrder instanceof StopMarketOrder) {
            if (this.stopBook.containsKey(initialOrder.getPrice()))
                throw new IllegalArgumentException("Line with this price already exists in the stop book.");
            StopMarketOrder order = (StopMarketOrder) initialOrder;
            OrderBookLine<StopMarketOrder> line = new OrderBookLine<StopMarketOrder>(linePrice, order);
            this.stopBook.put(linePrice, line);
        } else {
            throw new IllegalArgumentException("Initial order type not supported.");
        }

    }
    /**
     * It's private because it's used only by the class.
     * 
     * Remove a line from the order book.
     * The line is removed from the limit book or from the stop book.
     * 
     * The last order is used to detect the correct book to use.
     * NB: The last order is ALSO removed from the line.
     * 
     * This method is intended to remove a line with only one order, if the line with the specified price not exists, an exception will be throwed.
     * An exception will be throwed also if the line contains more than one order and if this the last order is not the one to remove.
     * 
     * @param <O> Order type, could be LimitOrder or StopMarketOrder.
     * @param linePrice The price of the line.
     * @param lastOrder The last order to remove from the line.
     * 
     * @throws IllegalArgumentException If the lastOrder market not match with order book market. If the price value of the lastOrder not match with the line price value. If the price type of the lastOrder not match with the line price type. If the price market of the lastOrder not match with the line price market. If the line with the specified price not exists in the book. If the line with the specified price contains not with only this order in the book. If the given order not match with the last order in the line. If the order type is not supported.
     * @throws NullPointerException If the lastOrder or the linePrice are null.
     */
    private <O extends Order> void removeLine(SpecificPrice linePrice, O lastOrder) throws IllegalArgumentException, NullPointerException {
    
        if (linePrice == null) {
            throw new NullPointerException("Line price cannot be null.");
        }
        if (lastOrder == null) {
            throw new NullPointerException("Order for type stop limit cannot be null.");
        }

        // Market checks.
        if (!lastOrder.getMarket().equals(this)) {
            throw new IllegalArgumentException("Order market not match with order book market.");
        }

        // Price checks.
        if (!lastOrder.getPrice().equals(linePrice)) {
            throw new IllegalArgumentException("Order price not match with line price.");
        }
        if (lastOrder.getPrice().getType() != linePrice.getType()) {
            throw new IllegalArgumentException("Order price type not match with line price type.");
        }

        if (lastOrder instanceof LimitOrder) {

            // Checking if the line exists.
            if (!this.limitBook.containsKey(lastOrder.getPrice()))
                throw new IllegalArgumentException("Line with this price not exists in the limit book.");
            
            // Preventing the removal of a line with more than one order.
            if (this.limitBook.get(lastOrder.getPrice()).getOrdersNumber() != 1)
                throw new IllegalArgumentException("Line with this price not with only this order in the limit book.");

            // Checking if the last order is the same as the one to remove.
            LimitOrder order = this.limitBook.get(lastOrder.getPrice()).extractLastOrder(false);
            if (!order.equals(lastOrder))
                throw new IllegalArgumentException("Order not match with the last order in the line.");

            // Removing the last order.
            this.limitBook.get(lastOrder.getPrice()).extractLastOrder(true);

            // Removing the line.
            this.limitBook.remove(lastOrder.getPrice());

        } else if (lastOrder instanceof StopMarketOrder) {

            // Checking if the line exists.
            if (!this.stopBook.containsKey(lastOrder.getPrice()))
                throw new IllegalArgumentException("Line with this price not exists in the stop book.");

            // Preventing the removal of a line with more than one order.
            if (this.stopBook.get(lastOrder.getPrice()).getOrdersNumber() != 1)
                throw new IllegalArgumentException("Line with this price not with only this order in the stop book.");
            
            // Checking if the last order is the same as the one to remove.
            StopMarketOrder order = this.stopBook.get(lastOrder.getPrice()).extractLastOrder(false);
            if (!order.equals(lastOrder))
                throw new IllegalArgumentException("Order not match with the last order in the line.");

            // Removing the last order.
            this.stopBook.get(lastOrder.getPrice()).extractLastOrder(true);

            // Removing the line.
            this.stopBook.remove(lastOrder.getPrice());
            
        } else {
            throw new IllegalArgumentException("Order type not supported.");
        }

    }
    /**
     * Public.
     * 
     * Get a line from the order book.
     * The line is get from the limit book or from the stop book.
     * 
     * @param <O> Order type, could be LimitOrder or StopMarketOrder.
     * @param price The price of the line to get.
     * 
     * @return The line with the specified price, a reference.
     * 
     * @throws NullPointerException If the price is null.
     * @throws IllegalArgumentException If the price market not match with order book market. If the order type is not supported.
     */
    public <O extends Order> OrderBookLine<?> getLine(SpecificPrice price) throws NullPointerException, IllegalArgumentException {
        if (price == null) {
            throw new NullPointerException("Price cannot be null.");
        }

        // Market checks.
        if (!price.getMarket().equals(this)) {
            throw new IllegalArgumentException("Price market not match with order book market.");
        }

        O order = null;
        if (order instanceof LimitOrder) {
            return (OrderBookLine<LimitOrder>) this.limitBook.get(price);
        } else if (order instanceof StopMarketOrder) {
            return (OrderBookLine<StopMarketOrder>) this.stopBook.get(price);
        } else {
            throw new IllegalArgumentException("Order type not supported.");
        }

    }

    // Super info + limit book lines.
    @Override
    public String toString() {
        String superInfo = super.toString();
        String separator = new Separator("-", superInfo.length()).toString();

        // Adding the basic market info.
        String result = separator + superInfo + "\n" + separator;

        // I want to divide the best ask and the best bid.
        // So I need to know the position of the best bid.
        LinkedList<String> lines = new LinkedList<String>();
        separator = new Separator("*").toString();
        Integer beforeLineBidIndex = 0;
        Integer counter = 0;
        for (OrderBookLine<LimitOrder> line : limitBook.values()) {
            String lineStr = line.toStringWithOrders();
            if (line.getLinePrice().getValue() == super.getActualPriceAsk().getValue()) {
                lineStr += separator;
            }
            if (line.getLinePrice().getValue() == super.getActualPriceBid().getValue()) {
                beforeLineBidIndex = counter;
            }
            lines.add(lineStr);
            counter++;
        }

        // Inserting the separator at the previous found bid index.
        lines.add(beforeLineBidIndex, separator);

        // Joining the lines.
        for (String line : lines) {
            result += line;
        }
        return result;
    }

    // ACTUAL PRICES MANAGEMENT
    /**
     * It's private because it's used only by the class.
     * 
     * Update the actual prices of the market, both the best ask and the best bid.
     * THIS MUST BE CALLED AT EACH LINE ADDED, AFTER.
     * IT'S ITENDED TO WORKS ONLY BY WITH THE LIMIT BOOK.
     * 
     * @param linePriceAdded The line price added.
     * 
     * @throws NullPointerException If the linePriceAdded is null.
     * @throws IllegalArgumentException If the linePriceAdded market not match with order book market.
     */
    private void updateActualPricesAdd(SpecificPrice linePriceAdded) throws NullPointerException, IllegalArgumentException {
        if (linePriceAdded == null) {
            throw new NullPointerException("Line price cannot be null.");
        }

        // Market checks.
        if (linePriceAdded.getMarket().equals(this)) {
            throw new IllegalArgumentException("Line price market not match with order book market.");
        }

        // Iterating in ascending order price lines. E.g.:
        // ask, ask, best ask, best bid, bid, bid

        SpecificPrice bestAsk = super.getActualPriceAsk();
        SpecificPrice bestBid = super.getActualPriceBid();
        if (linePriceAdded.getType() == PriceType.ASK) {
            if (bestAsk == null) {
                // First ask line.
                super.setActualPrices(linePriceAdded, super.getActualPriceBid());
                this.triggerStopOrders();
            } else {
                if (linePriceAdded.getValue() > bestAsk.getValue()) {
                    super.setActualPrices(linePriceAdded, super.getActualPriceBid());
                    this.triggerStopOrders();
                }
            }
        } else if (linePriceAdded.getType() == PriceType.BID) {
            if (bestBid == null) {
                // First bid line.
                super.setActualPrices(super.getActualPriceAsk(), linePriceAdded);
                this.triggerStopOrders();
            } else {
                if (linePriceAdded.getValue() < bestBid.getValue()) {
                    super.setActualPrices(super.getActualPriceAsk(), linePriceAdded);
                    this.triggerStopOrders();
                }
            }
        }

    }
    /**
     * It's private because it's used only by the class.
     * 
     * Update the actual prices of the market, both the best ask and the best bid.
     * THIS MUST BE CALLED AT EACH LINE REMOVED, AFTER.
     * 
     * @param linePriceRemove The line price removed.
     * 
     * @throws NullPointerException If the linePriceRemove is null.
     * @throws IllegalArgumentException If the linePriceRemove market not match with order book market.
     */
    private void updateActualPricesRemove(SpecificPrice linePriceRemove) throws NullPointerException, IllegalArgumentException {
        if (linePriceRemove == null) {
            throw new NullPointerException("Line price cannot be null.");
        }

        // Market checks.
        if (linePriceRemove.getMarket().equals(this)) {
            throw new IllegalArgumentException("Line price market not match with order book market.");
        }

        // Iterating in ascending order. E.g.:
        // ask, ask, best ask, best bid, bid, bid

        SpecificPrice bestAsk = super.getActualPriceAsk();
        SpecificPrice bestBid = super.getActualPriceBid();
        if (linePriceRemove.getType() == PriceType.ASK) {
            if (bestAsk.getValue() == linePriceRemove.getValue()) {
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
                super.setActualPrices(newBestAsk, super.getActualPriceBid());
                this.triggerStopOrders();
            }
        } else if (linePriceRemove.getType() == PriceType.BID) {
            if (bestBid.getValue() == linePriceRemove.getValue()) {
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
                super.setActualPrices(super.getActualPriceAsk(), newBestBid);
                this.triggerStopOrders();
            }
        }
    }
    /**
     * It's private because it's used only by the class.
     * 
     * This method MUST be called after the actual prices are updated.
     * 
     * It's execute all the stop orders (if no fail occurs) that are in the stop book on the new actual prices lines.
     */
    private void triggerStopOrders() {

        SpecificPrice bestAsk = super.getActualPriceAsk();
        SpecificPrice bestBid = super.getActualPriceBid();

        OrderBookLine<StopMarketOrder> lineAsk = stopBook.get(bestAsk);
        OrderBookLine<StopMarketOrder> lineBid = stopBook.get(bestBid);

        TreeMap<User, LinkedList<Order>> executedOrders = new TreeMap<User, LinkedList<Order>>();

        while (true) {

            MarketOrder order = null;
            Boolean executed = null;
            if (lineAsk != null) {
                order = lineAsk.executeStopOrder();

                if (order == null) {
                    // Last order on the line.
                    StopMarketOrder stop = lineAsk.extractLastOrder(false);

                    order = new MarketOrder(stop.getMarket(), stop.getPrice().getType(), stop.getQuantity(), stop.getUser());

                    order.setId(stop.getId());

                    this.removeLine(bestAsk, stop);

                    lineAsk = null;
                }else {
                    // Removing the order from the line.
                    lineAsk.extractLastOrder(true);
                }

                executed = this.executeOrder(order);

                if (executed) {
                    StopMarketOrder stop =  new StopMarketOrder(order.getPrice(), order.getQuantity(), order.getUser());
                    stop.setId(order.getId());
                    if (executedOrders.containsKey(order.getUser())) {
                        executedOrders.get(order.getUser()).add(stop);
                    }else {
                        LinkedList<Order> orders = new LinkedList<Order>();
                        orders.add(stop);
                        executedOrders.put(order.getUser(), orders);
                    }
                    for (User u : executedOrders.keySet()) {
                        // u.notifyTrades(executedOrders.get(u));
                    }
                }
            }
            if (lineBid != null) {
                // Cannot be null.
                order = lineBid.executeStopOrder();

                if (order == null) {
                    // Last order on the line.
                    StopMarketOrder stop = lineBid.extractLastOrder(false);

                    order = new MarketOrder(stop.getMarket(), stop.getPrice().getType(), stop.getQuantity(), stop.getUser());

                    order.setId(stop.getId());

                    this.removeLine(bestBid, stop);

                    lineBid = null;
                }else {
                    // Removing the order from the line.
                    lineBid.extractLastOrder(true);
                }

                executed = this.executeOrder(order);
                if (executed) {
                    StopMarketOrder stop =  new StopMarketOrder(order.getPrice(), order.getQuantity(), order.getUser());
                    stop.setId(order.getId());
                    if (executedOrders.containsKey(order.getUser())) {
                        executedOrders.get(order.getUser()).add(stop);
                    }else {
                        LinkedList<Order> orders = new LinkedList<Order>();
                        orders.add(stop);
                        executedOrders.put(order.getUser(), orders);
                    }
                    for (User u : executedOrders.keySet()) {
                        // u.notifyTrades(executedOrders.get(u));
                    }
                }            
            }

            if (lineAsk == null && lineBid == null) 
                break;

        }

    }

    // ORDERS EXECUTION
    // Overloading the method to handle the different types of orders.
    // The market order is the most complex to handle.
    /**
     * Execute a market order.
     * The order is executed against the limit book.
     * 
     * If the order is satisfiable, the order is executed.
     * An order is considered satisfiable if the total quantity of the order is less than or equal to the total quantity of the limit book (each line).
     * So a market order can be executed at different prices for different quantities.
     * 
     * The order is updated with the actual price of the market.
     * 
     * @param order The market order to execute.
     * @return True if the order is satisfiable and executed, false otherwise.
     * 
     * @throws NullPointerException If the order is null.
     */
    public Boolean executeOrder(MarketOrder order) throws NullPointerException {

        if (order == null) {
            throw new NullPointerException("MarketOrder cannot be null.");
        }

        // Checking satisfability...
        Boolean satisfiable = false;
        Quantity totalQuantity = new Quantity(0);
        for (SpecificPrice price : limitBook.keySet()) {

            OrderBookLine<LimitOrder> line = limitBook.get(price);

            // Calculating the total quantity.
            if (order.getPrice().getType() == PriceType.BID) {
                if (line.getLinePrice().getType() == PriceType.ASK) {
                    continue;
                }
                totalQuantity =  new Quantity(totalQuantity.getValue() + line.getTotalQuantity().getValue());
            }else if (order.getPrice().getType() == PriceType.ASK) {
                // We can exit before the last line, at the first bid line.
                if (line.getLinePrice().getType() == PriceType.BID) {
                    break;
                }
                totalQuantity =  new Quantity(totalQuantity.getValue() + line.getTotalQuantity().getValue());
            }

            if (totalQuantity.getValue() >= order.getQuantity().getValue()) {
                satisfiable = true;
                break;
            }
        }

        if (satisfiable) {
            // Execute the order.
            while (true) {

                // Updating order's price with the actual price of the market since the order is a market order.
                order.setUpdatedPrice();

                // Getting the best price.
                SpecificPrice bestPrice = order.getPrice();
                OrderBookLine<LimitOrder> bestLine = limitBook.get(bestPrice);

                // Executing the order.
                // I know I am playing only with pointers, not with objects copies.
                MarketOrder copyOfOrder = order;
                order = bestLine.executeMarketOrder(order, this);

                if (order == null) {
                    order = copyOfOrder;

                    // Last order on the line.
                    this.removeLine(bestPrice, bestLine.extractLastOrder(false));

                    // Updating the actual prices.
                    this.updateActualPricesRemove(bestPrice);
                } 

                if (order.getQuantity().getValue() == 0) {
                    // The order is fully executed.
                    break;
                }

                // The order is not fully executed.
                continue;

            }
        }else{
            // The order is not satisfiable.
            return false;
        }

        return true;

    }
    /**
     * Execute a limit order.
     * The order is added to the limit book.
     * 
     * A check if the order is already present in the list is omitted, because a O(n) operation would be needed.
     * 
     * @param order The limit order to execute.
     * @throws NullPointerException If the order is null.
     * @throws IllegalArgumentException If the order market not match with order book market.
     * @return True if the order is executed, false otherwise.
     */
    public Boolean executeOrder(LimitOrder order) throws NullPointerException, IllegalArgumentException {

        if (order == null) {
            throw new NullPointerException("LimitOrder cannot be null.");
        }

        // Market checks.
        if (!order.getMarket().equals(this)) {
            throw new IllegalArgumentException("Order market not match with order book market.");
        }

        SpecificPrice price = order.getPrice();
        OrderBookLine<LimitOrder> limitLine = limitBook.get(price);

        // New price line creation.
        if (limitLine == null) {
            this.addLine(price, order);
            // Updating the actual prices.
            this.updateActualPricesAdd(price);
        }
        
        // A check if the order is already present in the list is omitted, because a O(n) operation would be needed.
        // Adding the order to the line.
        limitLine.addOrder(order);

        return true;

    } 
    /**
     * Execute a stop order.
     * The order is added to the stop book.
     * 
     * @param order The stop order to execute.
     * @throws NullPointerException If the order is null.
     * @throws IllegalArgumentException If the order market not match with order book market.
     * @return True if the order is executed, false otherwise.
     */
    public Boolean executeOrder(StopMarketOrder order) throws NullPointerException, IllegalArgumentException {

        if (order == null) {
            throw new NullPointerException("StopMarketOrder cannot be null.");
        }

        // Market checks.
        if (!order.getMarket().equals(this)) {
            throw new IllegalArgumentException("Order market not match with order book market.");
        }

        SpecificPrice price = order.getPrice();
        OrderBookLine<StopMarketOrder> stopLine = stopBook.get(price);

        // New price line creation.
        if (stopLine == null) {
            this.addLine(price, order);
        }

        // A check if the order is already present in the list is omitted, because a O(n) operation would be needed.
        // Adding the order to the line.
        stopLine.addOrder(order);

        return true;

    }

}
