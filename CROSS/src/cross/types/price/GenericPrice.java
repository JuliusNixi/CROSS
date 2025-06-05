package cross.types.price;

/**
 *
 * GenericPrice is a class that represents a generic price.
 *
 * Generic price means without any associated additional information outside of the price value.
 *
 * It's extended by SpecificPrice class that adds other informations.
 *
 * The class has a validation check that ensures that the price is not negative or 0.
 *
 * The price is requested to be an Integer from the assignment.
 * 
 * Implements Comparable interface to compare prices.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see SpecificPrice
 * 
 * @see ClientActionsUtils
 * 
 * @see Comparable
 * 
 */
public class GenericPrice implements Comparable<GenericPrice> {

    private final Integer price;

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

        this.price = value;

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

        return this.price;

    }

    @Override
    public String toString() {

        return this.getValue().toString();

    }

    @Override
    public int compareTo(GenericPrice otherPrice) throws NullPointerException {

        // Null check.
        if (otherPrice == null) {
            throw new NullPointerException("Other price to compare with cannot be null.");
        }

        // Inverted comparison to have the higher price first (ask).
        return -Integer.compare(this.price, otherPrice.price);

    }

}
