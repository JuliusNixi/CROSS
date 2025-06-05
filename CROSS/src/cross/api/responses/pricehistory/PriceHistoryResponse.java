package cross.api.responses.pricehistory;

import cross.utils.Separator;

/**
 * 
 * This class represents the price history for a specific month.
 * 
 * It contains an array of DailyPriceStats objects.
 * 
 * It's used to response to a price history client request.
 * 
 * It's used as object in the Response object.
 * 
 * @see DailyPriceStats
 * 
 * @see Separator
 * 
 * @see Response
 * 
 */
public class PriceHistoryResponse {

    private DailyPriceStats[] priceHistory;

    /**
     *
     * Constructor for the class.
     *
     */
    public PriceHistoryResponse() {

        // Initializing the price history array to an empty array.
        this.priceHistory = new DailyPriceStats[0];

    }
    
    /**
     *
     * Add a daily price stats object to the array of daily price stats.
     *
     * @param dailyPriceStats The daily price stats object to be added to the array.
     *
     * @throws NullPointerException If the daily price stats object is null.
     * @throws RuntimeException If an error occurs while copying the daily price stats array to a new daily price stats array with one more element.
     *
     */
    public void addDailyPriceStats(DailyPriceStats dailyPriceStats) throws NullPointerException, RuntimeException {

        // Null check.
        if (dailyPriceStats == null) {
            throw new NullPointerException("Daily price stats object to be added to a price history array response cannot be null.");
        }

        // Copying the daily price stats array to a new daily price stats array with one more element.
        DailyPriceStats[] newDailyPriceStats = new DailyPriceStats[priceHistory.length + 1];
        try {
            System.arraycopy(priceHistory, 0, newDailyPriceStats, 0, priceHistory.length);
        } catch (IndexOutOfBoundsException | ArrayStoreException ex) {
            throw new RuntimeException("Error while copying the daily price stats array to a new daily price stats array with one more element in a price history response.");
        }

        // Appending the new daily price stats object to the end of the new daily price stats array.
        newDailyPriceStats[newDailyPriceStats.length - 1] = dailyPriceStats;

        // Setting the new daily price stats array as the daily price stats array.
        this.priceHistory = newDailyPriceStats;

    }

    // GETTERS
    /**
     *
     * Getter for the price history field.
     *
     * @return The price history field as an array of DailyPriceStats.
     *
     */
    public DailyPriceStats[] getPriceHistory() {

        return this.priceHistory;
    }
    
    @Override
    public String toString() {

        Separator separator = new Separator("=", 6);

        String result = separator + " " + "Price History Response" + " " + separator + "\n";
        result += "Daily Price Stats Array:\n";

        for (DailyPriceStats dailyPriceStats : priceHistory) {
            result += "\t" + dailyPriceStats.toString() + "\n";
        }

        return result;

    }

}
