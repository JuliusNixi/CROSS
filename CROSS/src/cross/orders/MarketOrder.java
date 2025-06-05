package cross.orders;

import cross.orderbook.OrderBook;
import cross.types.Currency;
import cross.types.Quantity;
import cross.types.price.PriceType;
import cross.types.price.SpecificPrice;

/**
 *
 * MarketOrder class represents a market order in the system.
 *
 * A market order is executed at the best available price in the market.
 * The best prices come from the OrderBook class.
 *
 * It extends the Order class.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Order
 *
 * @see PriceType
 * @see Quantity
 * @see Currency
 * @see SpecificPrice
 * 
 * @see OrderBook
 *
 * */
public class MarketOrder extends Order {

    private transient SpecificPrice executionPrice = null;
    private transient Long comingFromStopOrderId = null;

    // Since here, differently from LimitOrder and StopOrder, we don't have a price value to set in the constructor, we set it to a placeholder.
    // It will not be sent through the APIs by the client thanks to the CreationRequest class.
    // So in the constructor we request the type of the order and the currencies of the order instead of the whole SpecificPrice object.
    /**
     *
     * Constructor for the class.
     * It creates a new market order with the given type, quantity and currencies.
     *
     * ATTENTION: Here the logic is different from the LimitOrder.
     * Is reversed, because if it's a buy order it will HIT the sell orders on the limit book (red ones) and viceversa.
     *
     * @param type Type of the order (ASK or BID).
     * @param quantity The quantity of the order.
     * @param primaryCurrency The primary currency of the order.
     * @param secondaryCurrency The secondary currency of the order.
     *
     * @throws NullPointerException If the type, quantity, primary currency or secondary currency are null.
     *
     */
    public MarketOrder(PriceType type, Currency primaryCurrency, Currency secondaryCurrency, Quantity quantity) throws NullPointerException {

        // It will be replaced by the actual price of the market in the server side.
        // This price is a placeholder.
        super(OrderType.MARKET, quantity, new SpecificPrice(1, type, primaryCurrency, secondaryCurrency));

        // Null check.
        if (type == null) {
            throw new NullPointerException("Price type of a market order cannot be null.");
        }
        if (primaryCurrency == null) {
            throw new NullPointerException("Primary currency of a market order cannot be null.");
        }
        if (secondaryCurrency == null) {
            throw new NullPointerException("Secondary currency of a market order cannot be null.");
        }

    }
    
    // GETTERS
    @Override
    /**
     *
     * The market order is create with a type like ASK or BID.
     * But, the price at which the order is executed is the best price of the market, OPPOSITE to the indicated type.
     * So an ASK market order will be executed at the best BID price and viceversa.
     * To avoid confusion, and prevent behaviours like:
     * order.getPrice().getValue() that would return a placeholder price value (1) and not the possibly intended execution price value.
     * order.getPrice().getType() that would return the type of the order as desired by the creator and not the possibly intended execution price type.
     * And since in Java methods can't be hidden, I decide to override the getPrice method and throw an exception.
     * 
     * @throws RuntimeException Always, since the price is not available.
     *
     */
    public SpecificPrice getPrice() throws RuntimeException {

        throw new RuntimeException("Market order price is not available, use getExecutionPrice() and getMarketOrderPriceType() instead.");
    
    }
    /**
     *
     * Getter for the market order price type. The type of the market order is the opposite of the execution price type. It's the type of the order as desired by the creator.
     *
     * @return The market order price type as a PriceType object.
     *
     */
    public PriceType getMarketOrderPriceType() {

        return super.getPrice().getType();

    }
    /**
     *
     * Getter for the market order primary currency.
     *
     * @return The market order primary currency as a Currency object.
     *
     */
    public Currency getMarketOrderPrimaryCurrency() {

        return super.getPrice().getPrimaryCurrency();

    }
    /**
     *
     * Getter for the market order secondary currency.
     *
     * @return The market order secondary currency as a Currency object.
     *
     */
    public Currency getMarketOrderSecondaryCurrency() {

        return super.getPrice().getSecondaryCurrency();

    }
    /**
     *
     * Getter for the execution price. The execution price is the price at which the order is executed. It's the best price of the order book. It's the opposite of the market order price type.
     *
     * @return The execution price of the order as a SpecificPrice object.
     *
     */
    public SpecificPrice getExecutionPrice() {

        return executionPrice;

    }
    /**
     *
     * Getter for the coming from stop order id of the market order.
     *
     * @return The coming from stop order id of the market order.
     *
     */
    public Long getComingFromStopOrderId() {

        return comingFromStopOrderId;
        
    }

    // SETTERS
    @Override
    /**
     * 
     * The market order is create with a type like ASK or BID.
     * But, the price at which the order is executed is the best price of the market, OPPOSITE to the indicated type.
     * So an ASK market order will be executed at the best BID price and viceversa.
     * To avoid confusion, and prevent behaviours like:
     * order.setPrice() where can't tell what price is set, the execution price or the market order initial price type.
     * And since in Java methods can't be hidden, I decide to override the setPrice method and throw an exception.
     * 
     * @throws RuntimeException Always, use setExecutionPrice() instead.
     *
     */
    public synchronized void setPrice(SpecificPrice price) throws RuntimeException {

        throw new RuntimeException("Market order price cannot be set with setPrice(), use setExecutionPrice() instead.");

    }
    /**
     *
     * Sets the execution price of the market order.
     *
     * Synchronized to avoid multi-threads problems.
     *
     * @param executionPrice The new execution price of the market order.
     *
     * @throws NullPointerException If the new execution price of the market order is null.
     * @throws IllegalArgumentException If the new execution price has a primary or secondary currency that doesn't match with the currencies given in its creation.
     *
     * */
    public synchronized void setExecutionPrice(SpecificPrice executionPrice) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (executionPrice == null) {
            throw new NullPointerException("Execution price to set on a MARKET order cannot be null.");
        }

        // Currencies check.
        if (executionPrice.getPrimaryCurrency().compareTo(this.getMarketOrderPrimaryCurrency()) != 0 || executionPrice.getSecondaryCurrency().compareTo(this.getMarketOrderSecondaryCurrency()) != 0) {
            throw new IllegalArgumentException("The execution price to set on a MARKET order has a primary or secondary currency that doesn't match with the currencies given in its creation.");
        }

        // Synchronized on the price object is not needed, since it cannot be changed (has no setters).
        this.executionPrice = executionPrice;

    }
    /**
     *
     * Sets (updates) the execution price of the market order to the actual price of the market, ask or bid based on the type of the price.
     * Used to update the market order's execution price to the actual price of the market before executing it in the server side.
     *
     * Synchronized to avoid concurrency threads issues.
     * Synchornized on the order book to avoid changes in the best prices while setting the price of the market order.
     *
     * @throws IllegalStateException If the actual (best) prices (ask or bid) of the order book are null or the order book with the given price currencies is not found.
     *
     */
    public synchronized void setUpdatedExecutionPrice() throws IllegalStateException {

        // Getting the order book.
        OrderBook orderBook = OrderBook.getOrderBookByCurrencies(this.getMarketOrderPrimaryCurrency(), this.getMarketOrderSecondaryCurrency());

        if (orderBook == null) {
            orderBook = OrderBook.getMainOrderBook();
        }

        if (orderBook == null) {
            throw new IllegalStateException("The order book with the given currencies to be used to set the updated execution price of a market order has not been found. Create it before by setting a best ask or best bid price with these currencies by executing some orders.");
        }

        synchronized (orderBook) {

            // Check if the current market prices are valid.
            if ((orderBook.getActualPriceAsk() == null && this.getMarketOrderPriceType() == PriceType.BID) || (orderBook.getActualPriceBid() == null && this.getMarketOrderPriceType() == PriceType.ASK)) {
                String inverseAskOrBid = this.getMarketOrderPriceType() == PriceType.ASK ? "ask" : "bid";
                throw new IllegalStateException(String.format("Null best %s price of the %s/%s order book to be set on a market order.", inverseAskOrBid.toUpperCase(), orderBook.getPrimaryCurrency().name().toUpperCase(), orderBook.getSecondaryCurrency().name().toUpperCase()));
            }

            // Set the execution price of the order to the actual OPPOSITE price of the market.
            if (this.getMarketOrderPriceType() == PriceType.ASK) {
                this.setExecutionPrice(orderBook.getActualPriceBid());
            } else {
                this.setExecutionPrice(orderBook.getActualPriceAsk());
            }

        }

    }
    /**
     *
     * Sets the coming from stop order id of the market order.
     *
     * @param comingFromStopOrderId The coming from stop order id to be set on the market order.
     *
     * @throws NullPointerException If the coming from stop order id is null.
     *
     */
    public synchronized void setComingFromStopOrderId(Long comingFromStopOrderId) throws NullPointerException {

        // Null check.
        if (comingFromStopOrderId == null) {
            throw new NullPointerException("Coming from, stop order id, to be set on a market order cannot be null.");
        }

        this.comingFromStopOrderId = comingFromStopOrderId;

    }

    @Override
    public synchronized String toString() {

        String timestamp = this.getTimestamp() == null ? "null" : this.getTimestamp().toString();
        String id = this.getId() == null ? this.getId().toString() : this.getId().toString();
        String user = this.getUser() == null ? "null" : this.getUser().toString();
        String executionPrice = this.getExecutionPrice() == null ? "null" : this.getExecutionPrice().toString();
        String comingFromStopOrderId = this.getComingFromStopOrderId() == null ? "null" : this.getComingFromStopOrderId().toString();
        String marketOrderPriceType = this.getMarketOrderPriceType() == null ? "null" : this.getMarketOrderPriceType().toString();
        String initialSize = this.getInitialFixedSize() == null ? "null" : this.getInitialFixedSize().toString();

        return String.format("Order's Type [%s] - ID [%s] - User [%s] - Execution Price [%s] - Quantity [%s] - Timestamp [%s]  - Initial Size [%s] - Market Order Price Type [%s] - Coming From Stop Order ID [%s]", this.getClass().getSimpleName(), id, user, executionPrice, this.getQuantity().toString(), timestamp, initialSize, marketOrderPriceType, comingFromStopOrderId);

    }

}
