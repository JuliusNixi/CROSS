package cross.types;

// Not really needed since the software must handle only (as described in the assignment) one pair: BTC/USD.
// But it's a good practice to have it for future implementations and for more clarity.

/**
 *
 * Enum for the currency.
 * 
 * Two currencies make a pair.
 * For example, BTC/USD is a pair.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 */
public enum Currency {
    
    // Some examples of CRYPTO currencies.
    BTC,
    ETH,

    // Some examples of FIAT currencies.
    USD,
    EUR;

    // GETTERS
    /**
     * 
     * Returns the default primary currency.
     * 
     * @return The default primary currency.
     * 
     */
    public static Currency getDefaultPrimaryCurrency(){
        return BTC;
    }

    /** 
     * 
     * Returns the default secondary currency.
     * 
     * @return The default secondary currency.
     * 
     */
    public static Currency getDefaultSecondaryCurrency(){
        return USD;
    }

}

