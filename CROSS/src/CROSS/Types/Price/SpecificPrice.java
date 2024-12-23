package CROSS.Types.Price;

/**
 * SpecificPrice is a class that extends GenericPrice and implements Comparable<SpecificPrice>.
 * It is used to represent a price with a specific type.
 * A type is an enum that represents the type of the price in the PriceType enum.
 * The implementation of the Comparable interface is used to compare two prices.
 * It's used to sort a list of prices in descending order to use it in the OrderBook.
 * 
 * @version 1.0
 * @see GenericPrice
 * @see PriceType
 * @see Comparable
 */
public class SpecificPrice extends GenericPrice implements Comparable<SpecificPrice> {
    
    private PriceType type;

    /**
     * Constructor of the SpecificPrice class.
     * It takes an integer value and a PriceType type.
     * It calls the constructor of the GenericPrice class with the value.
     * 
     * @param value The value of the price.
     * @param type The type of the price.
     * @throws IllegalArgumentException If the type is null.
     */
    public SpecificPrice(Integer value, PriceType type) throws IllegalArgumentException {
        super(value);
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        this.type = type;
    }

    /**
     * Getter of the type attribute.
     * 
     * @return The type of the price.
     */
    public PriceType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("Type [%s] - Price [%s]", this.getType().name(), super.toString());
    }

    @Override
    public int compareTo(SpecificPrice price)  {
        return Integer.compare(price.getValue(), this.getValue());
    }
    
}
