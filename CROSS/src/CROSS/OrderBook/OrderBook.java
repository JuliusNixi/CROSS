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
import CROSS.Types.Price.SpecificPrice;
import CROSS.Utils.Separator;

/**
 * The order book is the core of the market.
 * It extends the Market class.
 * It contains all the orders.
 * It's used to match the orders.
 * It's used to execute the orders.
 * It contains the limit orders book and the stop orders book.
 * @version 1.0
 * @see Market
 * @see OrderBookLine
 * @see LimitOrder
 * @see StopMarketOrder
 * @see MarketOrder
 * @see Order
 * @see Currency
 * @see Quantity
 * @see SpecificPrice
 * @see GenericPrice
 * @see Separator
 * @see PriceType
 */
public class OrderBook extends Market {

    // By using a TreeMap, the order book is always sorted by price.
    private TreeMap<SpecificPrice, OrderBookLine<LimitOrder>> limitBook;

    // Technically the order book contains only the limit orders.
    // The majority of the brokers not show the stop orders in the order book.
    // The stop orders are hidden and are only executed when the price hits the stop price.
    // So i will follow this philosophy.

    // I will use the same data structure because i think that it fits well also for the stop orders.
    // But, the OFFICIAL order book is the limit orders book, that contains only the limit orders.
    // The stop orders book is "opaque".
    private TreeMap<SpecificPrice, OrderBookLine<StopMarketOrder>> stopBook;

    /**
     * It's private because it's used only by the class.
     * Add a line to the order book.
     * The line is added to the limit book or to the stop book.
     * The first order is used to detect the correct book to use.
     * This method is intended to create a NEW LINE, if the line with the specified price already exists, an exception will be throwed.
     * @param <O> Order type, could be LimitOrder or StopMarketOrder.
     * @param linePrice The price of the line.
     * @param initialOrder The first order to add to the line.
     * @throws IllegalArgumentException If the initialOrder market not match with order book market, if the initialOrder price not match with linePrice, if the initialOrder type not supported or if the line with the specified price already exists.
     * @throws NullPointerException If the initialOrder or the linePrice are null.
     */
    private <O extends Order> void addLine(SpecificPrice linePrice, O initialOrder) throws IllegalArgumentException, NullPointerException {
        if (initialOrder == null) {
            throw new NullPointerException("initialOrder is null.");
        }
        if (linePrice == null) {
            throw new NullPointerException("linePrice is null.");
        }
        
        if (initialOrder.getMarket().getPrimaryCurrency() != super.getPrimaryCurrency() || initialOrder.getMarket().getSecondaryCurrency() != super.getSecondaryCurrency()) {
            throw new IllegalArgumentException("initialOrder market not match with order book market.");
        }

        if (initialOrder.getPrice().getValue() != linePrice.getValue()) {
            throw new IllegalArgumentException("initialOrder price not match with line price.");
        }

        if (initialOrder instanceof LimitOrder) {
            if (this.limitBook.containsKey(initialOrder.getPrice()))
                throw new IllegalArgumentException("Line with this price already exists in the limit book.");
            OrderBookLine<LimitOrder> line = new OrderBookLine<LimitOrder>(linePrice, initialOrder);
            this.limitBook.put(linePrice, line);
        } else if (initialOrder instanceof StopMarketOrder) {
            if (this.stopBook.containsKey(initialOrder.getPrice()))
                throw new IllegalArgumentException("Line with this price already exists in the stop book.");
            OrderBookLine<StopMarketOrder> line = new OrderBookLine<StopMarketOrder>(linePrice, initialOrder);
            this.stopBook.put(linePrice, line);
        } else {
            throw new IllegalArgumentException("initialOrder type not supported.");
        }
    }

    /**
     * Constructor of the OrderBook class.
     * @param primary_currency The primary currency of the market.
     * @param secondary_currency The secondary currency of the market.
     * @param actualPriceAsk The actual ask price.
     * @param actualPriceBid The actual bid price.
     * @param increment The price increment.
     */
    public OrderBook(Currency primary_currency, Currency secondary_currency, SpecificPrice actualPriceAsk, SpecificPrice actualPriceBid, GenericPrice increment) {
        super(primary_currency, secondary_currency, actualPriceAsk, actualPriceBid, increment);
        limitBook = new TreeMap<SpecificPrice, OrderBookLine<LimitOrder>>();
        stopBook = new TreeMap<SpecificPrice, OrderBookLine<StopMarketOrder>>();
    }

    @Override
    public String toString() {
        String superInfo = super.toString();
        String separator = new Separator("-", superInfo.length()).toString();

        // Adding the basic market info.
        String result = separator + superInfo + "\n" + separator;

        // I want to divide the best ask and the best bid.
        // So i need to know the position of the best bid.
        LinkedList<String> lines = new LinkedList<String>();
        separator = new Separator("*").toString();
        Integer beforeLineBidIndex = 0;
        Integer counter = 0;
        for (OrderBookLine<LimitOrder> line : limitBook.values()) {
            String lineStr = line.toStringWithOrders();
            if (line.getLinePrice().getValue() == actualPriceAsk.getValue()) {
                lineStr += separator;
            }
            if (line.getLinePrice().getValue() == actualPriceBid.getValue()) {
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

    // Overloading the method to handle the different types of orders.
    // The market order is the most complex to handle.
    public void executeOrder(MarketOrder order) {
        // TODO: Implement the executeOrder method.
    }
    /**
     * Execute a limit order.
     * The order is added to the limit book.
     * @param order The limit order to execute.
     * @throws NullPointerException If the order is null.
     * @throws IllegalArgumentException If the order market not match with order book market.
     */
    public void executeOrder(LimitOrder order) throws NullPointerException, IllegalArgumentException {

        if (order == null) {
            throw new NullPointerException("Order is null.");
        }

        if (order.getMarket().getPrimaryCurrency() != super.getPrimaryCurrency() || order.getMarket().getSecondaryCurrency() != super.getSecondaryCurrency()) {
            throw new IllegalArgumentException("Order market not match with order book market.");
        }

        SpecificPrice price = order.getPrice();
        OrderBookLine<LimitOrder> limitLine = limitBook.get(price);

        // New price line creation.
        if (limitLine == null) {
            this.addLine(price, order);
            // The addLine method will automatically add the order to the line.
            return;
        }

        // Adding the order to the line.
        limitLine.addOrder(order);

    } 
    /**
     * Execute a stop order.
     * The order is added to the stop book.
     * @param order The stop order to execute.
     * @throws NullPointerException If the order is null.
     * @throws IllegalArgumentException If the order market not match with order book market.
     */
    public void executeOrder(StopMarketOrder order) throws NullPointerException, IllegalArgumentException {

        if (order == null) {
            throw new NullPointerException("Order is null.");
        }

        if (order.getMarket().getPrimaryCurrency() != super.getPrimaryCurrency() || order.getMarket().getSecondaryCurrency() != super.getSecondaryCurrency()) {
            throw new IllegalArgumentException("Order market not match with order book market.");
        }

        SpecificPrice price = order.getPrice();
        OrderBookLine<StopMarketOrder> stopLine = stopBook.get(price);

        // New price line creation.
        if (stopLine == null) {
            this.addLine(price, order);
            // The addLine method will automatically add the order to the line.
            return;
        }

        // Adding the order to the line.
        stopLine.addOrder(order);
    }

    // TODO: Implement the removeOrder method. In the method remove empty lines after a check.

}
