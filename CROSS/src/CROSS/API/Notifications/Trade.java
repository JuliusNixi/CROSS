package CROSS.API.Notifications;

import java.time.Instant;

/**
 * This class is used in the notification system to represent a trade.
 * 
 * It extends the Limit class, attention the Limit class used here is from API and rapresent a request.
 * It's used the Limit class because it's the one with the most information about the order.
 * 
 * This class adds an order id, order type, and timestamp.
 * @version 1.0
 * @see CROSS.API.Requests.Orders.Limit
 * @see CROSS.Orders.Order
 */
public class Trade extends CROSS.API.Requests.Orders.Limit {

    private Integer orderId;
    private String orderType;
    private Long timestamp;

    /**
     * Constructor for the Trade class.
     * 
     * @param order The order that was traded.
     */
    public <O extends CROSS.Orders.Order> Trade(O order) {

        super(order);

        this.orderId = order.getId();

        // Removing the "Order" part of the class name and converting to lowercase.
        String orderType = order.getClass().getName().replace("Order", "").toLowerCase();
        orderType = orderType.contains("stop") ? "stop" : orderType;
        this.orderType = orderType;

        // Setting the timestamp to the current time.
        this.timestamp = Long.valueOf(Instant.now().toEpochMilli());
        
    }

    // GETTERS
    /**
     * Gets the order id.
     * 
     * @return The order id.
     */
    public Integer getOrderId() {
        return Integer.valueOf(this.orderId);
    }
    /**
     * Gets the order type as a String.
     * 
     * @return The order type as String.
     */
    public String getOrderType() {
        return String.format("%s", this.orderType);
    }
    /**
     * Gets the timestamp.
     * 
     * @return The timestamp.
     */
    public Long getTimestamp() {
        return Long.valueOf(this.timestamp);
    }

    @Override
    public String toString() {
        return String.format("Order ID [%s] - Order Type [%s] - Timestamp [%s] - %s", this.getOrderId(), this.getOrderType(), this.getTimestamp(), super.toString());
    }

}
