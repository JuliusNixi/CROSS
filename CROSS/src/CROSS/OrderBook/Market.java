package CROSS.OrderBook;

import CROSS.Types.Currency;
import CROSS.Types.Price.GenericPrice;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;

public class Market {

    Currency primary_currency;
    Currency secondary_currency;

    SpecificPrice actualPriceAsk;
    SpecificPrice actualPriceBid;

    // Increment of the price between two consecutive prices.
    GenericPrice increment;
    
    public Market(Currency primary_currency, Currency secondary_currency, SpecificPrice actualPriceAsk, SpecificPrice actualPriceBid, GenericPrice increment) throws IllegalArgumentException {
        if (actualPriceAsk.getType() != PriceType.ASK)
            throw new IllegalArgumentException("The actual price ask of a market must be an ask price.");
        if (actualPriceBid.getType() != PriceType.BID)
            throw new IllegalArgumentException("The actual price bid of a market must be a bid price.");
        if (increment.getValue() <= 0)
            throw new IllegalArgumentException("The increment must be greater than 0.");
        this.primary_currency = primary_currency;
        this.secondary_currency = secondary_currency;
        // This price is intended to be the actual price of the market.
        this.actualPriceAsk = actualPriceAsk;
        this.actualPriceBid = actualPriceBid;
        this.increment = increment;
    }
    
    public Currency getPrimaryCurrency() {
        return primary_currency;
    }
    public Currency getSecondaryCurrency() {
        return secondary_currency;
    }
    
    public SpecificPrice getActualPriceAsk() {
        return actualPriceAsk;
    }
    public SpecificPrice getActualPriceBid() {
        return actualPriceBid;
    }

    public void setActualPriceAsk(SpecificPrice actualPriceAsk) {
        if (actualPriceAsk.getType() != PriceType.ASK)
            throw new IllegalArgumentException("The given price is not an ASK price.");
        this.actualPriceAsk = actualPriceAsk;
    }
    public void setActualPriceBid(SpecificPrice actualPriceBid) {
        if (actualPriceBid.getType() != PriceType.BID)
            throw new IllegalArgumentException("The given price is not a BID price.");
        this.actualPriceBid = actualPriceBid;
    }

    public GenericPrice getIncrement() {
        return increment;
    }

    @Override
    public String toString() {
        return String.format("Name [%s/%s] - Actual Ask [%s] - Actual Bid [%s] - Price Increment [%s]", this.getPrimaryCurrency().name(), this.getSecondaryCurrency().name(), this.getActualPriceAsk().toString(), this.getActualPriceBid().toString(), this.getIncrement().toString());
    }

}
