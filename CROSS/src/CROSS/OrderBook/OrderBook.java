package CROSS.OrderBook;

import java.util.TreeMap;

import CROSS.Enums.Currency;
import CROSS.Types.SpecificPrice;

public class OrderBook extends Market {

    // By using a TreeMap, the order book is always sorted by price.
    private TreeMap<SpecificPrice, OrderBookLine> limitBook;
    // Technically the order book contains only the limit orders.
    // The majority of the brokers not show the stop orders in the order book.
    // The stop orders are hidden and are only executed when the price hits the stop price.
    // So i will follow this philosophy.
    // I will use the same data structure because i think that it fits well also for the stop orders.
    // But, the OFFICIAL order book is the limit orders book, that contains only the limit orders.
    // The stop orders book is "opaque".
    // So, for example, the toString() method will show only the limit orders.
    private TreeMap<SpecificPrice, OrderBookLine> stopBook;
    
    public OrderBook(Currency primary_currency, Currency secondary_currency, SpecificPrice actualPriceAsk, SpecificPrice actualPriceBid) {
        super(primary_currency, secondary_currency, actualPriceAsk, actualPriceBid);
        limitBook = new TreeMap<SpecificPrice, OrderBookLine>();
    }

    @Override
    public String toString() {
        String basicInfos = super.toString() + "\n";
        for (OrderBookLine line : limitBook.values()) {
            String lineStr = line.toString();
            basicInfos += lineStr;
        }
        return basicInfos;
    }
    
}
