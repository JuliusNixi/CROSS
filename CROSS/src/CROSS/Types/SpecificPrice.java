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
    public String toString() throws IllegalArgumentException {
        String type = "";
        switch (this.type) {
            case ASK:
                type = "Ask";
                break;
            case BID:
                type = "Bid";
                break;
            default:
                throw new IllegalArgumentException("Invalid price type.");
        }
        return type + ": " + super.getValue();
    }

    public int compareTo(SpecificPrice p) throws IllegalArgumentException {
        if (this.type != p.getType()) {
            throw new IllegalArgumentException("Cannot compare prices of different types.");
        }
        return super.compareTo(p);
    }
    
}
