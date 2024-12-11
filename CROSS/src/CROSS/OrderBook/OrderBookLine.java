package CROSS.OrderBook;

import java.util.LinkedList;

import CROSS.Orders.Order;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;

public class OrderBookLine {
 
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

    public OrderBookLine(SpecificPrice price) {
        this.quantity = new Quantity(0);
        this.orders = new LinkedList<Order>();
        this.price = price;
    }

    public void addOrder(Order order) {
        orders.addFirst(order);
        Quantity newQuantity = new Quantity(this.quantity.getQuantity() + order.getQuantity().getQuantity());
        this.quantity = newQuantity;
    }

    // Returns the quantity of the order that was executed.
    // If the order is not fully executed, the order is updated with the remaining quantity.
    // In this case we need to go on a different price/order book line.
    public Quantity executeOrder(Order order) {

        Quantity executedQuantity = new Quantity(0);
        // While the order quantity is not 0 and there are orders in the line.
        while (order.getQuantity().getQuantity() > 0 && !orders.isEmpty()) {
            // Get last order in the line, the first arrived.
            Order tailOrder = orders.getLast();
            if (tailOrder.getQuantity().getQuantity() <= order.getQuantity().getQuantity()) {
                // Hitting the tail order, removing it, updating the executed quantity and the order quantity and continue hitting the next order.
                executedQuantity = new Quantity(executedQuantity.getQuantity() + tailOrder.getQuantity().getQuantity());
                order.setQuantity(new Quantity(order.getQuantity().getQuantity() - tailOrder.getQuantity().getQuantity()));
                orders.removeLast();
                // If order quantity is 0, we can stop the loop at the next iteration.
            } else {
                // Order satisfied by the tail order.
                executedQuantity = new Quantity(executedQuantity.getQuantity() + order.getQuantity().getQuantity());
                tailOrder.setQuantity(new Quantity(tailOrder.getQuantity().getQuantity() - order.getQuantity().getQuantity()));
                order.setQuantity(new Quantity(0));
            }
        }

        Quantity newQuantity = new Quantity(this.quantity.getQuantity() - executedQuantity.getQuantity());
        this.quantity = newQuantity;

        return executedQuantity;
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
