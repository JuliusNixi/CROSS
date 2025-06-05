package cross.api.requests.pricehistory;

import cross.utils.ClientActionsUtils;

/**
 *
 * PriceHistoryRequest is a class used to API request from the client the price history of the market.
 * 
 * It's used as values in the Request object.
 *
 * It contains a single field, month, which is a string representing the month in the format MMYYYY.
 * 
 * This must be valid and so it's parsed by the ClientActionsUtils class in the constructor.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see cross.api.utils.ClientActionsUtils
 * 
 * @see Request
 *
 */
public class PriceHistoryRequest {
    
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
    public PriceHistoryRequest(String month) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (month == null) {
            throw new NullPointerException("Month in price history request cannot be null.");
        }

        month = month.trim();

        try {
            ClientActionsUtils.parseMonthFromString(month);
        }catch (IllegalArgumentException ex) {
            // Forward the exception's message.
            throw new IllegalArgumentException(ex.getMessage());
        }

        this.month = month;

    }

    // GETTERS
    /**
     *
     * Returns the month field as a string in the format MMYYYY.
     *
     * @return The month field as a string in the format MMYYYY.
     *
     */
    public String getMonth() {

        return String.format("%s", this.month);

    }

}
