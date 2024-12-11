package CROSS.Types;

public class Quantity {

    private Integer quantity;

    public Quantity(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity cannot be negative or 0.");
        }
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }
    
}
