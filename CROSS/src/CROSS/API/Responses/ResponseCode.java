package CROSS.API.Responses;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * ResponseCode is a class, it contains some enums.
 * These are used to represent the response status with its code (and its content) of a response to a request.
 * 
 * Responses with different types could have the same response code for different content reasons.
 * For that I used different enums and a dedicated class and methods.
 * 
 * @version 1.0
 * @see ResponseType
 * @see AllResponses
 */
public class ResponseCode {

    // The type of the response.
    public enum ResponseType {
        // As described in the assignment.
        REGISTER,
        UPDATE_CREDENTIALS,
        LOGIN,
        LOGOUT,
        INSERT_LIMIT_ORDER,
        INSERT_MARKET_ORDER,
        INSERT_STOP_ORDER,
        CANCEL_ORDER,
        CLOSED_TRADES,
        GET_PRICE_HISTORY,

        // Added by me.
        SERVER_FULL,
        EXIT,
        INVALID_REQUEST,
        ORDER_INFO;

        @Override
        public String toString() {

            String operation = this.name();
            operation = operation.toLowerCase().replaceAll("_", " ");
            LinkedList<String> list = new LinkedList<>(Arrays.asList(operation.split(" ")));
            for (int i = 0; i < list.size(); i++) {
                list.set(i, list.get(i).substring(0, 1).toUpperCase() + list.get(i).substring(1));
            }
            operation = String.join("", list);
            operation = operation.substring(0, 1).toLowerCase() + operation.substring(1);

            return operation;
            
        }
    }

    // Contains ALL the possible responses regardless of the type.
    public static enum AllResponses {
        OK,
        INVALID_PASSWORD,
        USERNAME_NOT_AVAILABLE,
        OTHER_ERROR,
        INVALID_NEW_PASSWORD,
        INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST,
        NEW_PASSWORD_EQUAL_OLD,
        USER_CURRENTLY_LOGGED_IN,
        USER_ALREADY_LOGGED_IN,
        INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST_OR_USER_NOT_LOGGED_OR_OTHER_ERROR,
        ORDER_DOES_NOT_EXIST_OR_BELONGS_TO_DIFFERENT_USER_OR_HAS_ALREADY_BEEN_FINALIZED_OR_OTHER_ERROR_CASES,
        SERVER_FULL,
        EXIT,
        INVALID_REQUEST,
        ORDER_INFO;
    }

    // Binding the response code and its content to the type of the response.
    private static enum Register {

        OK(100),
        INVALID_PASSWORD(101),
        USERNAME_NOT_AVAILABLE(102),
        OTHER_ERROR(103),;

        private final int code;
        
        Register(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }

    }
    private static enum updateCredentials {

        OK(100),
        INVALID_NEW_PASSWORD(101),
        INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST(102),
        NEW_PASSWORD_EQUAL_OLD(103),
        USER_CURRENTLY_LOGGED_IN(104),
        OTHER_ERROR(105),;

        private final int code;
        
        updateCredentials(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }

    }
    private static enum Login {
        OK(100),
        INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST(101),
        USER_ALREADY_LOGGED_IN(102),
        OTHER_ERROR(103),;

        private final int code;

        Login(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }
    };
    private static enum Logout {
        OK(100),
        INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST_OR_USER_NOT_LOGGED_OR_OTHER_ERROR(101),;

        private final int code;

        Logout(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }
    }
    private static enum cancelOrder {
        OK(100),
        ORDER_DOES_NOT_EXIST_OR_BELONGS_TO_DIFFERENT_USER_OR_HAS_ALREADY_BEEN_FINALIZED_OR_OTHER_ERROR_CASES(101),;

        private final int code;

        cancelOrder(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }
    }
    private static enum serverFull {
        SERVER_FULL(100);

        private final int code;

        serverFull(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }   
    }
    private static enum exit {
        EXIT(100);

        private final int code;

        exit(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }   
    }
    private static enum invalidRequest {
        INVALID_REQUEST(100);

        private final int code;

        invalidRequest(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }   
    }
    private static enum orderInfo {
        ORDER_INFO(100);

        private final int code;

        orderInfo(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
        }
        public int getCode() {
            return code;
        }   
    }

    private ResponseType type;
    private AllResponses responseContent;
    
    // 2 different constructors for the ResponseCode class.
    // The first one is used when the response is given as an enum.
    // The second one is used when the response is given as a code.
    // With the below getters we could get the response as an enum or as a code.
    /**
     * Constructor of the ResponseCode class.
     * 
     * @param type The type of the response used because different types of responses could have the same response code.
     * @param responseContent The responseContent as an enum.
     * @throws NullPointerException If the type or the responseContent are null.
     * @see AllResponses
     * @see ResponseType
     */
    public ResponseCode(ResponseType type, AllResponses responseContent) throws NullPointerException {
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
     * Constructor of the ResponseCode class.
     * 
     * @param type The type of the response used because different types of responses could have the same response code.
     * @param code The code of the response.
     * @throws NullPointerException If the type or the code are null.
     * @see AllResponses
     * @see ResponseType
     */
    public ResponseCode(ResponseType type, Integer code) throws NullPointerException {
        if (type == null) {
            throw new NullPointerException("Type of the response cannot be null.");
        }
        if (code == null) {
            throw new NullPointerException("Code cannot be null.");
        }

        this.type = type;
        this.responseContent = AllResponses.values()[code];
    }

    // GETTERS
    /**
     * Getter for the type of the response.
     * 
     * @return The type of the response.
     */
    public ResponseType getType() {
        return ResponseType.valueOf(type.name());
    }
    /**
     * Getter for the response as an enum.
     * 
     * @return The response as an enum.
     */
    public AllResponses getResponseContent() {
        return AllResponses.valueOf(this.responseContent.name());
    }
    /**
     * Getter for the code of the response based on the type as an Integer.
     * 
     * @return The code of the response based on the type as an Integer.
     */
    public Integer getCode() {
        int i = 0;
        switch (type) {
            case REGISTER:
                i = Register.valueOf(responseContent.name()).getCode();
                break;
            case UPDATE_CREDENTIALS:
                i = updateCredentials.valueOf(responseContent.name()).getCode();
                break;
            case LOGIN:
                i = Login.valueOf(responseContent.name()).getCode();
                break;
            case LOGOUT:
                i = Logout.valueOf(responseContent.name()).getCode();
                break;
            case CANCEL_ORDER:
                i = cancelOrder.valueOf(responseContent.name()).getCode();
                break;
            case SERVER_FULL:
                i = serverFull.valueOf(responseContent.name()).getCode();
                break;
            case EXIT:
                i = exit.valueOf(responseContent.name()).getCode();
                break;
            case INVALID_REQUEST:
                i = invalidRequest.valueOf(responseContent.name()).getCode();
                break;
            case ORDER_INFO:
                i = orderInfo.valueOf(responseContent.name()).getCode();
                break;
            default:
                // This should never happen.
                System.exit(-1);
                break;
        }
        
        return Integer.valueOf(i);
    }

    @Override
    public String toString() {
        return String.format("Type [%s] - Content [%s] - Code [%s]", this.getType().name(), this.getResponseContent().name(), this.getCode());
    }
    
}
