package CROSS.OrderBook;

import CROSS.Enums.Currency;
import CROSS.Enums.PriceType;
import CROSS.Types.SpecificPrice;

public class Market {

    Currency primary_currency;
    Currency secondary_currency;
    // This is the actual price of the market, it's a ask price.
    SpecificPrice actualPrice;
    
    public Market(Currency primary_currency, Currency secondary_currency, SpecificPrice actualPrice) throws IllegalArgumentException {
        if (actualPrice.getType() != PriceType.ASK)
            throw new IllegalArgumentException("The actual price of a market must be an ask price.");
        this.primary_currency = primary_currency;
        this.secondary_currency = secondary_currency;
        // This price is intended to be the actual price of the market, it's a ask price.
        this.actualPrice = actualPrice;
    }
    
    public Currency getPrimaryCurrency() {
        return primary_currency;
    }
    public Currency getSecondaryCurrency() {
        return secondary_currency;
    }
    public SpecificPrice getActualPrice() {
        return actualPrice;
    }
    
    public void setActualPrice(SpecificPrice actualPrice) throws IllegalArgumentException {
        if (actualPrice.getType() != PriceType.ASK)
            throw new IllegalArgumentException("The actual price of a market must be an ask price.");
        this.actualPrice = actualPrice;
    }
    
    @Override
    public String toString() {
        return String.format("Name [%s/%s] - Actual Price [%s]", primary_currency.name(), secondary_currency.name(), actualPrice.toString());
    }

}
