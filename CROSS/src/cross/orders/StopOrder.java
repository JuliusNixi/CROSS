package cross.orders;

import cross.orderbook.OrderBook;
import cross.types.Quantity;
import cross.types.price.PriceType;
import cross.types.price.SpecificPrice;

/**
 *
 * StopOrder class represents a stop order in the system.
 *
 * A stop order is converted into a market order when the stop price is reached.
 *
 * It extends the Order class.
 * 
 * It has a validation method to check if the price is coherent with the market actual prices if used server side.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Order
 *
 * @see SpecificPrice
 * @see Quantity
 * 
 * @see OrderBook
 *
 * */
public class StopOrder extends Order {

    /**
     *
     * Check if the price given for the stop order creation is coherent with the market actual prices if used server side.
     *
     * Private method used in the constructor only.
     *
     * Synchronized on the order book object to avoid best prices changes while checking the coherence.
     *
     * @param price The price of the stop order.
     *
     * @throws IllegalArgumentException If the price is not valid for a stop order.
     * @throws IllegalStateException If the order book with the given currencies to be used to check the price coherence has not been found.
     *
     */
    private void checkPriceCoherence(SpecificPrice price) throws IllegalArgumentException, IllegalStateException {

        OrderBook orderBook = OrderBook.getOrderBookByCurrencies(price.getPrimaryCurrency(), price.getSecondaryCurrency());
        
        if (orderBook == null) {
            orderBook = OrderBook.getMainOrderBook();
        }

        if (orderBook == null) {
            throw new IllegalStateException("The order book with the given currencies to be used to check the price coherence has not been found. Create it before by setting a best ask or best bid price with these currencies by executing some orders.");
        }

        synchronized (orderBook) {

            // Check if the price is valid for a stop order.
            if (orderBook.getActualPriceBid() != null && price.getType() == PriceType.ASK && price.getValue() >= orderBook.getActualPriceBid().getValue()) {
                throw new IllegalArgumentException("The ASK price to use to SELL with a STOP is greater-equal than the best BID price in the market.");
            }
            if (orderBook.getActualPriceAsk() != null && price.getType() == PriceType.BID && price.getValue() <= orderBook.getActualPriceAsk().getValue()) {
                throw new IllegalArgumentException("The BID price to use to BUY with a STOP is lower-equal than the best ASK price in the market.");
            }

        }

    }
    
    // CONSTRUCTORS
    /**
     *
     * Constructor for the class.
     * It creates a new stop order with the given price and quantity.
     * 
     * It performs a price coherence check, not to be used in the client side.
     *
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     *
     * @throws IllegalArgumentException If the price is not valid for a stop order.
     * @throws NullPointerException If the price or quantity are null.
     * @throws IllegalStateException If the order book with the given currencies to be used to check the price coherence has not been found.
     *
     * */
    public StopOrder(SpecificPrice price, Quantity quantity) throws IllegalArgumentException, NullPointerException, IllegalStateException {

        // Synchronization in the super (but not needed).

        this(price, quantity, false);

    }

    /**
     *
     * Alternative constructor for the class.
     * It creates a new stop order with the given price and quantity.
     *
     * It also has a no coherence checks flag to skip the price coherence checks, used to load the orders from the demo database, where the prices are coherent.
     *
     * Use this constructor in the client side.
     *
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param noCoherenceChecks If true, skip the price coherence checks (discouraged if not loading from the demo database or client side).
     *
     * @throws IllegalArgumentException If the price is not valid for a stop order and no coherence checks is false.
     * @throws NullPointerException If any of the parameters are null.
     * @throws IllegalStateException If the order book with the given currencies to be used to check the price coherence has not been found.
     *
     * */
    public StopOrder(SpecificPrice price, Quantity quantity, Boolean noCoherenceChecks) throws NullPointerException, IllegalArgumentException, IllegalStateException {

        // Synchronization in the super (but not needed).

        super(OrderType.STOP, quantity, price);

        // Null check.
        if (noCoherenceChecks == null) {
            throw new NullPointerException("No coherence checks flag in the stop order creation cannot be null.");
        }

        if (noCoherenceChecks == false) {
            checkPriceCoherence(price);
        }

    }

}
