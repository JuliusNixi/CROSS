package cross.api.requests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import cross.api.JSONAPIMessage;
import cross.api.requests.orders.CancelRequest;
import cross.api.requests.orders.CreateRequest;
import cross.api.requests.pricehistory.PriceHistoryRequest;
import cross.api.requests.user.LogoutRequest;
import cross.api.requests.user.RegisterLoginRequest;
import cross.api.requests.user.UpdateCredentialsRequest;
import cross.exceptions.InvalidUser;
import cross.orderbook.OrderBook;
import cross.orders.LimitOrder;
import cross.orders.MarketOrder;
import cross.orders.Order;
import cross.orders.StopOrder;
import cross.types.Currency;
import cross.types.Quantity;
import cross.types.price.GenericPrice;
import cross.types.price.PriceType;
import cross.types.price.SpecificPrice;
import cross.users.User;
import cross.utils.ClientActionsUtils;
import cross.utils.ClientActionsUtils.ClientActions;

/**
 *
 * This class is responsible for creating a request Java object.
 * It's rapresents a JSON request string that will be sent to the server from the client through the TCP socket.
 *
 * It extends the JSONAPIMessage class to use the toJSONString conversion method.
 *
 * It contains the operation, that is the action (request's type) requested by the client.
 * The operation is a string to be encoded in JSON.
 * But, it's created through the constructor with the ClientActions enum.
 *
 * It also contains the values, that are the arguments for the operation. These values are request dependent.
 * So, they are rapresented as a generic Object.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see JSONAPIMessage
 * @see ClientActionsUtils.ClientActions
 * 
 * @see cross.api.requests.user.RegisterLoginRequest
 * @see cross.api.requests.user.LogoutRequest
 * @see cross.api.requests.user.UpdateCredentialsRequest
 * 
 * @see cross.api.requests.orders.CreateRequest
 * @see cross.api.requests.orders.CancelRequest
 * 
 * @see cross.api.requests.pricehistory.PriceHistoryRequest
 * 
 * @see cross.orders.LimitOrder
 * @see cross.orders.Order
 * @see cross.orders.MarketOrder
 * @see cross.orders.StopOrder
 * 
 * @see cross.orderbook.OrderBook
 * 
 */
public class Request extends JSONAPIMessage {

    // Keys for the JSON object as specified in the protocol in the assignment's text.
    private final String operation;
    private final Object values;

    /**
     *
     * Constructor of the class.
     *
     * @param action The action requested by the client as a ClientActions enum. Will be converted to a string.
     * @param values The values for the action as generic Object. Values are request dependent. Values can be null, for example for the EXIT request.
     *
     * @throws NullPointerException If the action object is null.
     *
     */
    public Request(ClientActionsUtils.ClientActions action, Object values) throws NullPointerException {

        // Null check.
        if (action == null)
            throw new NullPointerException("Action in the request cannot be null.");

        // Get the (NO TO LOWERCASE, BUT AS WRITTEN IN THE MAP AND IN THE ASSIGNMENT'S TEXT) string command from the action.
        String command = ClientActionsUtils.getKeywordCommand(action);

        this.operation = command;
        this.values = values;

    }
    /**
     *
     * Alternative constructor of the class.
     * 
     * Used to parse the received JSON request string in the ClientThread class.
     * Must be called only server side.
     *
     * @param JSONrequest The JSON request string.
     *
     * @throws NullPointerException If the JSON request string is null.
     * @throws IllegalArgumentException If the JSON request string is not a valid JSON object or the content is invalid.
     * @throws IllegalStateException If the registered string commands associated with the client actions enum are invalid.
     * @throws InvalidUser If the user credentials in the JSON request are not valid, too short, too long or with invalid characters and ignore invalid user is true.
     *
     */
    public Request(String JSONrequest, Boolean ignoreInvalidUser) throws NullPointerException, IllegalArgumentException, IllegalStateException, InvalidUser {

        // Null check.
        if (JSONrequest == null)
            throw new NullPointerException("JSON request string in the request cannot be null.");
        if (ignoreInvalidUser == null)
            throw new NullPointerException("Ignore invalid user flag in the request cannot be null.");

        JsonObject jsonObject = null;
        try {
            jsonObject = JsonParser.parseString(JSONrequest).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException ex) {
            throw new IllegalArgumentException("JSON request string in the request is not a valid JSON object.");
        }

        // Get the operation.
        String operation;
        try {
            operation = jsonObject.get("operation").getAsString();
            if (operation == null || operation.isEmpty() || operation.isBlank()) throw new UnsupportedOperationException();
        } catch (UnsupportedOperationException | IllegalStateException | NullPointerException ex) {
            throw new IllegalArgumentException("The operation in the JSON request is not valid.");
        }
        // Validate the operation.
        ClientActions action;
        try {
            action = ClientActionsUtils.actionFromString(operation);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("The operation in the JSON request is not valid.");
        } catch (IllegalStateException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
        this.operation = operation;

        // Get the values.
        JsonElement elementValues;
        try {
            elementValues = jsonObject.get("values");
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException("The values in the JSON request are not valid.");
        }

        // Parse the values.
        String username;
        String password;
        // Use OrderBook is ok since this method should be called only server side.
        OrderBook orderBook = OrderBook.getOrderBookByCurrencies(Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());
        switch (action) {
            case LOGIN, REGISTER -> {
                User user = null; 
                try {
                    username = elementValues.getAsJsonObject().get("username").getAsString();
                    password = elementValues.getAsJsonObject().get("password").getAsString();
                } catch (IllegalStateException | NullPointerException | UnsupportedOperationException ex) {
                    throw new InvalidUser("The username or password in the JSON request are not valid.");
                }
                try {
                    user = ClientActionsUtils.getUserFromString(username, password);
                } catch (IllegalArgumentException ex) {
                    if (ignoreInvalidUser == true) {
                        user = new User(username, password, true);
                    } else {
                        throw new InvalidUser("The username or password in the JSON request are not valid.");
                    }
                }
                this.values = new RegisterLoginRequest(user);
            }
            case UPDATE_CREDENTIALS -> {
                String newPassword;
                User userOld = null; 
                User userNew = null;
                try {
                    username = elementValues.getAsJsonObject().get("username").getAsString();
                    password = elementValues.getAsJsonObject().get("old_password").getAsString();
                    newPassword = elementValues.getAsJsonObject().get("new_password").getAsString();
                } catch (IllegalStateException | NullPointerException | UnsupportedOperationException ex) {
                    throw new InvalidUser("The username or the new password or the old password in the JSON request are not valid.");
                }
                try {
                    userOld = ClientActionsUtils.getUserFromString(username, password);
                    userNew = ClientActionsUtils.getUserFromString(username, newPassword);
                } catch (IllegalArgumentException ex) {
                    if (ignoreInvalidUser == true) {
                        userOld = new User(username, password, true);
                        userNew = new User(username, newPassword, true);
                    } else {
                        throw new InvalidUser("The username or the new password or the old password in the JSON request are not valid.");
                    }
                }
                try {
                    this.values = new UpdateCredentialsRequest(userOld, userNew);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("New user username in the JSON request is not equals to the old one.");
                }
            }
            case LOGOUT -> this.values = new LogoutRequest();
            case CANCEL_ORDER -> {
                Number orderId;
                try {
                    orderId = elementValues.getAsJsonObject().get("orderId").getAsNumber();
                    orderId = ClientActionsUtils.getOrderIDFromString(orderId.toString());
                } catch (IllegalStateException | NullPointerException | UnsupportedOperationException ex) {
                    throw new IllegalArgumentException("The order id in the JSON request is not valid.");
                }
                this.values = new CancelRequest(orderId);
            }
            case GET_PRICE_HISTORY -> {
                String month;
                try {
                    month = elementValues.getAsJsonObject().get("month").getAsString();
                    ClientActionsUtils.parseMonthFromString(month);
                    this.values = new PriceHistoryRequest(month);
                } catch (IllegalStateException | NullPointerException | UnsupportedOperationException | IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The month in the JSON request is not valid.");
                }
            }
            case INSERT_LIMIT_ORDER -> {

                String typeStr;
                PriceType type;
                try {
                    typeStr = elementValues.getAsJsonObject().get("type").getAsString();
                    type = ClientActionsUtils.getPriceTypeFromString(typeStr);
                } catch (IllegalStateException | NullPointerException | UnsupportedOperationException | IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The type of the limit order in the JSON request is not valid.");
                }

                String sizeStr;
                Quantity size;
                try {
                    sizeStr = elementValues.getAsJsonObject().get("size").getAsString();
                    size = ClientActionsUtils.getSizeFromString(sizeStr);
                } catch (IllegalStateException | NullPointerException | UnsupportedOperationException | IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The size of the limit order in the JSON request is not valid.");
                }

                String priceStr;
                GenericPrice price;
                SpecificPrice specificPrice;
                try {
                    priceStr = elementValues.getAsJsonObject().get("price").getAsString();
                    price = ClientActionsUtils.getPriceFromString(priceStr);

                    specificPrice = new SpecificPrice(price.getValue(), type, Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());
                } catch (IllegalStateException | NullPointerException | UnsupportedOperationException | IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The price of the limit order in the JSON request is not valid.");
                }

                LimitOrder limitOrder;
                try {
                    limitOrder = new LimitOrder(specificPrice, size, true);
                }catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The price or the quantity of the limit order in the JSON request is not valid.");
                }

                CreateRequest orderRequest = new CreateRequest((Order) limitOrder);
                this.values = orderRequest;

            }
            case INSERT_STOP_ORDER -> {

                String typeStr;
                PriceType type;
                try {
                    typeStr = elementValues.getAsJsonObject().get("type").getAsString();
                    type = ClientActionsUtils.getPriceTypeFromString(typeStr);
                } catch (IllegalStateException | NullPointerException | UnsupportedOperationException | IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The type of the stop order in the JSON request is not valid.");
                }

                String sizeStr;
                Quantity size;
                try {
                    sizeStr = elementValues.getAsJsonObject().get("size").getAsString();
                    size = ClientActionsUtils.getSizeFromString(sizeStr);
                } catch (IllegalStateException | NullPointerException | UnsupportedOperationException | IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The size of the stop order in the JSON request is not valid.");
                }

                String priceStr;
                GenericPrice price;
                SpecificPrice specificPrice;
                try {
                    priceStr = elementValues.getAsJsonObject().get("price").getAsString();
                    price = ClientActionsUtils.getPriceFromString(priceStr);

                    specificPrice = new SpecificPrice(price.getValue(), type, Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());
                } catch (IllegalStateException | NullPointerException | UnsupportedOperationException | IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The price of the stop order in the JSON request is not valid.");
                }

                StopOrder stopOrder;
                try {
                    stopOrder = new StopOrder(specificPrice, size, true);
                }catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The price or the quantity of the stop order in the JSON request is not valid.");
                }

                CreateRequest orderRequest = new CreateRequest((Order) stopOrder);
                this.values = orderRequest;

            }

            case INSERT_MARKET_ORDER -> {

                String typeStr;
                PriceType type;
                try {
                    typeStr = elementValues.getAsJsonObject().get("type").getAsString();
                    type = ClientActionsUtils.getPriceTypeFromString(typeStr);
                } catch (IllegalStateException | NullPointerException | UnsupportedOperationException | IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The type of the market order in the JSON request is not valid.");
                }

                String sizeStr;
                Quantity size;
                try {
                    sizeStr = elementValues.getAsJsonObject().get("size").getAsString();
                    size = ClientActionsUtils.getSizeFromString(sizeStr);
                } catch (IllegalStateException | NullPointerException | UnsupportedOperationException | IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The size of the market order in the JSON request is not valid.");
                }

                MarketOrder marketOrder;
                try {
                    marketOrder = new MarketOrder(type, Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency(), size);
                }catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The price or the quantity of the market order in the JSON request is not valid.");
                }

                CreateRequest orderRequest = new CreateRequest((Order) marketOrder);
                this.values = orderRequest;

            }
            case EXIT -> this.values = null;
            default -> throw new IllegalArgumentException("The operation in the JSON request is not valid.");
        }
        
        
    }

    // GETTERS
    /**
     *
     * Getter for the operation.
     *
     * @return The operation as string.
     *
     */
    public String getOperation() {

        return String.valueOf(this.operation);

    }
    /**
     *
     * Getter for the values.
     *
     * @return The values as generic Object.
     *
     */
    public Object getValues() {

        return this.values;

    }

}
