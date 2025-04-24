package CROSS.Orders;

import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;

/**
 * 
 * MarketOrder class represents a market order in the system.
 * 
 * A market order is executed at the best available price in the market.
 * 
 * It extends the Order class.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Order
 * 
 * @see Market
 * @see PriceType
 * @see Quantity
 * @see User
 * 
 * */
public class MarketOrder extends Order {

    // Used in the setUpdatedPrice() below method to get best ask or best bid price.
    // The type of the order, so if it's a buy or a sell order.
    private final PriceType type;

    // Since here, differently from LimitOrder and StopMarketOrder, we don't have a price to set in the constructor, we need the market and the type to get te best price.
    /**
     * 
     * Constructor for the class.
     * 
     * ATTENTION: Here the logic is different from the LimitOrder.
     * Is reversed, because if it's a buy order it will HIT the sell orders on the limit book (red ones) and viceversa.
     * 
     * @param type Type of the order (ASK or BID).
     * @param quantity Quantity of the order.
     * @param user User that placed the order.
     * 
     * @throws NullPointerException If any of the parameters are null.
     * 
     */
    public MarketOrder(Market market, PriceType type, Quantity quantity, User user) throws NullPointerException {
        
        // User synchronized in the super.
        // Need a placeholder price to call the super constructor.
        // It will be replaced by the actual price of the market below.
        super(new SpecificPrice(1, type, market), quantity, user);

        // Null check.
        if (type == null) {
            throw new NullPointerException("Price type of a market order cannot be null.");
        }

        this.type = type;

        // Set the price to the actual price of the market.
        // Synchronized on the market in the called method.
        this.setUpdatedPrice(market);

    }

    // SETTERS
    /**
     * 
     * Set the price of the order to the actual price of the market, ask or bid based on the type of the order.
     * Used to update the order's price to the actual price of the market.
     * 
     * It's called in the constructor to set the price to the actual price of the market but can be called also from different places at different times.
     * 
     * Synchronized to avoid concurrency threads issues.
     * Synchornized on the market to avoid changes in the market while setting the price.
     * 
     * @param market Market to get the actual (best) price from.
     * 
     * @throws RuntimeException If the actual (best) prices (ask or bid) of the market are null.
     * @throws NullPointerException If the market is null.
     * 
     */
    public synchronized void setUpdatedPrice(Market market) throws RuntimeException, NullPointerException {

        // Null check.
        if (market == null) {
            throw new NullPointerException("Market to be used to update the price of a market order cannot be null.");
        }

        synchronized (market) {

            // Check if the current market prices are valid.
            if (market.getActualPriceAsk() == null || market.getActualPriceBid() == null) {
                throw new RuntimeException(String.format("Null best %s price of the %s/%s market to be set on a market order.", this.type.name().toUpperCase(), market.getPrimaryCurrency().name().toUpperCase(), market.getSecondaryCurrency().name().toUpperCase()));
            }

            // Set the price of the order to the actual price of the market.
            // So to the best ask or bid price based on the type of the order.
            // The value here is just a placehoolder.
            SpecificPrice price = new SpecificPrice(1, this.type, market);
            if (this.type == PriceType.ASK) {
                price = new SpecificPrice(market.getActualPriceAsk().getValue(), this.type, market);
            } else {
                price = new SpecificPrice(market.getActualPriceBid().getValue(), this.type, market);
            }

            // Exception handling not needed because the price is valid.
            super.setPrice(price);

        }

    }

    @Override
    public SpecificPrice getPrice() {

        // The price of the order is updated to the actual price of the market in this getter, no need to do it manually.
        this.setUpdatedPrice(super.getPrice().getMarket());

        return super.getPrice();
        
    }

}
