package CROSS.Types;

/**
 * 
 * Quantity class.
 * This class is used to represent the quantity of an order.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 */
public class Quantity {

    private final Integer quantity;

    /**
     * 
     * Constructor of the class.
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

        // Quantity cannot be negative but can be zero.
        // 0 is used during the execution of an order.
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity value cannot be negative.");
        }

        this.quantity = quantity;

    }

    /**
     * 
     * Get the quantity.
     * 
     * @return The quantity as Integer.
     * 
     */
    public Integer getValue() {

        return Integer.valueOf(this.quantity);

    }

    @Override
    public String toString() {

        return this.getValue().toString();
        
    }

}
