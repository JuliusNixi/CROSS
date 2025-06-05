package cross.api.responses.pricehistory;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;
import cross.types.price.GenericPrice;

/**
 * 
 * This class represents the daily price stats for a specific day.
 * 
 * It contains the high, low, open and close prices for the day.
 * It contains the daily date in GMT format as a String.
 * 
 * It's used in the PriceHistoryResponse class to response to a price history client request.
 * 
 * @see PriceHistoryResponse
 * 
 */
public class DailyPriceStats {

    private final String dayGMT;
    private final Integer high;
    private final Integer low;
    private final Integer open;
    private final Integer close;

    /**
     *
     * Constructor for the class.
     *
     * @param timestamp The timestamp of the daily price stats as Long.
     * @param high The high price of the daily price stats as GenericPrice.
     * @param low The low price of the daily price stats as GenericPrice.
     * @param open The open price of the daily price stats as GenericPrice.
     * @param close The close price of the daily price stats as GenericPrice.
     *
     * @throws NullPointerException If the timestamp, high, low, open or close values are null.
     * @throws IllegalArgumentException If the date format to rapresent the timestamp is invalid.
     *
     */
    public DailyPriceStats(Long timestamp, GenericPrice high, GenericPrice low, GenericPrice open, GenericPrice close) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (timestamp == null || high == null || low == null || open == null || close == null) {
            throw new NullPointerException("Timestamp, high, low, open or close values for the daily price stats cannot be null.");
        }

        // Date format.
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = null;
        try {
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'GMT'");
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid date format to rapresent the timestamp for the daily price stats.");
        }
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Format the date to a String.
        String formattedDate = sdf.format(date);
        this.dayGMT = formattedDate;

        this.open = open.getValue();
        this.high = high.getValue();
        this.close = close.getValue();
        this.low = low.getValue();

    }
    
    // GETTERS
    /**
     *
     * Getter for the daily date from GMT string to epoch milliseconds as Long.
     *
     * @return The daily date from GMT string to epoch milliseconds as Long.
     * 
     * @throws IllegalArgumentException If the date format for the daily price stats is invalid.
     * 
     */
    public Long getDayGMT() throws IllegalArgumentException {

        Instant instant;
        try {
            instant = Instant.parse(dayGMT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format for the daily price stats GMT getter.");
        }

        // Convert to epoch milliseconds.
        Long epochMillis;
        try {
            epochMillis = instant.toEpochMilli();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Invalid date format for the daily price stats GMT getter.");
        }

        return epochMillis;

    }

    /**
     *
     * Getter for the high price.
     * 
     * @return The high price as GenericPrice.
     * 
     * @throws IllegalArgumentException If the high price is invalid.
     * 
     */
    public GenericPrice getHigh() throws IllegalArgumentException {

        GenericPrice highPrice;
        try {
            highPrice = new GenericPrice(high);
        } catch (NullPointerException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid high price for the daily price stats getter.");
        }

        return highPrice;

    }

    /**
     *
     * Getter for the low price.
     * 
     * @return The low price as GenericPrice.
     * 
     * @throws IllegalArgumentException If the low price is invalid.
     * 
     */
    public GenericPrice getLow() throws IllegalArgumentException {

        GenericPrice lowPrice;
        try {
            lowPrice = new GenericPrice(low);
        } catch (NullPointerException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid low price for the daily price stats getter.");
        }

        return lowPrice;

    }

    /**
     *
     * Getter for the open price.
     * 
     * @return The open price as GenericPrice.
     * 
     * @throws IllegalArgumentException If the open price is invalid.
     * 
     */
    public GenericPrice getOpen() throws IllegalArgumentException {

        GenericPrice openPrice;
        try {
            openPrice = new GenericPrice(open);
        } catch (NullPointerException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid open price for the daily price stats getter.");
        }

        return openPrice;

    }

    /**
     *
     * Getter for the close price.
     * 
     * @return The close price as GenericPrice.
     * 
     * @throws IllegalArgumentException If the close price is invalid.
     * 
     */ 
    public GenericPrice getClose() throws IllegalArgumentException {

        GenericPrice closePrice;
        try {
            closePrice = new GenericPrice(close);
        } catch (NullPointerException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid close price for the daily price stats getter.");
        }

        return closePrice;

    }
    
    @Override
    public String toString() {

        return String.format("Daily Price Stats [Daily GMT Date [%s], High Price Value [%s], Low Price Value [%s], Open Price Value [%s], Close Price Value [%s]]", dayGMT, high, low, open, close);

    }

}

