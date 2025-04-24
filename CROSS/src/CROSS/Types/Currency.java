package CROSS.Types; 

// Not really needed since the software must handle only one pair: BTC/USD.
// But it's a good practice to have it for future implementations.
/**
 * 
 * Enum for the currency.
 * Two currencies makes a pair.
 * For example, BTC/USD.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 */
public enum Currency {
    // Some examples of CRYPTO currencies.
    BTC,
    ETH,

    USD, // Some examples of FIAT currencies.
    EUR
}
