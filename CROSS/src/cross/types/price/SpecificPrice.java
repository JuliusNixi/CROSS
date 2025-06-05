package cross.types.price;

import cross.types.Currency;

/**
 *
 * SpecificPrice is a class that extends GenericPrice.
 *
 * It is used to represent a price with a specific associated type (ask / bid).
 * A type is an enum that represents the type of the price in the PriceType enum format.
 *
 * The price also has a primary and secondary currency attribute that represents the pair of the price.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see GenericPrice
 *
 * @see PriceType
 * 
 * @see Currency
 *
 */
public final class SpecificPrice extends GenericPrice {

    // ASK / BID
    private final PriceType type;

    // CURRENCIES
    private transient final Currency primaryCurrency;
    private transient final Currency secondaryCurrency;

    /**
     *
     * Constructor of the class.
     *
     * @param value The value of the price as an Integer.
     * @param type The type of the price as an enum PriceType.
     * @param primaryCurrency The primary currency of the price as an enum Currency.
     * @param secondaryCurrency The secondary currency of the price as an enum Currency.
     *
     * @throws NullPointerException If the value or the type or the primary or secondary currency are null.
     * @throws IllegalArgumentException If the value is 0 or negative or the primary and secondary currencies are the same.
     *
     */
    public SpecificPrice(Integer value, PriceType type, Currency primaryCurrency, Currency secondaryCurrency) throws NullPointerException, IllegalArgumentException {

        super(value);

        // Null check.
        if (type == null) {
            throw new NullPointerException("Price type cannot be null.");
        }
        if (primaryCurrency == null || secondaryCurrency == null) {
            throw new NullPointerException("Primary and / or secondary currency/ies cannot be null.");
        }

        // Check if the primary and secondary currencies are the same.
        if (primaryCurrency.compareTo(secondaryCurrency) == 0) {
            throw new IllegalArgumentException("Primary and secondary currencies cannot be the same.");
        }

        this.type = type;
        this.primaryCurrency = primaryCurrency;
        this.secondaryCurrency = secondaryCurrency;

    }

    // GETTERS
    /**
     *
     * Getter of the type attribute.
     *
     * @return The type of the price as an enum PriceType.
     *
     */
    public PriceType getType() {

        return this.type;

    }
    /**
     *
     * Getter of the primary currency attribute.
     *
     * @return The primary currency as an enum Currency.
     *
     */
    public Currency getPrimaryCurrency() {

        return this.primaryCurrency;

    }
    /**
     *
     * Getter of the secondary currency attribute.
     *
     * @return The secondary currency as an enum Currency.
     *
     */
    public Currency getSecondaryCurrency() {

        return this.secondaryCurrency;

    }
    
    // TO STRING METHODS
    @Override
    public String toString() {

        return String.format("Specific Price [Type [%s] - Price Value [%s] - Primary Currency [%s] - Secondary Currency [%s]]", this.getType().name(), super.toString(), this.primaryCurrency.name(), this.secondaryCurrency.name());

    }
    /**
     *
     * A short to string method for the price with the primary currency.
     *
     * @return A short string for the price with the primary currency.
     *
     */
    public String toStringShort() {

        return String.format("%s %s", super.toString(), this.primaryCurrency.name());

    }

    @Override
    public int compareTo(GenericPrice otherGenericPrice) throws NullPointerException, IllegalArgumentException {

        if (!(otherGenericPrice instanceof SpecificPrice))
            throw new IllegalArgumentException("Can only compare SpecificPrice with SpecificPrice.");

        SpecificPrice otherSpecificPrice = (SpecificPrice) otherGenericPrice;

        if (otherSpecificPrice.getPrimaryCurrency() != this.getPrimaryCurrency() || otherSpecificPrice.getSecondaryCurrency() != this.getSecondaryCurrency()) {
            throw new IllegalArgumentException("Cannot compare prices with different primary or secondary currencies.");
        }

        GenericPrice otherPriceGeneric = new GenericPrice(otherSpecificPrice.getValue());
        return super.compareTo(otherPriceGeneric);

    }

}

