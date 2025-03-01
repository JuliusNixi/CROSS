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

    /**
     * 
     * Constructor for the class.
     * 
     * ATTENTION: Here the logic is different from the LimitOrder.
     * Is reversed, because if it's a buy order it will HIT the sell orders on the book (red ones) and viceversa.
     * 
     * @param type Type of the order (ASK or BID).
     * @param quantity Quantity of the order.
     * @param user User that placed the order.
     * 
     * @throws NullPointerException If any of the parameters are null.
     * @throws RuntimeException If the market has a null actual price ask or bid and the order is an ask or bid respectively.
     * 
     */
    public MarketOrder(Market market, PriceType type, Quantity quantity, User user) throws NullPointerException, RuntimeException {
        
        // Null check.
        if (type == null) {
            throw new NullPointerException("Price type of a market order cannot be null.");
        }

        this.type = type;

        // Need a placeholder price to call the super constructor.
        // It will be replaced by the actual price of the market below.
        SpecificPrice placeholderPrice = new SpecificPrice(1, type, market);
        super(placeholderPrice, quantity, user);

        // Set the price to the actual price of the market.
        this.setUpdatedPrice();

    }

    // SETTERS
    /**
     * 
     * Set the price of the order to the actual price of the market, ask or bid based on the type of the order.
     * Used to update the order's price to the actual price of the market.
     * 
     * Synchronized to avoid concurrency threads issues.
     * 
     * @throws RuntimeException If the actual price (ask or bid) of the market is null.
     * 
     */
    public synchronized void setUpdatedPrice() throws RuntimeException {

        // Set the price to the actual price of the market.
        // So to the best ask or bid price based on the type of the order.
        SpecificPrice price;
        if (this.type == PriceType.ASK) {
            price = super.getMarket().getActualPriceAsk();
        } else {
            price = super.getMarket().getActualPriceBid();
        }

        // Check if the price is valid.
        if (price == null) {
            throw new RuntimeException(String.format("Null actual (best) %s price of the market to be set on a market order.", this.type.name()));
        }

        // Exception handling not needed because the price is valid.
        super.setPrice(price);

    }

}
