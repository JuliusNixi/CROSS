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
    private Quantity quantity;

    // LinkedList to keep the order in the order they were added.
    // To execute the orders (matching algoritmh) we use FIFO, so i give priority
    // to insert and remove element from the head and tail.
    // Insert and execute from the head and tail is O(1). These operations should be the most frequent.
    // Cancel an order is O(n) because we need to search for the order.

    // Generic order to handle both limit and stop orders.
    private LinkedList<Order> orders;
    private SpecificPrice price;

    public OrderBookLine(SpecificPrice price) throws IllegalArgumentException {
        T order = null;
        if (order instanceof LimitOrder || order instanceof StopMarketOrder) {
            // OK.
            ;
        } else {
            throw new IllegalArgumentException("Order type not supported.");
        }
        // Empty line.
        this.quantity = new Quantity(0);
        this.orders = new LinkedList<Order>();
        this.price = price;
    }

    // Add an order to the line.
    // Stop or limit order, it doesn't matter.
    // The line is omogeneus, all orders must have the same type.
    public void addOrder(Order order) throws IllegalArgumentException {
        if (this.getClass().getTypeParameters()[0].getClass().equals(order.getClass())) {
            orders.addFirst(order);
            Quantity newQuantity = new Quantity(this.quantity.getQuantity() + order.getQuantity().getQuantity());
            this.quantity = newQuantity;
        } else {
            throw new IllegalArgumentException("Order type not match with line type.");
        }
    }

    public Quantity checkMarketOrderLineSatisfability(MarketOrder order) {
        Quantity satisfiableQuantity = new Quantity(0);

        Quantity orderQuantityCopy = new Quantity(order.getQuantity().getQuantity());
        LinkedList<Order> ordersCopy = new LinkedList<Order>(orders);
        while (orderQuantityCopy.getQuantity() > 0 && !ordersCopy.isEmpty()) {
            Order tailOrder = ordersCopy.getLast();
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
    public HashMap<Quantity, HashMap<LinkedList<Order>, Order>> executeMarketOrderOnLine(MarketOrder order) {

        HashMap<Quantity, HashMap<LinkedList<Order>, Order>> totalQuantityAndExecutedOrdesList = new HashMap<Quantity, HashMap<LinkedList<Order>, Order>>();
        Quantity executedQuantity = new Quantity(0);
        LinkedList<Order> executedOrders = new LinkedList<Order>();
        Order lastOrderExecuted = null;

        // While the order quantity is not 0 and there are orders in the line.
        while (order.getQuantity().getQuantity() > 0 && !orders.isEmpty()) {
            // Get last order in the line, the first arrived.
            Order tailOrder = orders.getLast();
            if (tailOrder.getQuantity().getQuantity() <= order.getQuantity().getQuantity()) {
                // Hitting the tail order, removing it, updating the executed quantity and the order quantity and continue hitting the next order.
                executedQuantity = new Quantity(executedQuantity.getQuantity() + tailOrder.getQuantity().getQuantity());
                order.setQuantity(new Quantity(order.getQuantity().getQuantity() - tailOrder.getQuantity().getQuantity()));
                Order o = orders.removeLast();
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

        HashMap<LinkedList<Order>, Order> executedOrdersMap = new HashMap<LinkedList<Order>, Order>();
        executedOrdersMap.put(executedOrders, lastOrderExecuted);

        totalQuantityAndExecutedOrdesList.put(executedQuantity, executedOrdersMap);

        // Updating this class quantity.
        Quantity newQuantity = new Quantity(this.quantity.getQuantity() - executedQuantity.getQuantity());
        this.quantity = newQuantity;

        return totalQuantityAndExecutedOrdesList;
    }

    public Integer getOrdersNumber() {
        return orders.size();
    }
  
    public void cancelOrder(Order order) throws IllegalArgumentException {
        // Search for the order in the line.
        for (Order lineOrder : orders) {
            if (lineOrder.getId() == order.getId()) {
                // Remove the order from the line.
                orders.remove(lineOrder);
                Quantity newQuantity = new Quantity(this.quantity.getQuantity() - order.getQuantity().getQuantity());
                this.quantity = newQuantity;
                return;
            }
        }
        throw new IllegalArgumentException("Order not found in the line.");
    }

    @Override
    public String toString() {
        return String.format("Price: [%s] - Size [%s] - Total [%d]\n", price.toString(), this.quantity.toString(), this.quantity.getQuantity() * price.getValue());
    }

    public String toStringWithOrders() {
        String lineStr = this.toString();
        lineStr += "Orders:\n";
        for (Order order : orders) {
            lineStr += order.toString() + "\n";
        }
        return lineStr;
    }

}
