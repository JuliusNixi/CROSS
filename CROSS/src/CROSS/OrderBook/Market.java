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
 * So the class has a static field to store the main market of the system that will be used in the project.
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
    // ORDER HAS ITS SPECIFICPRICE, THIS SPECIFICPRICE HAS ITS MARKET, BUT THIS MARKET HAS AS SPECIFICPRICES ITS BEST PRICES, AND THE SPECIFICPRICES BEST PRICES HAVE AS A MARKET THE PREVIOUS, ...
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
     * Creates a new market with the given primary and secondary currencies, the actual ask and bid prices and the increment of the price between two consecutive prices.
     * 
     * The actual ask and bid prices are the prices at which the market is currently trading.
     * 
     * @param primary_currency The primary currency of the market.
     * @param secondary_currency The secondary currency of the market.
     * @param actualPriceAsk The actual (best) ask price of the market.
     * @param actualPriceBid The actual (best) bid price of the market.
     * @param increment The increment of the price between two consecutive prices.
     * 
     * @throws IllegalArgumentException If the primary and secondary currencies are the same, if the actual ask price is higher than the actual bid price.
     * @throws NullPointerException If the primary currency, secondary currency, actual prices or increment are null.
     * 
     */
    public Market(Currency primary_currency, Currency secondary_currency, GenericPrice actualPriceAsk, GenericPrice actualPriceBid, GenericPrice increment) throws IllegalArgumentException, NullPointerException {
        
        // Null checks.
        if (primary_currency == null)
            throw new NullPointerException("The primary currency of a market cannot be null.");
        if (secondary_currency == null)
            throw new NullPointerException("The secondary currency of a market cannot be null.");
        if (increment == null)
            throw new NullPointerException("The price increment of a market cannot be null.");
        if (actualPriceAsk == null || actualPriceBid == null)
            throw new NullPointerException("The actual prices of a market cannot be null.");

        // The primary and secondary currencies of a market cannot be the same.
        if (primary_currency == secondary_currency)
            throw new IllegalArgumentException("The primary and secondary currencies of a market cannot be the same.");

        // The actual ask price must be lower than the actual bid price.
        if (actualPriceAsk.getValue() > actualPriceBid.getValue())
            throw new IllegalArgumentException("The actual price ask must be lower or equal than the actual price bid.");

        this.primary_currency = primary_currency;
        this.secondary_currency = secondary_currency;

        // This price is intended to be the actual price of the market.
        this.actualPriceAsk = actualPriceAsk;
        this.actualPriceBid = actualPriceBid;

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
     * @return The actual ask price of the market as a SpecificPrice object.
     * 
     */
    public SpecificPrice getActualPriceAsk() {

        return new SpecificPrice(this.actualPriceAsk.getValue(), PriceType.ASK, Market.copyMarket(this));

    }
    /**
     * 
     * Returns the actual (best) bid price of the market.
     * 
     * @return The actual bid price of the market as a SpecificPrice object.
     * 
     */
    public SpecificPrice getActualPriceBid() {

        return new SpecificPrice(this.actualPriceBid.getValue(), PriceType.BID, Market.copyMarket(this));

    }
    /**
     * 
     * Returns the increment of the price between two consecutive prices.
     * 
     * @return The increment of the price between two consecutive prices as a GenericPrice object.
     * 
     */
    public GenericPrice getIncrement() {

        return new GenericPrice(this.increment.getValue());

    }
    /**
     * 
     * Returns the main market of the system.
     * 
     * @return A copy of the main market of the system as a Market object.
     * 
     * @throws RuntimeException If the main market of the system is null.
     * 
     */
    public static Market getMainMarket() throws RuntimeException {

        // Null check.
        if (Market.mainMarket == null)
            throw new RuntimeException("The main market of the system is null.");

        return Market.copyMarket(Market.mainMarket);

    }

    // SETTERS
    /**
     * 
     * Sets the actuals (best) ask and bid prices of the market.
     * 
     * Synchronized method to avoid multiple threads to set the actuals prices at the same time.
     * 
     * @param actualPriceAsk The new actual (best) ask price of the market.
     * @param actualPriceBid The new actual (best) bid price of the market.
     * 
     * @throws NullPointerException If the given prices are, at least one, null.
     * @throws IllegalArgumentException If the given price is not an ask price. If the given price is not a bid price.
     * 
     */
    public synchronized void setActualPrices(SpecificPrice actualPriceAsk, SpecificPrice actualPriceBid) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (actualPriceAsk == null || actualPriceBid == null)
            throw new NullPointerException("The actual prices of a market to set cannot be null.");

        // The actual price ask must be an ask price.
        if (actualPriceAsk.getType() != PriceType.ASK)
            throw new IllegalArgumentException("The actual price ask to set must be an ask price.");
        // The actual price bid must be a bid price.
        if (actualPriceBid.getType() != PriceType.BID)
            throw new IllegalArgumentException("The actual price bid to set must be a bid price.");
        
        // The actual ask price must be lower than the actual bid price.
        if (actualPriceAsk.getValue() > this.actualPriceBid.getValue())
            throw new IllegalArgumentException("The actual price ask to set must be lower than the actual price bid.");

        // Prices' market / this market check.
        if (!actualPriceAsk.getMarket().equals(this) || !actualPriceBid.getMarket().equals(this))
            throw new IllegalArgumentException("The market of the prices to set must match with the market.");

        GenericPrice genericActualPriceAsk = new GenericPrice(actualPriceAsk.getValue());
        GenericPrice genericActualPriceBid = new GenericPrice(actualPriceBid.getValue());

        this.actualPriceAsk = genericActualPriceAsk;
        this.actualPriceBid = genericActualPriceBid;

    }
    /**
     * 
     * Sets the main market of the system.
     * 
     * Synchronized method to avoid multiple threads to set the main market at the same time.
     * 
     * @param mainMarket The new main market of the system to set.
     * 
     * @throws NullPointerException If the given market is null.
     * 
     */
    public static synchronized void setMainMarket(Market mainMarket) throws NullPointerException {

        // Null check.
        if (mainMarket == null)
            throw new NullPointerException("The main market of the system to set cannot be null.");

        Market.mainMarket = mainMarket;

    }

    @Override
    public String toString() {

        String ask = this.getActualPriceAsk() == null ? "null" : this.getActualPriceAsk().toStringWithoutMarket();
        String bid = this.getActualPriceBid() == null ? "null" : this.getActualPriceBid().toStringWithoutMarket();
        
        return String.format("Pair [%s/%s] - Actual Ask [%s] - Actual Bid [%s] - Price Increment [%s]", this.getPrimaryCurrency().name(), this.getSecondaryCurrency().name(), ask, bid, this.getIncrement().toString());

    }

    @Override
    public int compareTo(Market otherMarket) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (otherMarket == null)
            throw new NullPointerException("The market to compare to cannot be null.");
        
        // Equal markets.
        if (this.getPrimaryCurrency() == otherMarket.getPrimaryCurrency() && this.getSecondaryCurrency() == otherMarket.getSecondaryCurrency())
            return 0;

        throw new IllegalArgumentException("Cannot compare different markets.");

    }

    /**
     * 
     * Copy a market by creating a new market object with the same properties.
     * 
     * Used to copy a market in the SpecificPrice class.
     * 
     * @param market The market to copy.
     * 
     * @return A new market object with the same primary and secondary currencies, the actuals ask and bid prices and the increment of the price between two consecutive prices.
     * 
     * @throws NullPointerException If the given market to copy is null.
     * 
     */
    public static Market copyMarket(Market market) throws NullPointerException {

        // Null check.
        if (market == null)
            throw new NullPointerException("The market to copy from cannot be null.");

        return new Market(market.getPrimaryCurrency(), market.getSecondaryCurrency(), market.actualPriceAsk, market.actualPriceBid, market.getIncrement());
        
    }

}
