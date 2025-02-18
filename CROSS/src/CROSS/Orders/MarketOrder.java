package CROSS.Orders;

import CROSS.OrderBook.Market;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;

/**
 * 
 * MarketOrder class represents a market order in the system.
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

    // Used in the setUpdatedPrice method to get ask or bid price.
    private final PriceType type;

    /**
     * 
     * Constructor for the class.
     * 
     * ATTENTION: Here the logic is different from the LimitOrder.
     * Is reversed because if it's a buy order it will HIT the sell orders on the book (red ones) and viceversa.
     * 
     * @param market Market where the order is placed.
     * @param type Type of the order (ASK or BID).
     * @param quantity Quantity of the order.
     * @param user User that placed the order.
     * 
     * @throws NullPointerException If the price type is null.
     * @throws RuntimeException If the actual price of the market is invalid.
     * 
     */
    public MarketOrder(Market market, PriceType type, Quantity quantity, User user) throws NullPointerException, RuntimeException {
        
        // Null check.
        if (type == null) {
            throw new NullPointerException("Price type of a market order cannot be null");
        }

        this.type = type;

        // Need a placeholder price to create the order by calling the super constructor.
        // It will be replaced by the actual price of the market below.
        SpecificPrice placeholderPrice = new SpecificPrice(1, type, market);
        super(market, placeholderPrice, quantity, user);

        // Set the price to the actual price of the market.
        this.setUpdatedPrice();

    }

    // SETTERS
    /**
     * 
     * Set the price of the order to the actual price of the market.
     * Used to update the order's price to the actual price of the market.
     * 
     * @throws RuntimeException If the actual price of the market is invalid.
     * 
     */
    public void setUpdatedPrice() throws RuntimeException {

        // Set the price to the actual price of the market.
        SpecificPrice price;
        if (this.type == PriceType.ASK) {
            price = super.getMarket().getActualPriceAsk();
        } else {
            price = super.getMarket().getActualPriceBid();
        }

        // Check if the price is valid.
        if (price == null) {
            throw new RuntimeException("Invalid actual price of the market to be used for a Market Order.");
        }

        // Exception handling not needed because the price is valid.
        super.setPrice(price);

    }

}
