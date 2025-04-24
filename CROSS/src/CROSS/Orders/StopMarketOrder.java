package CROSS.Orders; 

import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;

/**
 * 
 * A stop market order class represents a stop order in the system.
 * 
 * A stop order is converted into a market order when the stop price is reached.
 * 
 * It extends the Order class.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Order
 * 
 * @see Market
 * @see SpecificPrice
 * @see Quantity
 * @see User
 * 
 * */
public class StopMarketOrder extends Order {

    /**
     * 
     * Check if the price given for the stop market order creation is coherent with the market actual prices.
     * 
     * Private method used in the constructor.
     * 
     * Synchronized on the market object to avoid best prices changes while checking the coherence.
     * 
     * @param price The price of the stop market order.
     * 
     * @throws IllegalArgumentException If the price is not valid for a stop market order.
     * 
     */
    private void checkPriceCoherence(SpecificPrice price) throws IllegalArgumentException {

        Market market = price.getMarket();

        synchronized (market) {

            // Price coherence / order type checks.
            if (market.getActualPriceAsk() != null && price.getType() == PriceType.ASK && price.getValue() >= market.getActualPriceAsk().getValue()) {
                throw new IllegalArgumentException("The ASK price to use to SELL with a STOP is greater-equal than the best ASK price in the market.");
            }
            if (market.getActualPriceBid() != null && price.getType() == PriceType.BID && price.getValue() <= market.getActualPriceBid().getValue()) {
                throw new IllegalArgumentException("The BID price to use to BUY with a STOP is lower-equal than the best BID price in the market.");
            }

        }

    }

    /**
     * 
     * Constructor for the class.
     * It creates a new stop market order with the given price, quantity and user.
     * 
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param user The user who placed the order.
     * 
     * @throws IllegalArgumentException If the price of the order is not valid for a stop market order.
     * @throws NullPointerException If the price, the quantity or the user are null.
     * 
     * */
    public StopMarketOrder(SpecificPrice price, Quantity quantity, User user) throws IllegalArgumentException, NullPointerException {

        // Synchronization in the super.

        super(price, quantity, user);
        
        checkPriceCoherence(price);
        
    }
    /**
     * 
     * Alternative constructor for the class.
     * It creates a new stop market order with the given price, quantity and user.
     * 
     * It also has a no coherence checks flag to skip the price coherence checks, used to load the orders from the demo database.
     * 
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param user The user who placed the order.
     * @param noCoherenceChecks If true, skip the price coherence checks.
     * 
     * @throws IllegalArgumentException If the price is not valid for a stop market order and noCoherenceChecks is false.
     * @throws NullPointerException If any of the parameters are null.
     * 
     * */
    public StopMarketOrder(SpecificPrice price, Quantity quantity, User user, Boolean noCoherenceChecks) throws NullPointerException, IllegalArgumentException {
        
        // Synchronization in the super.

        super(price, quantity, user);

        // Null check.
        if (noCoherenceChecks == null) {
            throw new NullPointerException("No coherence checks flag in the stop market order creation cannot be null.");
        }

        if (noCoherenceChecks == false) {
            checkPriceCoherence(price);
        }

    }

}
