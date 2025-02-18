package CROSS.API.Requests.PriceHistory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

/**
 * 
 * PriceHistory is a class used to request the price history of a market.
 * 
 * It contains a single field, month, which is a string representing the month in the format MMYYYY.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 */
public class PriceHistory {
    
    // FORMAT: MMYYYY
    private final String month;

    /**
     * 
     * Constructor for the class.
     * 
     * @param month A string representing the month in the format MMYYYY.
     * 
     * @throws NullPointerException If the month is null.
     * @throws IllegalArgumentException If the month is not in the format MMYYYY.
     * 
     */
    public PriceHistory(String month) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (month == null) {
            throw new NullPointerException("Month in price history request cannot be null.");
        }

        month = month.trim().toLowerCase();

        // Length check.
        if (month.length() != 6) {
            throw new IllegalArgumentException("Month in price history request must be in the format MMYYYY.");
        }

        // Parsing the month string.
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("MMyyyy")
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .toFormatter();

        try {
            // Parse the string to a LocalDate, assuming the first day of the month.
            LocalDate date = LocalDate.parse(month, formatter);
            date.get(ChronoField.MONTH_OF_YEAR);
        } catch (DateTimeParseException | ArithmeticException ex) {
            throw new IllegalArgumentException("Month in price history request must be in the format MMYYYY.");
        }

        this.month = month;

    }

    // GETTERS
    /**
     * 
     * Returns the month field as a string.
     * 
     * @return The month field as a string.
     * 
     */
    public String getMonth() {

        return String.format("%s", this.month);

    }

}
