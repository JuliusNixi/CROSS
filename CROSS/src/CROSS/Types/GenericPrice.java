package CROSS.Types;

public class GenericPrice implements Comparable<GenericPrice> {
    
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

    // This method DOES NOT CHECK if the prices are of the same type.
    // Use the SpecificPrice class to compare prices of the same type.
    @Override
    public int compareTo(GenericPrice p) {
        return this.value.compareTo(p.value);
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
