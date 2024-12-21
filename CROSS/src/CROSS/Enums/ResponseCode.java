package CROSS.Enums;

/**
 * ResponseCode is a class, it contains some enums.
 * These are used to represent the response code of a response to a request.
 * Responses with different types could have the same response code for different reasons.
 * For that I used different enums.
 * 
 * @version 1.0
 */
public class ResponseCode {

    public static enum Register {

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

    public static enum updateCredentials {

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

    public static enum Login {
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

    public static enum Logout {
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

    public static enum cancelOrder {
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

    private ResponseType type;
    public ResponseCode(ResponseType type) {
        this.type = type;
    }

    public ResponseType getType() {
        return type;
    }

    public int getCode() {
        switch (type) {
            case REGISTER:
                return Register.OK.getCode();
            case UPDATE_CREDENTIALS:
                return updateCredentials.OK.getCode();
            case LOGIN:
                return Login.OK.getCode();
            case LOGOUT:
                return Logout.OK.getCode();
            case CANCEL_ORDER:
                return cancelOrder.OK.getCode();
            default:
                return -1;
        }
    }

};
