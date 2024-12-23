package CROSS.Types;

public class SpecificPrice extends GenericPrice implements Comparable<SpecificPrice> {
    
    private PriceType type;

    public SpecificPrice(Integer value, PriceType type) {
        super(value);
        this.type = type;
    }

    public PriceType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("Type [%s] - Price [%s]", this.getType().name(), super.toString());
    }

    @Override
    public int compareTo(SpecificPrice price)  {
        return Integer.compare(price.getValue(), this.getValue());
    }
    
}
