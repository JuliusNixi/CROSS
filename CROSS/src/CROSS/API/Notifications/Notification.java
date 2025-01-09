package CROSS.API.Notifications;

import CROSS.Utils.Separator;

/**
 * The Notification class is used to store a list of trades that need to be notified to the client.
 * It's extend the JSON class to be able to serialize the object to JSON.
 * @version 1.0
 * @see Trade
 * @see CROSS.API.Responses.Notify
 * @see JSON
 */
public class Notification extends CROSS.API.JSON {
    
    // Simple array of trades to be notified to avoid problems with JSON serialization.
    Trade[] trades;

    /**
     * Constructor for the Notification class.
     */
    public Notification() {
        this.trades = new Trade[0];
    }

    /**
     * Add a trade to the list of trades to be notified.
     * @param trade The trade to be added.
     * @throws NullPointerException If the trade is null.
     */
    public void addTrade(Trade trade) throws NullPointerException {
        if (trade == null) {
            throw new NullPointerException("Trade cannot be null.");
        }
        
        // Copying the trades array to a new array with one more element.
        Trade[] newTrades = new Trade[trades.length + 1];
        for (int i = 0; i < trades.length; i++) {
            newTrades[i] = trades[i];
        }
        // Appending the new trade to the end of the new array.
        newTrades[trades.length] = trade;
        trades = newTrades;
    }

    @Override
    public String toString() {
        Separator separator = new Separator("=", 5);
        String result = separator + " " + "Notification" + " " + separator + "\n";
        result += "Trades:\n";
        for (Trade trade : trades) {
            result += "\t" + trade.toString() + "\n";
        }
        return result;
    }

}
