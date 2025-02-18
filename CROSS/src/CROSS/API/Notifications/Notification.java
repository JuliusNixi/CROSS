package CROSS.API.Notifications;

import CROSS.API.JSON;
import CROSS.Utils.Separator;

/**
 * 
 * The Notification class is used to store an array of trades of the Trade class that need to be notified to the client.
 * 
 * It extends the JSON class to use the JSON string conversion method.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Trade
 * 
 * @see JSON
 * 
 * @see Separator
 * 
 */
public class Notification extends JSON {
    
    // The string field as described in the assignment.
    private final static String notification = "closedTrades";
    // Simple array of trades to be notified to avoid problems with JSON serialization.
    private Trade[] trades;

    /**
     * Constructor for the class.
     */
    public Notification() {

        this.trades = new Trade[0];

    }

    /**
     * 
     * Add a trade to the array of trades to be notified.
     * 
     * @param trade The trade to be added.
     * 
     * @throws NullPointerException If the trade is null.
     * 
     */
    public void addTrade(Trade trade) throws NullPointerException {

        // Null check.
        if (trade == null) {
            throw new NullPointerException("Trade to be added to a notification cannot be null.");
        }
        
        // Copying the trades array to a new array with one more element.
        Trade[] newTrades = new Trade[trades.length + 1];
        for (int i = 0; i < trades.length; i++) {
            newTrades[i] = trades[i];
        }

        // Appending the new trade to the end of the new array.
        newTrades[trades.length] = trade;

        // Setting the new array as the trades array.
        this.trades = newTrades;

    }

    /**
     * 
     * Getter for the notification field.
     * 
     * @return The notification field as a String.
     * 
     */
    public String getNotificationField() {
        return notification;
    }

    @Override
    public String toString() {

        Separator separator = new Separator("=", 6);

        String result = separator + " " + "Notification" + " " + separator + "\n";
        result += "Trades:\n";

        for (Trade trade : trades) {
            result += "\t" + trade.toString() + "\n";
        }

        return result;

    }

}
