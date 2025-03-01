package CROSS.Types.Price;

import CROSS.OrderBook.Market;

/**
 * 
 * SpecificPrice is a class that extends GenericPrice and implements Comparable<>.
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

        // Null checks.
        if (type == null) {
            throw new NullPointerException("Type of a specific price cannot be null.");
        }
        if (market == null)
            throw new NullPointerException("Market of a specific price cannot be null.");

        super(value);

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
     * Getter of the market attribute as a copy of the market.
     * 
     * @return The market of the price as a Market object.
     * 
     */
    public Market getMarket() {
        return Market.copyMarket(this.market);
    }

    // TO STRING METHODS
    @Override
    public String toString() {
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
    public int compareTo(SpecificPrice price) throws IllegalArgumentException, NullPointerException  {

        // Null check.
        if (price == null)
            throw new NullPointerException("Cannot compare a specific price to a null one.");

        // Check if the prices are from the same market.
        if (!price.getMarket().equals(this.market))
            throw new IllegalArgumentException("Cannot compare specific prices from different markets to prevent introducing bugs.");
        
        return Integer.compare(price.getValue(), this.getValue());

    }
    
}
