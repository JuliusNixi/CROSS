package CROSS.API;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * 
 * RequestResponse is a class.
 * 
 * It's used to represent and define the requests / responses forms.
 * 
 * For the responses from the server, there's a status ("response" code) and its response content ("message").
 * Responses with different types could have the same response code, but for different content reasons.
 * For that I used different enums and this dedicated class with methods, to put more order.
 * OTHERWISE, with some types of responses, there can be ONLY an "orderId" field, to pass the order's ID (or -1 in error case).
 * //TODO: GetPriceHistory Response
 * 
 * For the requests to the server sent by the clients, there's a "operation" field, to describe the desired action, and a "values" field, to pass the needed (if present) data.
 * This class takes care ONLY of the "operation" field, not of the dynamic "values" field.
 * //TODO: GetPriceHistory Request
 * 
 * A notification is sent (asynch, so without any previous related request) by the server to the client on executed orders.
 * In this case, the "operation" field is SUBSTITUTE by the "notification" field and the "values" field is SUBSTITUTE by the "trades" array field.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see RequestResponseType
 * @see ResponseContent
 * 
 */
public class RequestResponse {

    // Public enum, because also used outside the class.

    // The type of the request / response.
    public static enum RequestResponseType {

        // As described in the assignment.
        REGISTER,
        UPDATE_CREDENTIALS,
        LOGIN,
        LOGOUT,
        INSERT_LIMIT_ORDER,
        INSERT_MARKET_ORDER,
        INSERT_STOP_ORDER,
        CANCEL_ORDER,
        GET_PRICE_HISTORY,
        CLOSED_TRADES,
        ORDER_INFO,

        // Added by me.
        EXIT,
        INVALID_REQUEST;

        // To generate the field "operation" as described in the assignment to use in the JSON API requests.
        @Override
        public String toString() {

            String operation = this.name();
            operation = operation.toLowerCase().replaceAll("_", " ");

            LinkedList<String> list = new LinkedList<String>(Arrays.asList(operation.split(" ")));
            for (int i = 1; i < list.size(); i++) {
                // Converting to camelCase.
                list.set(i, list.get(i).substring(0, 1).toUpperCase() + list.get(i).substring(1));
            }

            operation = String.join("", list);

            return operation;
            
        }
    
    }

    // Contains ALL the possible responses' contents regardless of the type.
    // Only responses, NOT requests.
    // All as described in the assignment.
    public static enum ResponseContent {
        // Register
        OK,
        INVALID_PASSWORD,
        USERNAME_NOT_AVAILABLE,
        OTHER_ERROR,

        // UpdateCredentials
        INVALID_NEW_PASSWORD,
        INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST,
        NEW_PASSWORD_EQUAL_OLD,
        USER_CURRENTLY_LOGGED_IN,

        // Login
        USER_ALREADY_LOGGED_IN,

        // Logout
        USER_NOT_LOGGED_IN,

        // CancelOrder
        ORDER_DOES_NOT_EXIST_OR_BELONGS_TO_DIFFERENT_USER_OR_HAS_ALREADY_BEEN_FINALIZED_OR_OTHER_ERROR_CASES,
    }

    // Binding the response code and its content ("message") to the type of the response.
    // Only responses, NOT requests.
    // All as described in the assignment.
    // THIS IS NOT ALWAYS USED, ONLY IN THE SPECIFIC SERVER'S RESPONSES.
    private static enum responseRegister {

        OK(100),
        INVALID_PASSWORD(101),
        USERNAME_NOT_AVAILABLE(102),
        OTHER_ERROR(103),;

        private final int code;
        
        responseRegister(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }

    }
    private static enum responseUpdateCredentials {

        OK(100),
        INVALID_NEW_PASSWORD(101),
        INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST(102),
        NEW_PASSWORD_EQUAL_OLD(103),
        USER_CURRENTLY_LOGGED_IN(104),
        OTHER_ERROR(105),;

        private final int code;
        
        responseUpdateCredentials(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }

    }
    private static enum responseLogin {

        OK(100),
        INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST(101),
        USER_ALREADY_LOGGED_IN(102),
        OTHER_ERROR(103),;

        private final int code;

        responseLogin(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }

    };
    private static enum responseLogout {

        OK(100),
        INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST_OR_USER_NOT_LOGGED_OR_OTHER_ERROR(101),;

        private final int code;

        responseLogout(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }

    }
    private static enum responseCancelOrder {

        OK(100),
        ORDER_DOES_NOT_EXIST_OR_BELONGS_TO_DIFFERENT_USER_OR_HAS_ALREADY_BEEN_FINALIZED_OR_OTHER_ERROR_CASES(101),;

        private final int code;

        responseCancelOrder(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }

    }

    // The type of the request / response.
    // THIS IS ALWAYS USED, BOTH IN ALL REQUESTS AND ALL RESPONSES.
    private RequestResponseType type = null;

    // The response content as an enum.
    // THIS IS NOT ALWAYS USED, ONLY IN THE SPECIFIC SERVER'S RESPONSES.
    private ResponseContent responseContent = null;
    
    // CONSTRUCTORS
    // 3 different constructors for the RequestResponse class.
    // The first one is used when the REQUEST or the RESPONSE is generic and so only the type is needed.
    // The second one is used when the RESPONSE is given as an enum.
    // The third one is used when the RESPONSE is given as a code (number).

    /**
     * 
     * Constructor of the ResponseCode class.
     * 
     * @param type The type of the response used.
     * 
     * @throws NullPointerException If the type is null.
     * 
     */
    public RequestResponse(RequestResponseType type) throws NullPointerException {
        
        // Null check.
        if (type == null) {
            throw new NullPointerException("Type of the request / response cannot be null.");
        }

        this.type = type;

    }
    // With the below getters we could get the response as an enum or as a code.
    /**
     * 
     * Constructor of the ResponseCode class.
     * 
     * @param type The type of the response used because different types of responses could have the same response code.
     * @param responseContent The response's content as an enum.
     * 
     * @throws NullPointerException If the type or the content are null.
     * 
     */
    public RequestResponse(RequestResponseType type, ResponseContent responseContent) throws NullPointerException {
        
        // Null check.
        if (type == null) {
            throw new NullPointerException("Type of the response cannot be null.");
        }
        if (responseContent == null) {
            throw new NullPointerException("Response content cannot be null.");
        }

        this.type = type;
        this.responseContent = responseContent;

    }
    /**
     * 
     * Constructor of the ResponseCode class.
     * 
     * @param type The type of the response used because different types of responses could have the same response code.
     * @param code The code of the response.
     * 
     * @throws NullPointerException If the type or the code are null.
     * 
     */
    public RequestResponse(RequestResponseType type, Integer code) throws NullPointerException {

        // Null check.
        if (type == null) {
            throw new NullPointerException("Type of the response cannot be null.");
        }
        if (code == null) {
            throw new NullPointerException("Code cannot be null.");
        }

        this.type = type;
        this.responseContent = ResponseContent.values()[code];

    }

    // GETTERS
    /**
     * 
     * Getter for the type of the request / response.
     * 
     * @return The type of the request / response.
     * 
     */
    public RequestResponseType getType() {

        return RequestResponseType.valueOf(type.name());

    }
    /**
     * 
     * Getter for the response as an enum.
     * 
     * @return The response as an enum.
     * 
     */
    public ResponseContent getResponseContent() {

        return ResponseContent.valueOf(this.responseContent.name());

    }
    /**
     * 
     * Getter for the code of the response based on the type as an Integer.
     * 
     * @throws RuntimeException If the response's code associated with this type was not found.
     * 
     * @return The code of the response based on the type as an Integer.
     * 
     */
    public Integer getCode() throws RuntimeException {

        int i = 0;
        switch (type) {
            case REGISTER:
                i = responseRegister.valueOf(responseContent.name()).getCode();
                break;
            case UPDATE_CREDENTIALS:
                i = responseUpdateCredentials.valueOf(responseContent.name()).getCode();
                break;
            case LOGIN:
                i = responseLogin.valueOf(responseContent.name()).getCode();
                break;
            case LOGOUT:
                i = responseLogout.valueOf(responseContent.name()).getCode();
                break;
            case CANCEL_ORDER:
                i = responseCancelOrder.valueOf(responseContent.name()).getCode();
                break;
            default:
                // This should never happen.
                throw new RuntimeException("Response's code associated with this type was not found.");
        }
        
        return Integer.valueOf(i);

    }

    @Override
    public String toString() {

        String content = this.getResponseContent() == null ? "null" : this.getResponseContent().name();
        String code = this.getCode() == null ? "null" : this.getCode().toString();
        return String.format("Type [%s] - Content [%s] - Code [%s]", this.getType().name(), content, code);
    
    }
    
}
