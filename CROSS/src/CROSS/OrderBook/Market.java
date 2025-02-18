package CROSS.OrderBook;

import CROSS.Types.Currency;
import CROSS.Types.Price.GenericPrice;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;

/**
 * 
 * This class represents a market in the system. 
 * 
 * A market is defined by the primary and secondary currency, the actual ask and bid prices and the increment of the price between two consecutive prices.
 * The actual ask and bid prices are the prices at which the market is currently trading. 
 * 
 * The market is used to create orders to trade currencies using the order book of the market.
 * 
 * Each price has an associated market, so the market is used to create prices.
 * 
 * The project assignment specifies that there is only one market in the system.
 * This class has been implemented to stay generic and to allow the creation of multiple markets.
 * So the class has a static field to store the main market of the system that will be used in the project.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Currency
 * @see SpecificPrice
 * @see GenericPrice
 * 
 */
public class Market implements Comparable<Market> {

    private final Currency primary_currency;
    private final Currency secondary_currency;

    private SpecificPrice actualPriceAsk = null;
    private SpecificPrice actualPriceBid = null;

    // Increment of the price between two consecutive prices.
    private final GenericPrice increment;

    private static Market mainMarket = null;
    
    /**
     * 
     * Constructor of the class.
     * 
     * Creates a new market with the given primary and secondary currencies, the actual ask and bid prices and the increment of the price between two consecutive prices.
     * 
     * The actual ask and bid prices are the prices at which the market is currently trading and COULD be null.
     * 
     * @param primary_currency The primary currency of the market.
     * @param secondary_currency The secondary currency of the market.
     * @param actualPriceAsk The actual ask price of the market.
     * @param actualPriceBid The actual bid price of the market.
     * @param increment The increment of the price between two consecutive prices.
     * 
     * @throws IllegalArgumentException If the primary and secondary currencies are the same, if the actual ask price is higher than the actual bid price or if the actual prices are not of the correct type.
     * @throws NullPointerException If the primary currency, secondary currency or increment are null.
     * 
     */
    public Market(Currency primary_currency, Currency secondary_currency, SpecificPrice actualPriceAsk, SpecificPrice actualPriceBid, GenericPrice increment) throws IllegalArgumentException, NullPointerException {
        
        // Null checks.
        if (primary_currency == null)
            throw new NullPointerException("The primary currency of a market cannot be null.");
        if (secondary_currency == null)
            throw new NullPointerException("The secondary currency of a market cannot be null.");
        if (increment == null)
            throw new NullPointerException("The price increment of a market cannot be null.");

        // Prices types checks.
        if (actualPriceAsk != null && actualPriceAsk.getType() != PriceType.ASK)
            throw new IllegalArgumentException("The actual price ask of a market must be an ask price.");
        if (actualPriceBid != null && actualPriceBid.getType() != PriceType.BID)
            throw new IllegalArgumentException("The actual price bid of a market must be a bid price.");

        // The primary and secondary currencies of a market cannot be the same.
        if (primary_currency == secondary_currency)
            throw new IllegalArgumentException("The primary and secondary currencies of a market cannot be the same.");

        // The actual ask price must be lower than the actual bid price.
        if (actualPriceAsk != null && actualPriceBid != null && actualPriceAsk.getValue() >= actualPriceBid.getValue())
            throw new IllegalArgumentException("The actual price ask must be lower than the actual price bid.");

        this.primary_currency = primary_currency;
        this.secondary_currency = secondary_currency;

        // This price is intended to be the actual price of the market.
        // It could be null, because otherwise there's a recursive problem:
        // Market need price to be created, but price need market to be created.
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
     * Returns the actual ask price of the market.
     * 
     * @return The actual ask price of the market as a SpecificPrice object.
     * 
     */
    public SpecificPrice getActualPriceAsk() {

        // Null check.
        if (this.actualPriceAsk == null)
            return null;

        return new SpecificPrice(this.actualPriceAsk.getValue(), this.actualPriceAsk.getType(), this.actualPriceAsk.getMarket());

    }
    /**
     * 
     * Returns the actual bid price of the market.
     * 
     * @return The actual bid price of the market as a SpecificPrice object.
     * 
     */
    public SpecificPrice getActualPriceBid() {

        // Null check.
        if (this.actualPriceBid == null)
            return null;
            
        return new SpecificPrice(this.actualPriceBid.getValue(), this.actualPriceBid.getType(), this.actualPriceBid.getMarket());

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
     * @return The main market of the system as a Market object.
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
     * Sets the actual ask and bid prices of the market.
     * 
     * Synchonized method to avoid multiple threads to set the actual prices at the same time.
     * 
     * @param actualPriceAsk The new actual ask price of the market.
     * @param actualPriceBid The new actual bid price of the market.
     * 
     * @throws NullPointerException If the given prices are BOTH null.
     * 
     */
    public synchronized void setActualPrices(SpecificPrice actualPriceAsk, SpecificPrice actualPriceBid) throws NullPointerException {
        
        // Null check.
        if (actualPriceAsk == null && actualPriceBid == null)
            throw new NullPointerException("The actual prices of a market cannot be null at the same time.");
        
        if (actualPriceAsk != null)
            this.setActualPriceAsk(actualPriceAsk);
        if (actualPriceBid != null)
            this.setActualPriceBid(actualPriceBid);

    }
    /**
     * 
     * Sets the actual ask price of the market.
     * 
     * Synchonized method to avoid multiple threads to set the actual prices at the same time.
     * 
     * @param actualPriceAsk The new actual ask price of the market.
     * 
     * @throws IllegalArgumentException If the given price is not an ask price, if the actual price bid is not null and the new actual price ask is higher than the actual price bid.
     * @throws NullPointerException If the given price is null.
     * 
     */
    public synchronized void setActualPriceAsk(SpecificPrice actualPriceAsk) throws IllegalArgumentException, NullPointerException {
        
        // Null check.
        if (actualPriceAsk == null)
            throw new NullPointerException("The actual ask price of a market cannot be null.");

        // Price type check.
        if (actualPriceAsk.getType() != PriceType.ASK)
            throw new IllegalArgumentException("The given price to set is not an ASK price.");

        // The actual ask price must be lower than the actual bid price.
        if (this.actualPriceBid != null && actualPriceAsk.getValue() >= this.actualPriceBid.getValue())
            throw new IllegalArgumentException("The actual price ask must be lower than the actual price bid.");

        this.actualPriceAsk = actualPriceAsk;

    }
    /**
     * 
     * Sets the actual bid price of the market.
     * 
     * Synchonized method to avoid multiple threads to set the actual prices at the same time.
     * 
     * @param actualPriceBid The new actual bid price of the market.
     * 
     * @throws IllegalArgumentException If the given price is not a bid price, if the actual price ask is not null and the new actual price bid is lower than the actual price ask.
     * @throws NullPointerException If the given price is null.
     * 
     */
    public synchronized void setActualPriceBid(SpecificPrice actualPriceBid) throws IllegalArgumentException, NullPointerException {
        
        // Null check.
        if (actualPriceBid == null)
            throw new NullPointerException("The actual bid price of a market cannot be null.");

        // Price type check.
        if (actualPriceBid.getType() != PriceType.BID)
            throw new IllegalArgumentException("The given price to set is not a BID price.");

        // The actual bid price must be higher than the actual ask price.
        if (this.actualPriceAsk != null && this.actualPriceAsk.getValue() >= actualPriceBid.getValue())
            throw new IllegalArgumentException("The actual price ask must be lower than the actual price bid.");

        this.actualPriceBid = actualPriceBid;

    }

    /**
     * 
     * Sets the main market of the system.
     * 
     * Synchonized method to avoid multiple threads to set the main market at the same time.
     * 
     * @param mainMarket The new main market of the system.
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
        
        return String.format("Name [%s/%s] - Actual Ask [%s] - Actual Bid [%s] - Price Increment [%s]", this.getPrimaryCurrency().name(), this.getSecondaryCurrency().name(), ask, bid, this.getIncrement().toString());

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
     * Copy a market.
     * 
     * Used to copy a market in the SpecificPrice class.
     * 
     * @param market The market to copy.
     * 
     * @return A new market with the same primary and secondary currencies, the actual ask and bid prices and the increment of the price between two consecutive prices.
     * 
     * @throws NullPointerException If the given market is null.
     * 
     */
    public static Market copyMarket(Market market) throws NullPointerException {

        // Null check.
        if (market == null)
            throw new NullPointerException("The market to copy cannot be null.");

        return new Market(market.getPrimaryCurrency(), market.getSecondaryCurrency(), market.getActualPriceAsk(), market.getActualPriceBid(), market.getIncrement());
        
    }

}
