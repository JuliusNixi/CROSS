package cross.types;

/**
 *
 * Quantity class.
 *
 * This class is used to represent the quantity of an order.
 *
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see ClientActionsUtils
 *
 */
public class Quantity {
    
    private final Integer quantity;

    /**
     *
     * Constructor of the class.
     * 
     * Quantity is the size of the order, could be zero when the order is fully executed / filled.
     *
     * @param quantity The quantity of the order as an Integer.
     *
     * @throws NullPointerException If the quantity is null.
     * @throws IllegalArgumentException If the quantity is negative.
     *
     */
    public Quantity(Integer quantity) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (quantity == null) {
            throw new NullPointerException("Quantity value cannot be null.");
        }

        // Quantity cannot be negative but COULD be zero.
        // 0 is used during the execution / filling of an order.
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity value cannot be negative.");
        }

        this.quantity = quantity;

    }

    // GETTERS
    /**
     *
     * Get the quantity.
     *
     * @return The quantity as Integer.
     *
     */
    public Integer getValue() {

        return this.quantity;

    }

    @Override
    public String toString() {

        return this.getValue().toString();

    }

}
