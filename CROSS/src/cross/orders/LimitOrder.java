package cross.orders;

import cross.orderbook.OrderBook;
import cross.types.Quantity;
import cross.types.price.PriceType;
import cross.types.price.SpecificPrice;

/**
 *
 * LimitOrder class represents a limit order in the system.
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
public class LimitOrder extends Order {
    
    /**
     *
     * Check if the price given for the limit order creation is coherent with the market actual prices if used server side.
     *
     * Private method used in the constructor only.
     * 
     * Synchronized on the order book object to avoid best prices changes while checking the coherence.
     *
     * @param price The price of the limit order.
     *
     * @throws IllegalArgumentException If the price is not valid for a limit order.
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

            // Price coherence / order type (ask / bid) checks.
            /*
            *
            * The ASK are prices at which the market (someone) is willing to sell.
            * Are called ASK because I can ask (buy) at that price with a market order.
            * So inserting an ASK (sell) limit order lower than the bid ask DOESN'T make sense.
            *
            * ASK
            * ASK
            * ASK
            * BEST ASK
            * --------
            * BEST BID
            * BID
            * BID
            * BID
            *
            *
            */
            // Check if the price is valid for a limit order.
            if (price.getType() == PriceType.ASK && orderBook.getActualPriceBid() != null && price.getValue() <= orderBook.getActualPriceBid().getValue()) {
                throw new IllegalArgumentException("The ASK price to use to SELL with a LIMIT is lower-equal than the best BID price in the market.");
            }
            if (price.getType() == PriceType.BID && orderBook.getActualPriceAsk() != null && price.getValue() >= orderBook.getActualPriceAsk().getValue()) {
                throw new IllegalArgumentException("The BID price to use to BUY with a LIMIT is higher-equal than the best ASK price in the market.");
            }

        }

    }

    // CONSTRUCTORS
    /**
     *
     * Constructor for the class.
     * It creates a new limit order with the given price and quantity.
     * 
     * It performs a price coherence check, not to be used in the client side.
     *
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     *
     * @throws IllegalArgumentException If the price is not valid for a limit order.
     * @throws IllegalStateException If the order book with the given currencies to be used to check the price coherence has not been found.
     * @throws NullPointerException If the price or quantity are null.
     *
     * */
    public LimitOrder(SpecificPrice price, Quantity quantity) throws IllegalArgumentException, NullPointerException, IllegalStateException {

        // Synchronization in the super (but not needed).

        this(price, quantity, false);

    }

    /**
     *
     * Alternative constructor for the class.
     * It creates a new limit order with the given price and quantity.
     *
     * It also has a no coherence checks flag to skip the price coherence checks, used to load the orders from the demo database, where the prices are coherent.
     * 
     * Use this constructor in the client side.
     *
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param noCoherenceChecks If true, skip the price coherence checks (discouraged if not loading from the demo database or client side).
     *
     * @throws IllegalArgumentException If the price is not valid for a limit order and no coherence checks is false.
     * @throws NullPointerException If any of the parameters are null.
     * @throws IllegalStateException If the order book with the given currencies to be used to check the price coherence has not been found.
     *
     * */
    public LimitOrder(SpecificPrice price, Quantity quantity, Boolean noCoherenceChecks) throws NullPointerException, IllegalArgumentException, IllegalStateException {

        // Synchronization in the super (but not needed).

        super(OrderType.LIMIT, quantity, price);

        // Null check.
        if (noCoherenceChecks == null) {
            throw new NullPointerException("No coherence checks flag in the limit order creation cannot be null.");
        }

        if (noCoherenceChecks == false) {
            checkPriceCoherence(price);
        }

    }

}
