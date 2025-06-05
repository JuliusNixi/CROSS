package cross.api.requests.orders;

import cross.orders.MarketOrder;
import cross.orders.Order;
import cross.orders.OrderType;
import cross.types.Currency;
import cross.types.Quantity;
import cross.types.price.PriceType;
import cross.types.price.SpecificPrice;

/**
 *
 * CreateRequest is a class used to rapresent the client's API requests about creating orders.
 * 
 * It's used as values in the Request object.
 *
 * It contains the type (ask / bid) and the size of the order and the price, this latter only if the order is not a market order.
 * It also contains the primary and secondary currency of the price as transient fields since they are not needed to be serialized but to wrap the price back into the correct object in the getter.
 * 
 * It's used as values in the Request object.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see cross.orders.Order
 * 
 * @see cross.types.price.PriceType
 * @see cross.types.Quantity
 * 
 * @see cross.orders.OrderType
 * 
 * @see cross.types.Currency
 *
 * @see cross.types.price.SpecificPrice
 * 
 * @see Request
 *
 */
public class CreateRequest {

    // Type is ask or bid.
    private final String type;
    private final Integer size;
    protected Integer price;
    private final transient Currency pricePrimaryCurrency;
    private final transient Currency priceSecondaryCurrency;

    /**
     *
     * Constructor of the class.
     *
     * @param order The order to get the type, the size and the price from.
     *
     * @throws NullPointerException If the order is null.
     *
     */
    public CreateRequest(Order order) throws NullPointerException {

        // Null check.
        if (order == null) {
            throw new NullPointerException("The order in the order request cannot be null.");
        }

        this.size = order.getQuantity().getValue();
        if (order.getOrderType() != OrderType.MARKET) {
            this.price = order.getPrice().getValue();
            this.type = order.getPrice().getType().name().toLowerCase();
            this.pricePrimaryCurrency = order.getPrice().getPrimaryCurrency();
            this.priceSecondaryCurrency = order.getPrice().getSecondaryCurrency();
        } else {
            this.price = null;
            this.type = ((MarketOrder) order).getMarketOrderPriceType().name().toLowerCase();
            this.pricePrimaryCurrency = ((MarketOrder) order).getMarketOrderPrimaryCurrency();
            this.priceSecondaryCurrency = ((MarketOrder) order).getMarketOrderSecondaryCurrency();
        }

    }
    
    // GETTERS
    /**
     *
     * Getter for the type (ask / bid) of the order.
     *
     * @return The type of the order as PriceType enum.
     *
     */
    public PriceType getType() {

        return PriceType.valueOf(this.type.toUpperCase());

    }
    /**
     *
     * Getter for the size of the order.
     *
     * @return The size of the order as Quantity object.
     *
     */
    public Quantity getSize() {

        return new Quantity(this.size);

    }
    /**
     *
     * Getter for the price of the order.
     * 
     * The price is returned only if the order is not a market order, otherwise it is returned as null.
     *
     * @return The price of the order as SpecificPrice object or null if the order is a market order.
     *
     */
    public SpecificPrice getPrice() {

        if (this.price == null) {
            return null;
        }
        return new SpecificPrice(this.price, this.getType(), this.pricePrimaryCurrency, this.priceSecondaryCurrency);

    }

    @Override
    public String toString() {

        return String.format("CreateRequest [Type [%s] - Size [%s] - Price [%s]]", this.type, this.size, this.price);

    }

}
