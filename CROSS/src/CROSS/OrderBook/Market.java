package CROSS.OrderBook;

import CROSS.Types.Currency;
import CROSS.Types.Price.GenericPrice;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;

/**
 * This class represents a market in the system. A market is defined by the primary and secondary currency, the actual ask and bid prices and the increment of the price between two consecutive prices.
 * The actual ask and bid prices are the prices at which the market is currently trading. The increment is the difference between two consecutive prices.
 * The market is used to create orders and to trade currencies.
 * The market is also used to create the order book of the market.
 * @version 1.0
 * @see Currency
 * @see SpecificPrice
 * @see GenericPrice
 */
public class Market {

    Currency primary_currency;
    Currency secondary_currency;

    SpecificPrice actualPriceAsk;
    SpecificPrice actualPriceBid;

    // Increment of the price between two consecutive prices.
    GenericPrice increment;
    
    /**
     * Constructor of the Market class.
     * Creates a new market with the given primary and secondary currencies, the actual ask and bid prices and the increment of the price between two consecutive prices.
     * @param primary_currency The primary currency of the market.
     * @param secondary_currency The secondary currency of the market.
     * @param actualPriceAsk The actual ask price of the market.
     * @param actualPriceBid The actual bid price of the market.
     * @param increment The increment of the price between two consecutive prices.
     * @throws IllegalArgumentException If the primary and secondary currencies are the same, if the actual ask price is higher than the actual bid price or if the actual prices are not of the correct type.
     * @throws NullPointerException If the primary currency, secondary currency or increment are null.
     */
    public Market(Currency primary_currency, Currency secondary_currency, SpecificPrice actualPriceAsk, SpecificPrice actualPriceBid, GenericPrice increment) throws IllegalArgumentException, NullPointerException {
        if (primary_currency == null)
            throw new NullPointerException("The primary currency of a market cannot be null.");
        if (secondary_currency == null)
            throw new NullPointerException("The secondary currency of a market cannot be null.");
        if (increment == null)
            throw new NullPointerException("The increment of a market cannot be null.");

        if (actualPriceAsk != null && actualPriceAsk.getType() != PriceType.ASK)
            throw new IllegalArgumentException("The actual price ask of a market must be an ask price.");
        if (actualPriceBid != null && actualPriceBid.getType() != PriceType.BID)
            throw new IllegalArgumentException("The actual price bid of a market must be a bid price.");

        if (primary_currency == secondary_currency)
            throw new IllegalArgumentException("The primary and secondary currencies of a market cannot be the same.");

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
     * Returns the primary currency of the market.
     * @return The primary currency of the market.
     */
    public Currency getPrimaryCurrency() {
        return primary_currency;
    }
    /**
     * Returns the secondary currency of the market.
     * @return The secondary currency of the market.
     */
    public Currency getSecondaryCurrency() {
        return secondary_currency;
    }
    
    /**
     * Returns the actual ask price of the market.
     * @return The actual ask price of the market.
     */
    public SpecificPrice getActualPriceAsk() {
        return actualPriceAsk;
    }
    /**
     * Returns the actual bid price of the market.
     * @return The actual bid price of the market.
     */
    public SpecificPrice getActualPriceBid() {
        return actualPriceBid;
    }

    /**
     * Returns the increment of the price between two consecutive prices.
     * @return The increment of the price between two consecutive prices.
     */
    public GenericPrice getIncrement() {
        return increment;
    }

    // SETTERS
    /**
     * Sets the actual ask price of the market.
     * @param actualPriceAsk The new actual ask price of the market.
     * @throws IllegalArgumentException If the given price is not an ask price or if the actual ask price is higher than the actual bid price.
     * @throws NullPointerException If the given price is null.
     */
    public void setActualPriceAsk(SpecificPrice actualPriceAsk) throws IllegalArgumentException, NullPointerException {
        if (actualPriceAsk == null)
            throw new NullPointerException("The actual ask price of a market cannot be null.");
        if (actualPriceAsk.getType() != PriceType.ASK)
            throw new IllegalArgumentException("The given price is not an ASK price.");
        if (this.getActualPriceAsk() != null && actualPriceAsk.getValue() >= this.actualPriceBid.getValue())
            throw new IllegalArgumentException("The actual price ask must be lower than the actual price bid.");
        this.actualPriceAsk = actualPriceAsk;
    }
    /**
     * Sets the actual bid price of the market.
     * @param actualPriceBid The new actual bid price of the market.
     * @throws IllegalArgumentException If the given price is not a bid price, if the actual bid price is lower than the actual ask price or if the actual prices are not of the correct type.
     * @throws NullPointerException If the given price is null.
     */
    public void setActualPriceBid(SpecificPrice actualPriceBid) throws IllegalArgumentException, NullPointerException {
        if (actualPriceBid == null)
            throw new NullPointerException("The actual bid price of a market cannot be null.");
        if (actualPriceBid.getType() != PriceType.BID)
            throw new IllegalArgumentException("The given price is not a BID price.");
        if (this.getActualPriceBid() != null && actualPriceBid.getValue() <= this.actualPriceAsk.getValue())
            throw new IllegalArgumentException("The actual price bid must be higher than the actual price ask.");
        this.actualPriceBid = actualPriceBid;
    }

    @Override
    public String toString() {
        String ask = this.getActualPriceAsk() == null ? "null" : this.getActualPriceAsk().toStringWithoutMarket();
        String bid = this.getActualPriceBid() == null ? "null" : this.getActualPriceBid().toStringWithoutMarket();
        return String.format("Name [%s/%s] - Actual Ask [%s] - Actual Bid [%s] - Price Increment [%s]", this.getPrimaryCurrency().name(), this.getSecondaryCurrency().name(), ask, bid, this.getIncrement().toString());
    }

}
