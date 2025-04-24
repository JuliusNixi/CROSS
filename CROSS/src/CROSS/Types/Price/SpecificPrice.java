package CROSS.Types.Price;

import CROSS.OrderBook.Market;

/**
 * 
 * SpecificPrice is a class that extends GenericPrice and implements Comparable.
 * 
 * It is used to represent a price with a specific associated type (ask / bid) in a specific market.
 * A type is an enum that represents the type of the price in the PriceType enum format.
 * 
 * The implementation of the Comparable interface is used to compare two prices.
 * It's used to sort a list of prices and to use it in the OrderBook.
 * 
 * The price also has a market attribute that represents the market of the price, to avoid comparing prices from different markets. 
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see GenericPrice
 * 
 * @see PriceType
 * @see Market
 * 
 * @see Comparable
 * 
 */
public class SpecificPrice extends GenericPrice implements Comparable<SpecificPrice> {
    
    // ASK / BID
    private final PriceType type;

    // The market of the price, to avoid comparing prices from different markets.
    private final Market market;

    /**
     * 
     * Constructor of the class.
     * 
     * @param value The value of the price as an Integer.
     * @param type The type of the price as an enum PriceType.
     * @param market The market of the price as a Market object.
     * 
     * @throws NullPointerException If the value or the type or the market are null.
     * @throws IllegalArgumentException If the value is 0 or negative.
     * 
     */
    public SpecificPrice(Integer value, PriceType type, Market market) throws NullPointerException, IllegalArgumentException {

        super(value);

        // Null checks.
        if (type == null) {
            throw new NullPointerException("Type of a specific price cannot be null.");
        }
        if (market == null)
            throw new NullPointerException("Market of a specific price cannot be null.");

        this.type = type;
        this.market = market;

    }

    // GETTERS
    /**
     * 
     * Getter of the type attribute.
     * 
     * @return The type of the price as an enum PriceType.
     * 
     */
    public PriceType getType() {

        return this.type;

    }
    /**
     * 
     * Getter of the market attribute.
     * 
     * @return The market of the price as a Market object.
     * 
     */
    public Market getMarket() {

        return this.market;

    }

    // TO STRING METHODS
    @Override
    public String toString() {

        // The market is a copy, no synchronization needed.
        return String.format("Type [%s] - Price Value [%s] - Market [%s]", this.getType().name(), super.toString(), market.toString());

    }
    /**
     * 
     * A short to string method for the price with the currency.
     * 
     * @return A short string for the price with the currency.
     * 
     */
    public String toStringShort() {

        // The market is a copy, no synchronization needed.
        return String.format("%s %s", super.toString(), this.market.getPrimaryCurrency().name());

    }
    /**
     * 
     * A to string method for the price without the market.
     * Used to avoid infinite loops in the toString method of the Market class.
     * 
     * @return A string with the price but without the market.
     * 
     */
    public String toStringWithoutMarket() {

        return String.format("Type [%s] - Price Value [%s]", this.getType().name(), super.toString());

    }

    @Override
    public int compareTo(SpecificPrice otherPrice) throws IllegalArgumentException, NullPointerException  {

        // Null check.
        if (otherPrice == null)
            throw new NullPointerException("Cannot compare a specific price to a null one.");

        // Check if the prices are from the same market.
        // No synchronization needed since the price has no setters and the market is a copy.
        if (otherPrice.getMarket().compareTo(this.market) != 0)
            throw new IllegalArgumentException("Cannot compare specific prices from different markets to prevent introducing bugs.");
        
        return Integer.compare(otherPrice.getValue(), this.getValue());

    }
    
}
