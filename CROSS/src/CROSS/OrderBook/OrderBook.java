package CROSS.OrderBook;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import CROSS.Enums.Direction;
import CROSS.Order.LimitOrder;
import CROSS.Order.MarketOrder;
import CROSS.Order.StopMarketOrder;
import CROSS.Types.Currency;
import CROSS.Types.Quantity;
import CROSS.Types.Price.GenericPrice;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Utils.Separator;

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
    // So, for example, the toString() method will show only the limit orders.
    private TreeMap<SpecificPrice, OrderBookLine<StopMarketOrder>> stopBook;

    // The first order is used to detect the correct book to use.
    private void addLine(SpecificPrice price, LimitOrder order) throws IllegalArgumentException {
        if (order.getMarket().getPrimaryCurrency() != super.getPrimaryCurrency() || order.getMarket().getSecondaryCurrency() != super.getSecondaryCurrency()) {
            throw new IllegalArgumentException("Order market not match with order book market.");
        }
        OrderBookLine<LimitOrder> line = new OrderBookLine<LimitOrder>(price, LimitOrder.class);
        this.limitBook.put(price, line);
    }
    private void addLine(SpecificPrice price, StopMarketOrder order) throws IllegalArgumentException {
        if (order.getMarket().getPrimaryCurrency() != super.getPrimaryCurrency() || order.getMarket().getSecondaryCurrency() != super.getSecondaryCurrency()) {
            throw new IllegalArgumentException("Order market not match with order book market.");
        }
        OrderBookLine<StopMarketOrder> line = new OrderBookLine<StopMarketOrder>(price, StopMarketOrder.class);
        this.stopBook.put(price, line);
    }
    
    public OrderBook(Currency primary_currency, Currency secondary_currency, SpecificPrice actualPriceAsk, SpecificPrice actualPriceBid, GenericPrice increment) {
        super(primary_currency, secondary_currency, actualPriceAsk, actualPriceBid, increment);
        limitBook = new TreeMap<SpecificPrice, OrderBookLine<LimitOrder>>();
        stopBook = new TreeMap<SpecificPrice, OrderBookLine<StopMarketOrder>>();
    }

    @Override
    public String toString() {
        String separator = "";
        String superInfo = super.toString();
        separator = Separator.getSeparator("-", superInfo.length());
        String result = separator + superInfo + "\n" + separator;
        LinkedList<String> lines = new LinkedList<String>();
        separator = Separator.getSeparator("*");
        Integer beforeLineBidIndex = 0;
        Integer counter = 0;
        for (OrderBookLine<LimitOrder> line : limitBook.values()) {
            String lineStr = line.toStringWithOrders();
            if (line.getPrice().getValue() == actualPriceAsk.getValue()) {
                lineStr += separator;
            }
            if (line.getPrice().getValue() == actualPriceBid.getValue()) {
                beforeLineBidIndex = counter;
            }
            lines.add(lineStr);
            counter++;
        }
        lines.add(beforeLineBidIndex, separator);
        for (String line : lines) {
            result += line;
        }
        return result;
    }

    private Boolean checkMarketOrderSatisfability(MarketOrder order) throws IllegalArgumentException, RuntimeException { 

        if (order.getMarket() != this) {
            throw new IllegalArgumentException("Order market not match with order book market.");
        }

        // Getting the actual opposite price.
        SpecificPrice actualOppositePrice;
        if (order.getDirection() == Direction.BUY) {
            actualOppositePrice = actualPriceBid;
        } else {
            actualOppositePrice = actualPriceAsk;
        }
        // Get the actual opposite price line.
        OrderBookLine<LimitOrder> limitLine = limitBook.get(actualOppositePrice);
        if (limitLine == null || limitLine.getOrdersNumber() == 0) {
            throw new RuntimeException("Invalid actual price.");
        }

        Quantity avaibleTotalQuantity = new Quantity(0);
        while (true) {
            // Empty line for this price.
            if (limitLine == null) continue;

            // Avaible quantity for this price.
            avaibleTotalQuantity.setQuantity(avaibleTotalQuantity.getQuantity() + limitLine.checkMarketOrderLineSatisfability(order).getQuantity());
            // The order could be satisfied.
            if (order.getQuantity().getQuantity() == avaibleTotalQuantity.getQuantity()) {
                return true;
            }

            SpecificPrice maxPrice = limitBook.lastKey();
            SpecificPrice minPrice = limitBook.firstKey();
            if (order.getDirection() == Direction.BUY) {
                // Going to the next price.
                actualOppositePrice = new SpecificPrice(actualOppositePrice.getValue() + super.getIncrement().getValue(), actualOppositePrice.getType());
                if (actualOppositePrice.getValue() > maxPrice.getValue()) {
                    return false;
                }
            } else {
                actualOppositePrice = new SpecificPrice(actualOppositePrice.getValue() - super.getIncrement().getValue(), actualOppositePrice.getType());
                if (actualOppositePrice.getValue() < minPrice.getValue()) {
                    return false;
                }
            }

        }

    }

    // Overloading the method to handle the different types of orders.
    public Boolean executeOrder(MarketOrder order) {
        if (this.checkMarketOrderSatisfability(order)) {
            Quantity currentRemainingQuantity = new Quantity(order.getQuantity().getQuantity());
            while (currentRemainingQuantity.getQuantity() > 0){

                SpecificPrice actualOppositePrice;
                if (order.getDirection() == Direction.BUY) {
                    actualOppositePrice = actualPriceBid;
                } else {
                    actualOppositePrice = actualPriceAsk;
                }

                OrderBookLine<LimitOrder> limitLine = limitBook.get(actualOppositePrice);
                if (limitLine == null || limitLine.getOrdersNumber() == 0) {
                    throw new RuntimeException("Invalid actual price.");
                }

                while (true) {
                    HashMap<Quantity, HashMap<LinkedList<LimitOrder>, LimitOrder>> executedOrders = limitLine.executeMarketOrderOnLine(order);
                    currentRemainingQuantity.setQuantity(currentRemainingQuantity.getQuantity() - executedOrders.keySet().iterator().next().getQuantity());
                    if (currentRemainingQuantity.getQuantity() == 0) {
                        return true;
                    }
                }

            }
        }
        
        return false;

    }

    public void executeOrder(LimitOrder order) {
        SpecificPrice price = order.getPrice();
        OrderBookLine<LimitOrder> limitLine = limitBook.get(price);

        // New price line creation.
        if (limitLine == null) {
            this.addLine(price, order);
            limitLine = limitBook.get(price);
        }

        // Adding the order to the line.
        limitLine.addOrder(order);

    }

    public void executeOrder(StopMarketOrder order) {
        SpecificPrice price = order.getPrice();

        OrderBookLine<StopMarketOrder> stopLine = stopBook.get(price);
        // New price line creation.
        if (stopLine == null) {
            this.addLine(price, order);
            stopLine = stopBook.get(price);
        }

        // Adding the order to the line.
        stopLine.addOrder(order);
    }

    public LinkedList<LimitOrder> getLimitOrders() {
        LinkedList<LimitOrder> orders = new LinkedList<LimitOrder>();
        for (OrderBookLine<LimitOrder> line : limitBook.values()) {
            orders.addAll(line.getOrders());
        }
        return orders;
    }

    // TODO: Implement the removeOrder method.
}
