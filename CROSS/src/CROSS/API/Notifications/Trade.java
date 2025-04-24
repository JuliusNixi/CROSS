package CROSS.API.Notifications;

import java.time.Instant;
import CROSS.Orders.Order;
import CROSS.Types.Price.GenericPrice;

/**
 * 
 * This class is used in the notification system to represent a trade.
 * 
 * It extends the Generic order class.
 * It's used the Generic order class because it's the one with the most information (size and type) about an order and has the more generic constructor.
 * 
 * This class adds a price, an order id, order type, and timestamp.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see CROSS.API.Requests.Orders.Generic
 * 
 * @see CROSS.Orders.Order
 * 
 * @see GenericPrice
 * 
 */
public class Trade extends CROSS.API.Requests.Orders.Generic {

    private final Integer price;
    private final Integer orderId;
    private final String orderType;
    private final Long timestamp;

    /**
     * 
     * Constructor for the class.
     * 
     * @param <GenericOrder> A generic order. Not used the generic Order class object since I need to know the type of the order, so keep its class and used a generic type.
     * @param order The order that was traded.
     * 
     * @throws NullPointerException If the order is null.
     * @throws RuntimeException If there is an error while setting the timestamp.
     * 
     */
    public <GenericOrder extends CROSS.Orders.Order> Trade(GenericOrder order) throws NullPointerException, RuntimeException {

        super((Order) order);

        // Setting the price of the trade.
        this.price = order.getPrice().getValue();

        // Setting the order id.
        this.orderId = order.getId();

        // Removing the "Order" part of the class name and converting to lowercase.
        String orderType = order.getClass().getName().replace("Order", "").toLowerCase();
        orderType = orderType.contains("stop") ? "stop" : orderType;
        this.orderType = orderType;

        // Setting the timestamp to the current time.
        try {
            this.timestamp = Long.valueOf(Instant.now().toEpochMilli());
        } catch (ArithmeticException ex) {
            throw new RuntimeException("Error while setting the timestamp for the trade.");
        }
        
    }

    // GETTERS
    /**
     * 
     * Gets the price of the trade.
     * 
     * @return The price of the trade as GenericPrice object.
     * 
     */
    public GenericPrice getPrice() {

        return new GenericPrice(this.price);

    }
    /**
     * 
     * Gets the order's id.
     * 
     * @return The order's id as Integer.
     * 
     */
    public Integer getOrderId() {

        return this.orderId;

    }
    /**
     * 
     * Gets the order type as a String.
     * 
     * @return The order type as String.
     * 
     */
    public String getOrderType() {

        return this.orderType;

    }
    /**
     * 
     * Gets the timestamp.
     * 
     * @return The timestamp as Long.
     * 
     */
    public Long getTimestamp() {

        return this.timestamp;

    }

    @Override
    public String toString() {

        return "Trade [Price [" + price + "] - Order ID [" + orderId + "] - Order Type [" + orderType + "] - Timestamp [" + timestamp + "]]";

    }

}
