package CROSS.OrderBook;

import java.util.TreeMap;

import CROSS.Enums.Currency;
import CROSS.Types.SpecificPrice;

public class OrderBook extends Market {

    // Only for LIMITS orders.
    // TODO: Change this to: Key: SpecificPrice, Value: Line. Line is a new class that contains quantity and users orders list.
    // TODO: Impment a new data structure to store the STOP orders.
    private TreeMap<SpecificPrice, Quantity> book;
    
    public OrderBook(Currency primary_currency, Currency secondary_currency, SpecificPrice actualPrice) {
        super(primary_currency, secondary_currency, actualPrice);
        book = new TreeMap<SpecificPrice, Quantity>();
    }

    @Override
    public String toString() {
        String basicInfos = super.toString();
        for (SpecificPrice price : book.keySet()) {
            String line;
            // TODO: Finish toString().
            line = price + " : " + book.get(price) + "\n";
        }
        return basicInfos;
    }
    


}
