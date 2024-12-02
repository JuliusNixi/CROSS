package CROSS;
public class Quantity {
    Double quantity;
    public Quantity(Double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity cannot be negative or 0.");
        }
        this.quantity = quantity;
    }
    public Double getQuantity() {
        return quantity;
    }
}
