package CROSS.API.Responses;

/**
 * 
 * ResponseCode is a class.
 * 
 * It's used to represent and define the responses codes.
 * 
 * For some type responses from the server, there's a status ("response" code as Integer) and its response content ("message" as string).
 * Responses with different types could have the same response code, but for different content reasons.
 * For that I used different enums and this dedicated class with methods to do some mapping.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see ResponseType
 * @see ResponseContent
 * 
 */
public class ResponseCode {

    // Public enum, because also used outside the class.

    // The type of the response.
    public static enum ResponseType {

        // As described in the assignment.
        REGISTER,
        UPDATE_CREDENTIALS,
        LOGIN,
        LOGOUT,
        CANCEL_ORDER,

        // Some others responses types doesn't exist here, since they have no response code.

        // Added by me.
        EXIT,
        INVALID_REQUEST;

    }

    // Contains ALL the possible responses' contents regardless of the type.
    // All as described in the assignment.
    public static enum ResponseContent {
        // Register
        // OK used also in update credentials and login and logout and cancel order.
        OK,
        INVALID_PASSWORD,
        USERNAME_NOT_AVAILABLE,
        // OTHER_ERROR used also in update credentials and login.
        OTHER_ERROR,

        // UpdateCredentials
        INVALID_NEW_PASSWORD,
        // INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST used also in login.
        INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST,
        NEW_PASSWORD_EQUAL_OLD,
        USER_CURRENTLY_LOGGED_IN,

        // Login
        USER_ALREADY_LOGGED_IN,

        // Logout
        USER_NOT_LOGGED_IN_OR_OTHER_ERROR,

        // CancelOrder
        ORDER_DOES_NOT_EXIST_OR_BELONGS_TO_DIFFERENT_USER_OR_HAS_ALREADY_BEEN_FINALIZED_OR_OTHER_ERROR_CASES,
    }

    // Binding the response code and its content to the type of the response.
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
        USER_NOT_LOGGED_IN_OR_OTHER_ERROR(101),;

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

    // The type of the response as an enum.
    private final ResponseType type;

    // The response content as an enum.
    private final ResponseContent responseContent;
    
    // CONSTRUCTORS
    /**
     * 
     * Constructor of the class.
     * 
     * @param type The type of the response used because different types of responses could have the same response code.
     * @param responseContent The response's content as an enum.
     * 
     * @throws NullPointerException If the type or the content are null.
     * 
     */
    public ResponseCode(ResponseType type, ResponseContent responseContent) throws NullPointerException {
        
        // Null check.
        if (type == null) {
            throw new NullPointerException("Type of the response in the response code generation cannot be null.");
        }
        if (responseContent == null) {
            throw new NullPointerException("Response content in the response code (content) generation cannot be null.");
        }

        this.type = type;
        this.responseContent = responseContent;

    }

    // GETTERS
    /**
     * 
     * Getter for the type of the response.
     * 
     * @return The type of the response as an enum.
     * 
     */
    public ResponseType getType() {

        return ResponseType.valueOf(this.type.name());

    }
    /**
     * 
     * Getter for the response content as an enum.
     * 
     * @return The response content as an enum.
     * 
     */
    public ResponseContent getResponseContent() {

        return ResponseContent.valueOf(this.responseContent.name());

    }
    /**
     * 
     * Getter for the code of the response based on the type and the content, as an Integer.
     * 
     * @return The code of the response based on the type and content, as an Integer.
     * 
     */
    public Integer getCode() {

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
            // TODO: Add other cases, exit and invalid request.
            default:
                // This should never happen.
                throw new RuntimeException("Response's code associated with this type was not found.");
        }
        
        return Integer.valueOf(i);

    }

}
