package CROSS.OrderBook;

import CROSS.Types.Currency;
import CROSS.Types.Price.GenericPrice;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;

/**
 * 
 * This class represents a market in the system. 
 * 
 * A market is defined by its primary and secondary currencies, the actuals ask and bid prices and the increment of the price between two consecutive prices.
 * The actuals ask and bid prices are the prices at which the market is currently trading, so they are the best ask and the best bid prices.
 * 
 * The market is used to create orders to trade currencies using the order book of the market.
 * 
 * This class is extended by the OrderBook class.
 * 
 * Each price has an associated market, so the market is also used to create prices.
 * 
 * The project assignment specifies that there is only one market in the system.
 * This class (and the whole project) has been implemented to stay generic and to allow the creation of multiple markets.
 * So the class has a static field to store the main market of the system that will be used in the project as the default one.
 * 
 * It implements the Comparable interface to allow to check the equality of two markets.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see OrderBook
 * 
 * @see Currency
 * @see SpecificPrice
 * @see GenericPrice
 * 
 * @see Comparable
 * 
 */
public class Market implements Comparable<Market> {

    // The primary and secondary currencies of the market.
    private final Currency primary_currency;
    private final Currency secondary_currency;

    // The actuals (best) ask and bid prices of the market.
    // GENERIC PRICES AND NOT SPECIFIC ONES TO AVOID RECURSION PROBLEM IN TO STRING JSON ORDERS CONVERSION:
    // ORDER SPECIFICPRICE -> MARKET OF THE PRICE -> BEST PRICES OF THE MARKET -> MARKET OF THE PRICE -> ...
    private GenericPrice actualPriceAsk = null;
    private GenericPrice actualPriceBid = null;

    // Increment of the price between two consecutive prices.
    // So, for example, if the increment is 1, the prices could be 1, 2, 3, 4, 5, ...
    private final GenericPrice increment;

    // The main market of the system.
    private static Market mainMarket = null;
    
    /**
     * 
     * Constructor of the class.
     * 
     * Creates a new market with the given primary and secondary currencies and the increment of the price between two consecutive prices.
     * 
     * The actuals (best) market prices COULD BE NULL at the beginning, since they are updated by the order book of the market.
     * 
     * @param primary_currency The primary currency of the market.
     * @param secondary_currency The secondary currency of the market.
     * @param increment The increment of the price between two consecutive prices.
     * 
     * @throws IllegalArgumentException If the primary and secondary currencies are the same.
     * @throws NullPointerException If the primary currency, secondary currency or increment are null.
     * 
     */
    public Market(Currency primary_currency, Currency secondary_currency, GenericPrice increment) throws IllegalArgumentException, NullPointerException {
        
        // No synchronization needed for the increment, it has no setters.

        // Null checks.
        if (primary_currency == null)
            throw new NullPointerException("The primary currency of a market cannot be null.");
        if (secondary_currency == null)
            throw new NullPointerException("The secondary currency of a market cannot be null.");
        if (increment == null)
            throw new NullPointerException("The price increment of a market cannot be null.");

        // The primary and secondary currencies of a market cannot be the same.
        if (primary_currency == secondary_currency)
            throw new IllegalArgumentException("The primary and secondary currencies of a market cannot be the same.");

        this.primary_currency = primary_currency;
        this.secondary_currency = secondary_currency;
        // Increment has no setters, no synchronization needed.
        this.increment = increment;

    }
    
    // GETTERS
    /**
     * 
     * Returns the primary currency of the market.
     * 
     * @return The primary currency of the market as a Currency object.
     * 
     */
    public Currency getPrimaryCurrency() {
        
        return primary_currency;

    }
    /**
     * 
     * Returns the secondary currency of the market.
     * 
     * @return The secondary currency of the market as a Currency object.
     * 
     */
    public Currency getSecondaryCurrency() {

        return secondary_currency;

    }
    /**
     * 
     * Returns the actual (best) ask price of the market.
     * 
     * @return The actual ask price of the market as a SpecificPrice object or null if not set.
     * 
     */
    public SpecificPrice getActualPriceAsk() {
        
        // Null check.
        if (this.actualPriceAsk == null)
            return null;

        return new SpecificPrice(this.actualPriceAsk.getValue(), PriceType.ASK, this);

    }
    /**
     * 
     * Returns the actual (best) bid price of the market.
     * 
     * @return The actual bid price of the market as a SpecificPrice object or null if not set.
     * 
     */
    public SpecificPrice getActualPriceBid() {

        // Null check.
        if (this.actualPriceBid == null)
            return null;

        return new SpecificPrice(this.actualPriceBid.getValue(), PriceType.BID, this);

    }
    /**
     * 
     * Returns the increment of the price between two consecutive prices.
     * 
     * @return The increment of the price between two consecutive prices as a GenericPrice object.
     * 
     */
    public GenericPrice getIncrement() {

        return this.increment;

    }
    /**
     * 
     * Returns the main market of the system.
     * 
     * @return The main market of the system as a Market object.
     * 
     * @throws RuntimeException If the main market of the system is null.
     * 
     */
    public static Market getMainMarket() throws RuntimeException {

        // Null check.
        if (Market.mainMarket == null)
            throw new RuntimeException("The main market of the system to get is null. Use setMainMarket() to set it.");

        return Market.mainMarket;


    }

    // SETTERS
    /**
     * 
     * Sets the actuals (best) ask and bid prices of the market.
     * 
     * Synchronized method to avoid multiple threads to set the actuals prices at the same time.
     * 
     * One (of the two) price could be null, since they are updated by the order book of the market.
     * Both cannot be null at the same time.
     * 
     * @param actualPriceAskP The new actual (best) ask price of the market.
     * @param actualPriceBidP The new actual (best) bid price of the market.
     * 
     * @throws IllegalArgumentException If the given price is not an ask price. If the given price is not a bid price. If the ask price is higher than the bid price. If the market of the prices to set does not match with the market.
     * @throws NullPointerException If the given prices to set are BOTH null.
     * 
     */
    public synchronized void setActualPrices(SpecificPrice actualPriceAskP, SpecificPrice actualPriceBidP) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (actualPriceAskP == null && actualPriceBidP == null)
            throw new NullPointerException("The actual (best) prices of a market to set cannot be null.");

        // Prices cannot change, have no setters, no synchronization needed.

        // The actual price ask must be an ask price.
        if (actualPriceAskP != null && actualPriceAskP.getType() != PriceType.ASK)
            throw new IllegalArgumentException("The actual (best) price ask to set must be an ask price.");
        // The actual price bid must be a bid price.
        if (actualPriceBidP != null && actualPriceBidP.getType() != PriceType.BID)
            throw new IllegalArgumentException("The actual (best) price bid to set must be a bid price.");
        
        // The actual ask price must be GREATER than the actual bid price.
        if (actualPriceAskP != null && actualPriceBidP != null && actualPriceAskP.getValue() < actualPriceBidP.getValue())
            throw new IllegalArgumentException("The actual (best) price ask to set must be GREATER than the actual (best) price bid.");

        // Prices' market / this market check.
        if ((actualPriceAskP != null && actualPriceAskP.getMarket().compareTo(this) != 0) || (actualPriceBidP != null && actualPriceBidP.getMarket().compareTo(this) != 0))
            throw new IllegalArgumentException("The market of the actual (best) prices to set must match with the market.");

        // Conversion to the generic prices.
        GenericPrice genericActualPriceAsk = null;
        GenericPrice genericActualPriceBid = null;

        if (actualPriceAskP != null)
            genericActualPriceAsk = new GenericPrice(actualPriceAskP.getValue());
        if (actualPriceBidP != null)
            genericActualPriceBid = new GenericPrice(actualPriceBidP.getValue());

        this.actualPriceAsk = genericActualPriceAsk;
        this.actualPriceBid = genericActualPriceBid;

        // Updating the main market prices if needed to prevent problems with the main market order book.
        if (Market.mainMarket != null && Market.mainMarket.compareTo(this) == 0) {
            Market.mainMarket.actualPriceAsk = this.actualPriceAsk;
            Market.mainMarket.actualPriceBid = this.actualPriceBid;
        }

    }
    /**
     * 
     * Sets the main market of the system.
     * 
     * Synchronized ON CLASS method to avoid multiple threads to set the main market at the same time.
     * Synchronized ON MARKET method to avoid changes in the market during the set.
     * 
     * @param mainMarket The new main market of the system to set.
     * 
     * @throws NullPointerException If the given market to set as main is null.
     * 
     */
    public static void setMainMarket(Market mainMarket) throws NullPointerException {

        synchronized (Market.class) {

            // Null check.
            if (mainMarket == null)
                throw new NullPointerException("The main market of the system to set cannot be null.");

            synchronized (mainMarket) {

                Market.mainMarket = mainMarket;

            }

        }

    }

    @Override
    public synchronized String toString() {

        String bestAsk = this.getActualPriceAsk() == null ? "null" : this.getActualPriceAsk().toStringWithoutMarket();
        String bestBid = this.getActualPriceBid() == null ? "null" : this.getActualPriceBid().toStringWithoutMarket();
        
        return String.format("Pair [%s/%s] - Actual Ask [%s] - Actual Bid [%s] - Price Increment [%s]", this.getPrimaryCurrency().name(), this.getSecondaryCurrency().name(), bestAsk, bestBid, this.getIncrement().toString());

    }

    @Override
    public int compareTo(Market otherMarket) throws NullPointerException, IllegalArgumentException {
        
        // No synchronization needed, no changes (no setters) IN THESE (CURRENCIES) market's properties.

        // Null check.
        if (otherMarket == null)
            throw new NullPointerException("The market to compare to cannot be null.");
        
        // Equal markets.
        if (this.getPrimaryCurrency() == otherMarket.getPrimaryCurrency() && this.getSecondaryCurrency() == otherMarket.getSecondaryCurrency())
            return 0;

        throw new IllegalArgumentException("Cannot compare different markets.");

    }
 
}
