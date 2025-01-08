package CROSS.Types.Price;

import CROSS.OrderBook.Market;

/**
 * SpecificPrice is a class that extends GenericPrice and implements Comparable<SpecificPrice>.
 * 
 * It is used to represent a price with a specific type (ask/bid) in a specific market.
 * 
 * A type is an enum that represents the type of the price in the PriceType enum.
 * 
 * The implementation of the Comparable interface is used to compare two prices.
 * 
 * It's used to sort a list of prices and to use it in the OrderBook.
 * 
 * @version 1.0
 * @see GenericPrice
 * @see PriceType
 * @see Comparable
 * @see Market
 */
public class SpecificPrice extends GenericPrice implements Comparable<SpecificPrice> {
    
    private PriceType type;
    private Market market;

    /**
     * Constructor of the SpecificPrice class.
     * It takes an integer value and a PriceType type.
     * 
     * @param value The value of the price.
     * @param type The type of the price.
     * @param market The market of the price.
     * 
     * @throws NullPointerException If the type or market are null.
     */
    public SpecificPrice(Integer value, PriceType type, Market market) throws NullPointerException {

        if (type == null) {
            throw new NullPointerException("Type cannot be null.");
        }
        if (market == null)
            throw new NullPointerException("Market cannot be null.");

        super(value);

        this.type = type;
        this.market = market;
    }

    /**
     * Getter of the type attribute.
     * 
     * @return The type of the price.
     */
    public PriceType getType() {
        return this.type;
    }
    /**
     * Getter of the market attribute.
     * 
     * @return The market of the price.
     */
    public Market getMarket() {
        return this.market;
    }

    @Override
    public String toString() {
        return String.format("Type [%s] - Price [%s] - Market [%s]", this.getType().name(), super.toString(), market.toString());
    }
    /**
     * A short to string method for the price with the currency.
     * @return A short to string method for the price with the currency.
     */
    public String toStringShort() {
        return String.format("%s %s", super.toString(), this.getMarket().getPrimaryCurrency().name());
    }
    /**
     * A to string method for the price without the market.
     * Used to avoid infinite loops in the toString method of the Market class.
     * 
     * @return A to string method for the price without the market.
     */
    public String toStringWithoutMarket() {
        return String.format("Type [%s] - Price [%s]", this.getType().name(), super.toString());
    }

    @Override
    public int compareTo(SpecificPrice price) throws IllegalArgumentException, NullPointerException  {

        if (price == null)
            throw new NullPointerException("Cannot compare a SpecificPrice to null.");

        if (!price.getMarket().equals(this.getMarket()))
            throw new IllegalArgumentException("Cannot compare SpecificPrices from different markets to prevent errors.");
        
        return Integer.compare(price.getValue(), this.getValue());
    }
    
}
