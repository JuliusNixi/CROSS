package CROSS.Types;

public class Quantity {

    private Integer quantity;

    public Quantity(Integer quantity) throws IllegalArgumentException {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return this.quantity;
    }
    public void setQuantity(Integer quantity) throws IllegalArgumentException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity cannot be negative or 0.");
        }
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return this.getQuantity().toString();
    }

}
