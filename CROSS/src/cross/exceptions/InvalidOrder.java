package cross.exceptions;

/**
 *
 * Exception for invalid order.
 * It's throwed, for example, when adding an order that already exists in the orders database.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Exception
 * 
 * @see cross.users.db.DBUsersInterface
 * @see cross.orders.db.Users
 *
 */
public class InvalidOrder extends Exception {

    /**
     *
     * Constructor of the exception.
     *
     * @param message Message of the exception.
     *
     */
    public InvalidOrder(String message) {

        super(message);

    }
    
}
