package CROSS.Types;

import CROSS.Enums.PriceType;

public class SpecificPrice extends GenericPrice {
    
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

    // This method is used to compare prices of the same type.
    // On different types, it throws an exception.
    public int compareTo(SpecificPrice price) throws IllegalArgumentException {
        if (this.type != price.getType()) {
            throw new IllegalArgumentException("Cannot compare prices of different types.");
        }
        return super.compareTo(price);
    }
    
}
