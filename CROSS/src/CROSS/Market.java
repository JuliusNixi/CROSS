package CROSS;

import CROSS.Enums.Currency;

public class Market {
    Currency primary_currency;
    Currency secondary_currency;
    Price price;
    public Market(Currency primary_currency, Currency secondary_currency, Price price) {
        this.primary_currency = primary_currency;
        this.secondary_currency = secondary_currency;
        // This price is intended to be the actual price of the market.
        this.price = price;
    }
    public Currency getPrimaryCurrency() {
        return primary_currency;
    }
    public Currency getSecondaryCurrency() {
        return secondary_currency;
    }
    public Price getPrice() {
        return price;
    }
    public String toString() {
        return primary_currency + "/" + secondary_currency + ": " + price;
    }
}
