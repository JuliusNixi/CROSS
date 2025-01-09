package CROSS.API.Responses;

import CROSS.API.Notifications.Notification;

/**
 * Represents a notification sent by the server to the client about a certain order.
 * @version 1.0
 * @see CROSS.API.Notifications.Trade
 * @see CROSS.Orders.Order
 */
public class NotificationResponse extends Notification {

    /**
     * Constructor for the NotificationResponse class.
     */
    public NotificationResponse() {
        super();
    }

}
