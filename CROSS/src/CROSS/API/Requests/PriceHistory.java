package CROSS.API.Requests;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

import CROSS.API.JSON;
import CROSS.API.Responses.ResponseCode.ResponseType;

/**
 * PriceHistory is a class used to request the price history of a market.
 * 
 * It extends the JSON class.
 * 
 * It contains a single field, month, which is a string representing the month in the format MMYYYY.
 * 
 * @version 1.0
 * @see JSON
 */
public class PriceHistory extends JSON {
    
    // FORMAT: MMYYYY
    private String month;

    /**
     * Constructor for the PriceHistory class.
     * 
     * @param month A string representing the month in the format MMYYYY.
     * @throws NullPointerException If the month is null.
     * @throws IllegalArgumentException If the month is not in the format MMYYYY.
     */
    public PriceHistory(String month) throws NullPointerException, IllegalArgumentException {
        if (month == null) {
            throw new NullPointerException("Month cannot be null.");
        }

        month = month.trim().toLowerCase();

        if (month.length() != 6) {
            throw new IllegalArgumentException("Month must be in the format MMYYYY.");
        }

        super(ResponseType.GET_PRICE_HISTORY);

        // Parsing the month string.
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("MMyyyy")
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .toFormatter();

        try {
            // Parse the string to a LocalDate, assuming the first day of the month.
            LocalDate date = LocalDate.parse(month, formatter);
            date.get(ChronoField.MONTH_OF_YEAR);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Month must be in the format MMYYYY.");
        }

        this.month = month;
    }

    /**
     * Returns the month field as a string.
     * 
     * @return The month field as a string.
     */
    public String getMonth() {
        return String.format("%s", this.month);
    }

    @Override
    public String toString() {
        return String.format("Month [%s]", this.getMonth());
    }

}
