package CROSS.API.Notifications;

/**
 * The Notification class is used to store a list of trades that need to be notified to the client.
 * @version 1.0
 * @see Trade
 */
public class Notification {
    
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
        String result = "";
        for (Trade trade : trades) {
            result += trade.toString() + "\n";
        }
        return result;
    }

}
