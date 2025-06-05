package cross.api.notifications;

import cross.api.requests.orders.CreateRequest;
import cross.orders.MarketOrder;
import cross.orders.Order;
import cross.orders.OrderType;
import cross.users.User;

/**
 *
 * This class is used in the notification system to represent a trade.
 *
 * It extends the CreateRequest order class.
 * It's used the CreateRequest order class because it's the one with the most information (size, type, price) about an order and has the more generic constructor.
 *
 * This class adds an order id, an order type, and a timestamp.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see cross.api.requests.orders.CreateRequest
 *
 * @see cross.orders.Order
 * 
 * @see cross.orders.OrderType
 *
 */
public class Trade extends CreateRequest {

    // Generic number to handle the Long positive order's id, but also the Integer -1 for errors.
    private final Number orderId;
    private final String orderType;
    private final Long timestamp;
    private final transient User user;

    /**
     *
     * Constructor for the class.
     *
     * @param order The order that was traded.
     *
     * @throws NullPointerException If the order is null.
     * @throws RuntimeException If there is an error while setting the timestamp.
     *
     */
    public Trade(Order order) throws NullPointerException, RuntimeException {

        super(order);

        // Setting the order id.
        Number orderId = order.getId();

        // Setting the order type.
        String orderType  = order.getOrderType().name().toLowerCase();

        if (orderType.compareTo("market") == 0 && order instanceof MarketOrder) {
            super.price = ((MarketOrder) order).getExecutionPrice().getValue();
            if (((MarketOrder) order).getComingFromStopOrderId() != null) {
                orderType = "stop";
                orderId = ((MarketOrder) order).getComingFromStopOrderId();
            }
        }
        this.orderType = orderType;
        this.orderId = orderId;

        // Setting the user.
        this.user = order.getUser();

        this.timestamp = order.getTimestamp();

    }
    
    // GETTERS
    /**
     *
     * Gets the order's id.
     *
     * @return The order's id as Number.
     *
     */
    public Number getOrderId() {

        return this.orderId;

    }
    /**
     *
     * Gets the user that made the order.
     *
     * @return The user that made the order as User.
     *
     */
    public User getUser() {

        return this.user;

    }
    /**
     *
     * Gets the order type as a OrderType enum.
     *
     * @return The order type as OrderType enum.
     *
     */
    public OrderType getOrderType() {

        return OrderType.valueOf(this.orderType.toUpperCase());

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

        String superString = super.toString();
        superString = superString.replace(CreateRequest.class.getSimpleName(), "").trim();
        superString = superString.substring(1, superString.length() - 1);
        return String.format("Trade [%s - Order ID [%s] - Order Type [%s] - Timestamp [%s]]", superString, this.orderId, this.orderType, this.timestamp);
    
    }

}
