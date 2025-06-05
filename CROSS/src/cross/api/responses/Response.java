package cross.api.responses;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import cross.api.JSONAPIMessage;
import cross.api.responses.orders.CancelResponse;
import cross.api.responses.orders.ExecutionResponse;
import cross.api.responses.pricehistory.DailyPriceStats;
import cross.api.responses.pricehistory.PriceHistoryResponse;
import cross.api.responses.user.UserResponse;
import cross.types.price.GenericPrice;
import cross.utils.ClientActionsUtils;
import cross.utils.ClientActionsUtils.ClientActions;

/**
 *
 * This class is responsible for creating a response Java object.
 * It's rapresents a JSON response string that will be sent to the client from the server through the TCP socket.
 *
 * It extends the JSONAPIMessage class to use the toJSONString conversion method.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see JSONAPIMessage
 * 
 * @see CancelResponse
 * @see ExecutionResponse
 * @see PriceHistoryResponse
 * @see UserResponse
 * 
 * @see ClientActions
 * 
 * @see ResponseCode
 *
 */
public class Response extends JSONAPIMessage {
    
    // This field name is not important, since its content will be extracted in this object root.
    private final Object response;

    // ClientActions to be used to parse the JSON string response in the ResponsesThread class.
    // No ResponseType, since it doesn't have the order execution or the price history.
    // Is null if the response is a notification.
    // transient to avoid serialization in JSON by Gson.
    private final transient ClientActions type;

    /**
     *
     * Constructor of the class.
     * 
     * Object since it takes any type of response.
     *
     * @param response The response as Object.
     * @param type The type of the response as ClientActions or null if it's a notification.
     *
     * @throws NullPointerException If the response is null.
     *
     */
    public Response(Object response, ClientActions type) throws NullPointerException {

        // Null check.
        if (response == null)
            throw new NullPointerException("Response object in the response cannot be null.");

        this.response = response;
        this.type = type;
        
    }
    /**
     *
     * Alternative constructor of the class.
     * 
     * Used in the ResponsesThread class to parse the response object from a JSON string.
     *
     * @param JSONresponse The JSON response string.
     *
     * @throws NullPointerException If the JSON response string is null.
     * @throws IllegalArgumentException If the JSON string is not a valid JSON object or if the JSON string content is not valid or the whole response is not valid / recognized.
     *
     */
    public Response(String JSONresponse) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (JSONresponse == null)
            throw new NullPointerException("JSON response string in the response cannot be null.");

        JsonObject jsonObject = null;
        try {
            jsonObject = JsonParser.parseString(JSONresponse).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException ex) {
            throw new IllegalArgumentException("JSON response string in the response is not a valid JSON object.");
        }


        // Trying to parse the response of an user's data request.
        String responseCode = null;
        String errorMessage = null;
        Integer responseCodeInteger = null;
        try {

            responseCode = jsonObject.get("response").getAsString();

            errorMessage = jsonObject.get("errorMessage").getAsString();

            responseCodeInteger = Integer.valueOf(responseCode);
        // NullPointerException thrown by the .getAsJsonObject() on null object.
        } catch (UnsupportedOperationException | IllegalStateException | NullPointerException ex) {
            // Maybe not this type of request.
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("The response code is not a valid integer.");
        }
        if (responseCode != null && errorMessage != null) {
            ResponseCode responseCodeObject = null;

            try {
                responseCodeObject = new ResponseCode(responseCodeInteger, errorMessage);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
            switch (responseCodeObject.getType()) {
                case REGISTER -> {
                    this.response = new UserResponse(responseCodeObject, responseCodeObject.getDefaultMessage());
                    this.type = ClientActions.REGISTER;
                }
                case UPDATE_CREDENTIALS -> {
                    this.response = new UserResponse(responseCodeObject, responseCodeObject.getDefaultMessage());
                    this.type = ClientActions.UPDATE_CREDENTIALS;
                }
                case LOGIN -> {
                    this.response = new UserResponse(responseCodeObject, responseCodeObject.getDefaultMessage());
                    this.type = ClientActions.LOGIN;
                }
                case LOGOUT -> {
                    this.response = new UserResponse(responseCodeObject, responseCodeObject.getDefaultMessage());
                    this.type = ClientActions.LOGOUT;
                }
                case CANCEL_ORDER -> {
                    this.response = new CancelResponse(responseCodeObject, responseCodeObject.getDefaultMessage());
                    this.type = ClientActions.CANCEL_ORDER;
                }
                default -> throw new IllegalArgumentException("The response code is not valid.");
            }
            return;
        }

        // Trying to parse the response of an execution's order request.
        String orderId = null;
        Number orderIdNumber = null;
        try {
            orderId = jsonObject.get("orderId").getAsString();
            orderIdNumber = ClientActionsUtils.getOrderIDFromString(orderId);
        } catch (UnsupportedOperationException | IllegalStateException | NullPointerException ex) {
            // Maybe not this type of request.
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("The order's id is not a valid integer.");
        }
        if (orderId != null && orderIdNumber != null) {
            this.response = new ExecutionResponse(orderIdNumber);
            this.type = ClientActions.INSERT_MARKET_ORDER;
            return;
        }

        // Trying to parse the response of a price history request.
        try {
            JsonArray priceHistory = null;
            priceHistory = jsonObject.get("priceHistory").getAsJsonArray();

            DailyPriceStats dailyPriceStats = null;
            PriceHistoryResponse priceHistoryResponse = new PriceHistoryResponse();
            for (int i = 0; i < priceHistory.size(); i++) {
                JsonObject priceHistoryObject = priceHistory.get(i).getAsJsonObject();

                String dateGMTString = priceHistoryObject.get("dayGMT").getAsString();
                Long timestamp;
                Instant instant = null;
                try {
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("yyyy-MM-dd HH:mm:ss z").toFormatter(Locale.ENGLISH);
                    instant = ZonedDateTime.parse(dateGMTString, formatter).toInstant();
                } catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException();
                }
                try {
                    timestamp = instant.toEpochMilli();
                } catch (ArithmeticException ex) {
                    throw new IllegalArgumentException();
                }

                String priceHighString = priceHistoryObject.get("high").getAsString();
                GenericPrice genericPriceHigh = ClientActionsUtils.getPriceFromString(priceHighString);

                String priceLowString = priceHistoryObject.get("low").getAsString();
                GenericPrice genericPriceLow = ClientActionsUtils.getPriceFromString(priceLowString);

                String priceOpenString = priceHistoryObject.get("open").getAsString();
                GenericPrice genericPriceOpen = ClientActionsUtils.getPriceFromString(priceOpenString);

                String priceCloseString = priceHistoryObject.get("close").getAsString();
                GenericPrice genericPriceClose = ClientActionsUtils.getPriceFromString(priceCloseString);
                
                dailyPriceStats = new DailyPriceStats(timestamp, genericPriceHigh, genericPriceLow, genericPriceOpen, genericPriceClose);

                priceHistoryResponse.addDailyPriceStats(dailyPriceStats);
                
            }
            this.response = priceHistoryResponse;
            this.type = ClientActions.GET_PRICE_HISTORY;
            return;
        } catch (RuntimeException ex) {
            // Maybe not this type of request.
        }

        // If we get here, the response is not valid.
        throw new IllegalArgumentException("The response is not valid.");

    }

    @Override
    public String toJSONString() {

        /*
         *
         * {
         *   response: {
         *      ...
         *   }
         * }
         *
         * Need to remove the first parenthesis couple with the response field.
         * The "..." is the response content.
         * Must be extracted in this object root.
         *
         */
        String nested = super.toJSONString();
        JsonObject jsonObject = JsonParser.parseString(nested).getAsJsonObject();
        return jsonObject.get("response").getAsJsonObject().toString().replace("\n", "").replace("\r", "").replace("\t", "").trim() + "\n";

    }

    // GETTERS
    /**
     *
     * Getter for the response.
     *
     * @return The response as generic Object.
     *
     */
    public Object getResponse() {

        return this.response;

    }
    /**
     *
     * Getter for the type.
     *
     * @return The type as ClientActions or null if it's a notification.
     *
     */
    public ClientActions getType() {

        return this.type;

    }

}
