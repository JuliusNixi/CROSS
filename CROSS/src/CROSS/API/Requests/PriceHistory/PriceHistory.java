package CROSS.API.Requests.PriceHistory;

import CROSS.Client.ClientActionsUtils;

/**
 * 
 * PriceHistory is a class used to request the price history of a market.
 * 
 * It contains a single field, month, which is a string representing the month in the format MMYYYY.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see ClientActionsUtils
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

        month = month.trim();

        try {
            ClientActionsUtils.parseMonthFromString(month);
        }catch (Exception ex) {
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

        return this.month;

    }

}
