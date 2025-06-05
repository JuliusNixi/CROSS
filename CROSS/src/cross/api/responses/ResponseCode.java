package cross.api.responses;

/**
 *
 * ResponseCode is a class.
 *
 * It's used to represent and define the responses codes for the server's responses.
 *
 * For some type responses from the server, there's a status ("response" code as Integer) and its response content ("message" as String).
 * Responses with different types could have the same response code, but for different content reasons.
 * For that I used different enums and this dedicated class with methods to do some mapping.
 * 
 * Used in CancelResponse, UserResponse.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see ResponseType
 * @see ResponseContent
 * 
 * @see CancelResponse
 * @see UserResponse
 *
 */
public class ResponseCode {

    // The type of the response.
    public static enum ResponseType {

        // As described in the assignment.
        REGISTER,
        UPDATE_CREDENTIALS,
        LOGIN,
        LOGOUT,

        CANCEL_ORDER,

        // Some others responses types doesn't exist here, since they have no response code.

    }
    
    // Contains ALL the possible responses' contents regardless of the type.
    // All as described in the assignment's text.
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
    // All as described in the assignment's text.
    // THIS IS NOT ALWAYS USED, ONLY IN THE SPECIFIC SERVER'S RESPONSES.
    private static enum responseRegister {

        OK(100),
        INVALID_PASSWORD(101),
        USERNAME_NOT_AVAILABLE(102),
        OTHER_ERROR(103),;

        private final int code;
        private final String defaultMessage;

        responseRegister(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
            switch (code) {
                case 100:
                    this.defaultMessage = "Registered successfully.";
                    break;
                case 101:
                    this.defaultMessage = "Invalid password.";
                    break;
                case 102:
                    this.defaultMessage = "Username not available.";
                    break;
                case 103:
                    this.defaultMessage = "Other error.";
                    break;
                default:
                    this.defaultMessage = "The response code is not valid.";
            }
        }
        
        public int getCode() {
            return code;
        }

        public String getDefaultMessage() {
            return defaultMessage;
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
        private final String defaultMessage;

        responseUpdateCredentials(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
            switch (code) {
                case 100:
                    this.defaultMessage = "Updated credentials successfully.";
                    break;
                case 101:
                    this.defaultMessage = "Invalid new password.";
                    break;
                case 102:
                    this.defaultMessage = "Invalid username or password match or username not exist.";
                    break;
                case 103:
                    this.defaultMessage = "New password equals old password.";
                    break;
                case 104:
                    this.defaultMessage = "User currently logged in.";
                    break;
                case 105:
                    this.defaultMessage = "Other error.";
                    break;
                default:
                    this.defaultMessage = "The response code is not valid.";
            }
        }

        public int getCode() {
            return code;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }

    }
    private static enum responseLogin {

        OK(100),
        INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST(101),
        USER_ALREADY_LOGGED_IN(102),
        OTHER_ERROR(103),;

        private final int code;
        private final String defaultMessage;

        responseLogin(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
            switch (code) {
                case 100:
                    this.defaultMessage = "Logged in successfully.";
                    break;
                case 101:
                    this.defaultMessage = "Invalid username or password match or username not exist.";
                    break;
                case 102:
                    this.defaultMessage = "User already logged in.";
                    break;
                case 103:
                    this.defaultMessage = "Other error.";
                    break;
                default:
                    this.defaultMessage = "The response code is not valid.";
            }
        }

        public int getCode() {
            return code;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }

    };
    private static enum responseLogout {

        OK(100),
        USER_NOT_LOGGED_IN_OR_OTHER_ERROR(101),;

        private final int code;
        private final String defaultMessage;

        responseLogout(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
            switch (code) {
                case 100:
                    this.defaultMessage = "Logged out successfully.";
                    break;
                case 101:
                    this.defaultMessage = "User not logged in or other error.";
                    break;
                default:
                    this.defaultMessage = "The response code is not valid.";
            }
        }

        public int getCode() {
            return code;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }

    }
    private static enum responseCancelOrder {

        OK(100),
        ORDER_DOES_NOT_EXIST_OR_BELONGS_TO_DIFFERENT_USER_OR_HAS_ALREADY_BEEN_FINALIZED_OR_OTHER_ERROR_CASES(101),;

        private final int code;
        private final String defaultMessage;

        responseCancelOrder(int code) {
            // Checking of the code is not necessary, as it is an enum.
            // It's done by the compiler.
            this.code = code;
            switch (code) {
                case 100:
                    this.defaultMessage = "Canceled order successfully.";
                    break;
                case 101:
                    this.defaultMessage = "Order does not exist or belongs to different user or has already been finalized or other error cases.";
                    break;
                default:
                    this.defaultMessage = "The response code is not valid.";
            }
        }

        public int getCode() {
            return code;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }

    }

    // The type of the response as an enum.
    private ResponseType type;

    // The response content as an enum.
    private ResponseContent responseContent;

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
            throw new NullPointerException("Response content in the response code generation cannot be null.");
        }

        this.type = type;
        this.responseContent = responseContent;

    }
    /**
     *
     * Alternative constructor of the class.
     * 
     * Used in the Response class to parse the response object from a JSON string.
     *
     * @param code The code of the response as an Integer.
     * @param responseDefaultMessage The default message of the response as a String.
     *
     * @throws NullPointerException If the default message string or the integer code are null.
     * @throws IllegalArgumentException If the response code associated to the default message is not valid.
     *
     */
    public ResponseCode(Integer code, String responseDefaultMessage) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (responseDefaultMessage == null) {
            throw new NullPointerException("Response default message in the response code generation cannot be null.");
        }
        if (code == null) {
            throw new NullPointerException("Code in the response code generation cannot be null.");
        }

        for (responseRegister response : responseRegister.values()) {
            if (response.getCode() == code && response.getDefaultMessage().compareTo(responseDefaultMessage) == 0) {
                this.type = ResponseType.REGISTER;
                this.responseContent = ResponseContent.valueOf(response.name());
            }
        }

        for (responseUpdateCredentials response : responseUpdateCredentials.values()) {
            if (response.getCode() == code && response.getDefaultMessage().compareTo(responseDefaultMessage) == 0) {
                this.type = ResponseType.UPDATE_CREDENTIALS;
                this.responseContent = ResponseContent.valueOf(response.name());
            }
        }

        for (responseLogin response : responseLogin.values()) {
            if (response.getCode() == code && response.getDefaultMessage().compareTo(responseDefaultMessage) == 0) {
                this.type = ResponseType.LOGIN;
                this.responseContent = ResponseContent.valueOf(response.name());
            }
        }

        for (responseLogout response : responseLogout.values()) {
            if (response.getCode() == code && response.getDefaultMessage().compareTo(responseDefaultMessage) == 0) {
                this.type = ResponseType.LOGOUT;
                this.responseContent = ResponseContent.valueOf(response.name());
            }
        }

        for (responseCancelOrder response : responseCancelOrder.values()) {
            if (response.getCode() == code && response.getDefaultMessage().compareTo(responseDefaultMessage) == 0) {
                this.type = ResponseType.CANCEL_ORDER;
                this.responseContent = ResponseContent.valueOf(response.name());
            }
        }

        if (this.type == null) {
            throw new IllegalArgumentException("The response code associated to the default message is not valid.");
        }
        
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
            case REGISTER -> i = responseRegister.valueOf(responseContent.name()).getCode();
            case UPDATE_CREDENTIALS -> i = responseUpdateCredentials.valueOf(responseContent.name()).getCode();
            case LOGIN -> i = responseLogin.valueOf(responseContent.name()).getCode();
            case LOGOUT -> i = responseLogout.valueOf(responseContent.name()).getCode();
            case CANCEL_ORDER -> i = responseCancelOrder.valueOf(responseContent.name()).getCode();
        }

        return (Integer) i;

    }
    /**
     *
     * Getter for the default message of the response based on the type and the content, as a String.
     *
     * @return The default message of the response based on the type and content, as a String.
     *
     */
    public String getDefaultMessage() {

        String defaultMessage;
        defaultMessage = switch (type) {
            case REGISTER -> responseRegister.valueOf(responseContent.name()).getDefaultMessage();
            case UPDATE_CREDENTIALS -> responseUpdateCredentials.valueOf(responseContent.name()).getDefaultMessage();
            case LOGIN -> responseLogin.valueOf(responseContent.name()).getDefaultMessage();
            case LOGOUT -> responseLogout.valueOf(responseContent.name()).getDefaultMessage();
            case CANCEL_ORDER -> responseCancelOrder.valueOf(responseContent.name()).getDefaultMessage();
            default -> "The response code is not valid.";
        };

        return defaultMessage;  

    }

}
