package CROSS.Types;

public class GenericPrice {
    
    private Integer value;

    public GenericPrice(Integer value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Price cannot be negative or 0.");
        }
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return this.getValue().toString();
    }

}
