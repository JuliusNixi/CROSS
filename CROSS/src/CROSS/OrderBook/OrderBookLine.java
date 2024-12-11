package CROSS.OrderBook;

import java.util.LinkedList;

import CROSS.Orders.LimitOrder;
import CROSS.Types.Quantity;

public class OrderBookLine {
 
    private Quantity quantity;
    private LinkedList<LimitOrder> orders;

    public OrderBookLine() {
        this.quantity = new Quantity(0);
        orders = new LinkedList<LimitOrder>();
    }
    public void addOrder(LimitOrder order) {
        orders.add(order);
        //quantity.add(order.getQuantity());
    }

}
