package CROSS.Types;

/**
 * Quantity class.
 * This class is used to represent the quantity of an order.
 */
public class Quantity {

    private Integer quantity;

    /**
     * Constructor.
     * @param quantity The quantity of the order.
     * @throws IllegalArgumentException If the quantity is negative.
     * @throws NullPointerException If the quantity is null.
     */
    public Quantity(Integer quantity) throws IllegalArgumentException, NullPointerException {
        if (quantity == null) {
            throw new NullPointerException("Quantity cannot be null.");
        }
        // Quantity cannot be negative but can be zero.
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.quantity = quantity;
    }

    /**
     * Get the quantity.
     * @return The quantity.
     */
    public Integer getQuantity() {
        return this.quantity;
    }

    @Override
    public String toString() {
        return this.getQuantity().toString();
    }

}
