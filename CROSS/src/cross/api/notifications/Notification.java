package cross.api.notifications;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import cross.api.JSONAPIMessage;
import cross.orders.LimitOrder;
import cross.orders.MarketOrder;
import cross.orders.OrderType;
import cross.orders.StopOrder;
import cross.types.Currency;
import cross.types.Quantity;
import cross.types.price.GenericPrice;
import cross.types.price.PriceType;
import cross.types.price.SpecificPrice;
import cross.utils.ClientActionsUtils;
import cross.utils.Separator;

/**
 *
 * The Notification class is used to store an array of trades of the Trade class that need to be notified to the client.
 *
 * It extends the JSONAPIMessage class to use the toJSONString conversion method.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Trade
 *
 * @see JSONAPIMessage
 *
 * @see Separator
 *
 */
public class Notification extends JSONAPIMessage {

    // The string field as described in the assignment's text with the name and value.
    private final static String notification = "closedTrades";
    // A simple array of trades to be notified to the client to avoid problems with JSON serialization (if using different objects from the array).
    private Trade[] trades;

    /**
     *
     * Constructor for the class.
     *
     */
    public Notification() {

        // Initializing the trades array to an empty array.
        this.trades = new Trade[0];

    }
    
    /**
     *
     * Alternative constructor of the class.
     * 
     * Used in the NotificationsThread class to parse the notification object from a JSON string.
     *
     * @param JSONnotification The JSON notification string.
     *
     * @throws NullPointerException If the JSON notification string is null.
     * @throws IllegalArgumentException If the JSON string is not a valid JSON object or if the JSON string content is not valid or the whole notification is not valid / recognized.
     *
     */
    public Notification(String JSONnotification) throws NullPointerException, IllegalArgumentException {

        this();

        // Null check.
        if (JSONnotification == null)
            throw new NullPointerException("JSON notification string in the notification cannot be null.");

        Currency defaultPrimaryCurrency = Currency.getDefaultPrimaryCurrency();
        Currency defaultSecondaryCurrency = Currency.getDefaultSecondaryCurrency();
        JsonObject jsonObject = null;
        try {
            jsonObject = JsonParser.parseString(JSONnotification).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException ex) {
            throw new IllegalArgumentException("JSON notification string in the notification is not a valid JSON object.");
        }
        try {
            JsonArray tradeArray = jsonObject.get("trades").getAsJsonArray();

            for (int i = 0; i < tradeArray.size(); i++) {
                JsonObject tradeObject = tradeArray.get(i).getAsJsonObject();

                String tradeOrderId = tradeObject.get("orderId").getAsString();
                Number orderIdNumber = ClientActionsUtils.getOrderIDFromString(tradeOrderId);

                String tradeOrderType = tradeObject.get("orderType").getAsString();
                OrderType orderType = ClientActionsUtils.getOrderTypeFromString(tradeOrderType);

                String tradePriceType = tradeObject.get("type").getAsString();
                PriceType priceType = ClientActionsUtils.getPriceTypeFromString(tradePriceType);

                String tradePrice = tradeObject.get("price").getAsString();
                GenericPrice genericPrice = ClientActionsUtils.getPriceFromString(tradePrice);
                
                String tradeQuantity = tradeObject.get("size").getAsString();
                Quantity quantity = ClientActionsUtils.getSizeFromString(tradeQuantity);

                JsonElement timestampJson = tradeObject.get("timestamp");
                Long timestamp = null;
                if (timestampJson != null)
                    timestamp = timestampJson.getAsLong();

                SpecificPrice specificPrice = new SpecificPrice(genericPrice.getValue(), priceType, defaultPrimaryCurrency, defaultSecondaryCurrency);

                Trade trade = null;
                switch (orderType) {
                    case LIMIT -> {
                        LimitOrder limitOrder = new LimitOrder(specificPrice, quantity, true);
                        limitOrder.setId(orderIdNumber.longValue());
                        if (timestamp != null) limitOrder.setTimestamp(timestamp);
                        trade = new Trade(limitOrder);
                    }
                    case MARKET -> {
                        MarketOrder marketOrder = new MarketOrder(priceType, defaultPrimaryCurrency, defaultSecondaryCurrency, quantity);
                        marketOrder.setId(orderIdNumber.longValue());
                        if (timestamp != null) marketOrder.setTimestamp(timestamp);
                        marketOrder.setExecutionPrice(specificPrice);
                        trade = new Trade(marketOrder);
                    }
                    case STOP -> {
                        StopOrder stopOrder = new StopOrder(specificPrice, quantity, true);
                        stopOrder.setId(orderIdNumber.longValue());
                        if (timestamp != null) stopOrder.setTimestamp(timestamp);
                        trade = new Trade(stopOrder);
                    }
                }

                this.addTrade(trade);

            }

        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException("JSON notification string in the notification is not valid.");
        }

    }


    /**
     *
     * Add a trade to the array of trades to be notified to the client.
     *
     * @param trade The trade to be added to the notification array.
     *
     * @throws NullPointerException If the trade is null.
     * @throws RuntimeException If an error occurs while copying the trades array to a new trades array with one more element.
     *
     */
    public void addTrade(Trade trade) throws NullPointerException, RuntimeException {

        // Null check.
        if (trade == null) {
            throw new NullPointerException("Trade to be added to a notification cannot be null.");
        }

        // Copying the trades array to a new trades array with one more element.
        Trade[] newTrades = new Trade[trades.length + 1];
        try {
            System.arraycopy(trades, 0, newTrades, 0, trades.length);
        } catch (IndexOutOfBoundsException | ArrayStoreException ex) {
            throw new RuntimeException("Error while copying the trades array to a new trades array with one more element in a notification.");
        }

        // Appending the new trade to the end of the new trades array.
        newTrades[newTrades.length - 1] = trade;

        // Setting the new trades array as the trades array.
        this.trades = newTrades;

    }

    // GETTERS
    /**
     *
     * Getter for the notification field.
     *
     * @return The notification field as a String.
     *
     */
    public static String getNotificationField() {

        return String.format("%s", notification);

    }
    /**
     *
     * Getter for the trades field.
     *
     * @return The trades field as an array of Trade.
     *
     */
    public Trade[] getTrades() {

        return this.trades;
    }
    

    public String toString(Long mainOrderId) {

        Separator separator = new Separator("=", 6);

        String result = separator + " " + "Notification About Orders" + " " + separator + "\n";
        result += "Trades:\n";

        for (Trade trade : trades) {
            String line = null;
            if (mainOrderId != null && trade.getOrderId().longValue() == mainOrderId.longValue()) {
                line = "\t" + "YOUR MAIN ORDER: " + trade.toString() + "\n";
            }else{
                line = "\t" + "MATCHED WITH: " + trade.toString() + "\n";
            }
            if (trade.getOrderId().intValue() == -1)
                line = "\t" + "NOT EXECUTED ORDER: " + trade.toString() + "\n";
            result += line;
        }

        return result;

    }

}
