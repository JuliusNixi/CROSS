package CROSS.OrderBook;

import java.util.HashMap;
import java.util.LinkedList;

import CROSS.Orders.LimitOrder;
import CROSS.Orders.MarketOrder;
import CROSS.Orders.Order;
import CROSS.Orders.StopMarketOrder;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;

// A line in a order book.
// It's used both by limit orders book and stop orders book.
public class OrderBookLine<T extends Order> {
 
    // This quantity is the sum of all orders in this line.
    private Quantity totalQuantity;

    // LinkedList to keep the order in the order they were added.
    // To execute the orders (matching algoritmh) we use FIFO, so i give priority
    // to insert and remove element from the head and tail.
    // Insert and execute from the head and tail is O(1). These operations should be the most frequent.
    // Cancel an order is O(n) because we need to search for the order.

    // Generic order to handle both limit and stop orders.
    private LinkedList<T> orders;

    private SpecificPrice price;

    private Class<T> lineType;

    // I don't want to pass an order to the constructor.
    // Because a line can be created without orders.
    // For that i need Class<T> to check the line type.
    public OrderBookLine(SpecificPrice price, Class<T> lineType) throws IllegalArgumentException {
        this.lineType = lineType;
        if (this.lineType.getSimpleName().compareTo(LimitOrder.class.getSimpleName()) == 0 || this.lineType.getSimpleName().compareTo(StopMarketOrder.class.getSimpleName()) == 0) {
            // OK.
            ;
        } else {
            throw new IllegalArgumentException("Order (alias line) type not supported, LimitOrder or StopMarketOrder are the only admitted.");
        }
        // Empty line.
        this.totalQuantity = new Quantity(0);
        this.orders = new LinkedList<T>();
        this.price = price;
    }

    // Add an order to the line.
    // Stop or limit order, it doesn't matter.
    // The line is omogeneus, all orders must have the same type.
    public void addOrder(T order) throws IllegalArgumentException {
        if (order.getClass().getSimpleName().compareTo(lineType.getSimpleName()) == 0) {
            orders.addFirst(order);
            Quantity newQuantity = new Quantity(this.getTotalQuantity().getQuantity() + order.getQuantity().getQuantity());
            this.totalQuantity = newQuantity;
        } else {
            throw new IllegalArgumentException("Order type doesn't match with line type.");
        }
    }

    public Quantity checkMarketOrderLineSatisfability(MarketOrder order) {
        Quantity satisfiableQuantity = new Quantity(0);

        Quantity orderQuantityCopy = new Quantity(order.getQuantity().getQuantity());
        LinkedList<T> ordersCopy = new LinkedList<T>(orders);
        while (orderQuantityCopy.getQuantity() > 0 && !ordersCopy.isEmpty()) {
            T tailOrder = ordersCopy.getLast();
            if (tailOrder.getQuantity().getQuantity() <= orderQuantityCopy.getQuantity()) {
                // Hitting the tail order, updating the executed quantity and the order quantity and continue hitting the next order.
                satisfiableQuantity = new Quantity(satisfiableQuantity.getQuantity() + tailOrder.getQuantity().getQuantity());
                orderQuantityCopy = new Quantity(orderQuantityCopy.getQuantity() - tailOrder.getQuantity().getQuantity());
                ordersCopy.removeLast();
                // If order quantity is 0, we can stop the loop at the next iteration.
            } else {
                // Order satisfied by the tail order.
                satisfiableQuantity = new Quantity(satisfiableQuantity.getQuantity() + orderQuantityCopy.getQuantity());
                tailOrder.setQuantity(new Quantity(tailOrder.getQuantity().getQuantity() - orderQuantityCopy.getQuantity()));
                orderQuantityCopy = new Quantity(0);
            }
        }
        return satisfiableQuantity;
    }
    // Returns an hash map with the quantity of the market order that was executed as key.
    // And as value an hash map with the executed orders list as key and the last order that was executed as value.
    // The last order is needed in case of partial execution.
    // If the order is not fully executed, the order is updated with the remaining quantity.
    // In this case we need to go on a different price/order book line from the caller.
    public HashMap<Quantity, HashMap<LinkedList<T>, T>> executeMarketOrderOnLine(MarketOrder order) {

        HashMap<Quantity, HashMap<LinkedList<T>, T>> totalQuantityAndExecutedOrdesList = new HashMap<Quantity, HashMap<LinkedList<T>, T>>();
        Quantity executedQuantity = new Quantity(0);
        LinkedList<T> executedOrders = new LinkedList<T>();
        T lastOrderExecuted = null;

        // While the order quantity is not 0 and there are orders in the line.
        while (order.getQuantity().getQuantity() > 0 && !orders.isEmpty()) {
            // Get last order in the line, the first arrived.
            T tailOrder = orders.getLast();
            if (tailOrder.getQuantity().getQuantity() <= order.getQuantity().getQuantity()) {
                // Hitting the tail order, removing it, updating the executed quantity and the order quantity and continue hitting the next order.
                executedQuantity = new Quantity(executedQuantity.getQuantity() + tailOrder.getQuantity().getQuantity());
                order.setQuantity(new Quantity(order.getQuantity().getQuantity() - tailOrder.getQuantity().getQuantity()));
                T o = orders.removeLast();
                // Moved to the executed orders list.
                executedOrders.addFirst(o);
                // If order quantity is 0, we can stop the loop at the next iteration.
            } else {
                // Order satisfied by the tail order.
                executedQuantity = new Quantity(executedQuantity.getQuantity() + order.getQuantity().getQuantity());
                tailOrder.setQuantity(new Quantity(tailOrder.getQuantity().getQuantity() - order.getQuantity().getQuantity()));
                order.setQuantity(new Quantity(0));
                // At the next iteration the order quantity will be 0, we can stop the loop.
                lastOrderExecuted = tailOrder;
            }
        }

        HashMap<LinkedList<T>, T> executedOrdersMap = new HashMap<LinkedList<T>, T>();
        executedOrdersMap.put(executedOrders, lastOrderExecuted);

        totalQuantityAndExecutedOrdesList.put(executedQuantity, executedOrdersMap);

        // Updating this class quantity.
        Quantity newQuantity = new Quantity(this.getTotalQuantity().getQuantity() - executedQuantity.getQuantity());
        this.totalQuantity = newQuantity;

        return totalQuantityAndExecutedOrdesList;
    }

    public Integer getOrdersNumber() {
        return orders.size();
    }
    public LinkedList<T> getOrders() {
        LinkedList<T> ordersCopy = new LinkedList<T>(this.orders);
        return ordersCopy;
    }
    public SpecificPrice getPrice() {
        return price;
    }
    public Class<T> getLineType() {
        return lineType;
    }
    public Quantity getTotalQuantity() {
        return totalQuantity;
    }

    public void cancelOrder(T order) throws IllegalArgumentException {
        // Search for the order in the line.
        for (T lineOrder : orders) {
            if (lineOrder.getId() == order.getId()) {
                // Remove the order from the line.
                orders.remove(lineOrder);
                Quantity newQuantity = new Quantity(this.getTotalQuantity().getQuantity() - order.getQuantity().getQuantity());
                this.totalQuantity = newQuantity;
                return;
            }
        }
        throw new IllegalArgumentException("Order not found in the line.");
    }

    @Override
    public String toString() {
        return String.format("Price [%s] - Size [%s] - Total [%d]\n", price.toString(), this.getTotalQuantity().toString(), this.getTotalQuantity().getQuantity() * price.getValue());
    }

    public String toStringWithOrders() {
        String lineStr = this.toString();
        lineStr += "\tOrders: -> ";
        for (T order : orders) {
            lineStr += order.toStringShort() + "\n";
        }
        return lineStr;
    }

}
