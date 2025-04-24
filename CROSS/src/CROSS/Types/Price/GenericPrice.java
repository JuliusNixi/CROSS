package CROSS.Types.Price;

/**
 * 
 * GenericPrice is a class that represents a generic price.
 * 
 * Generic price means without any associated additional information, outside of the price value.
 * It's used for example to rapresent the increment of the price in the Market class.
 * 
 * It's extended by SpecificPrice class that adds other informations.
 * 
 * The class has a validation check that ensures that the price is not negative or 0.
 * 
 * The price is requested to be an Integer from the assignment.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see SpecificPrice
 * 
 */
public class GenericPrice {
    
    private final Integer value;

    /**
     * 
     * Constructor for the class.
     * 
     * @param value The price value as Integer.
     * 
     * @throws NullPointerException If the price value is null.
     * @throws IllegalArgumentException If the price value is negative or 0.
     * 
     */
    public GenericPrice(Integer value) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (value == null) {
            throw new NullPointerException("Price value cannot be null.");
        }

        // Negative or 0 check.
        if (value <= 0) {
            throw new IllegalArgumentException("Price value cannot be negative or 0.");
        }

        this.value = value;

    }

    // GETTERS
    /**
     * 
     * Getter for the price value.
     * 
     * @return The price value as Integer.
     * 
     */
    public Integer getValue() {

        return this.value;

    }
    
    @Override
    public String toString() {

        return this.getValue().toString();
        
    }

}
