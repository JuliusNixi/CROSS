package CROSS.Orders; 

import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;

/**
 * 
 * LimitOrder class represents a limit order in the system. 
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
public class LimitOrder extends Order {

    /**
     * 
     * Check if the price given for the limit order creation is coherent with the market actual prices.
     * 
     * Private method used in the constructor.
     * 
     * Synchronized on the market object to avoid best prices changes while checking the coherence.
     * 
     * @param price The price of the limit order.
     * 
     * @throws IllegalArgumentException If the price is not valid for a limit order.
     * 
     */
    private void checkPriceCoherence(SpecificPrice price) throws IllegalArgumentException {

        Market market = price.getMarket();

        synchronized (market) {

            // Price coherence / order type checks.
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
            if (price.getType() == PriceType.ASK && market.getActualPriceBid() != null && price.getValue() < market.getActualPriceBid().getValue()) {
                throw new IllegalArgumentException("The ASK price to use to SELL with a LIMIT is lower than the best BID price in the market.");
            }
            if (price.getType() == PriceType.BID && market.getActualPriceAsk() != null && price.getValue() > market.getActualPriceAsk().getValue()) {
                throw new IllegalArgumentException("The BID price to use to BUY with a LIMIT is higher than the best ASK price in the market.");
            }

        }

    }

    /**
     * 
     * Constructor for the class.
     * It creates a new limit order with the given price, quantity and user.
     * 
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param user The user who placed the order.
     * 
     * @throws IllegalArgumentException If the price is not valid for a limit order.
     * @throws NullPointerException If any of the parameters are null.
     * 
     * */
    public LimitOrder(SpecificPrice price, Quantity quantity, User user) throws IllegalArgumentException, NullPointerException {
        
        // Synchronization in the super.

        super(price, quantity, user);
        
        checkPriceCoherence(price);

    } 
    
     /**
     * 
     * Alternative constructor for the class.
     * It creates a new limit order with the given price, quantity and user.
     * 
     * It also has a no coherence checks flag to skip the price coherence checks, used to load the orders from the demo database.
     * 
     * @param price The price of the order.
     * @param quantity The quantity of the order.
     * @param user The user who placed the order.
     * @param noCoherenceChecks If true, skip the price coherence checks.
     * 
     * @throws IllegalArgumentException If the price is not valid for a limit order and noCoherenceChecks is false.
     * @throws NullPointerException If any of the parameters are null.
     * 
     * */
    public LimitOrder(SpecificPrice price, Quantity quantity, User user, Boolean noCoherenceChecks) throws NullPointerException, IllegalArgumentException {

        // Synchronization in the super.

        super(price, quantity, user);

        // Null check.
        if (noCoherenceChecks == null) {
            throw new NullPointerException("No coherence checks flag in the limit order creation cannot be null.");
        }

        if (noCoherenceChecks == false) {
            checkPriceCoherence(price);
        }

    }

}
