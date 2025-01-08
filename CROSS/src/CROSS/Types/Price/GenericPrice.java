package CROSS.Types.Price;

/**
 * GenericPrice class is a class that represents a generic price.
 * 
 * Generic price means without any type (ask or bid).
 * It's used for example to rapresent the increment of the price in the Market class.
 * 
 * It's extended by SpecificPrice class.
 * 
 * The class has a validation check that ensures that the price is not negative or 0.
 * 
 * @version 1.0
 * @see SpecificPrice
 */
public class GenericPrice {
    
    private Integer value;

    /**
     * Constructor for GenericPrice class.
     * 
     * @param value The price value.
     * @throws IllegalArgumentException If the price is negative or 0.
     * @throws NullPointerException If the price is null.
     */
    public GenericPrice(Integer value) throws IllegalArgumentException, NullPointerException {
        if (value == null) {
            throw new IllegalArgumentException("Price cannot be null.");
        }

        if (value <= 0) {
            throw new IllegalArgumentException("Price cannot be negative or 0.");
        }

        this.value = value;
    }

    /**
     * Getter for the price value.
     * @return The price value.
     */
    public Integer getValue() {
        return Integer.valueOf(this.value);
    }
    
    @Override
    public String toString() {
        return this.getValue().toString();
    }

}
