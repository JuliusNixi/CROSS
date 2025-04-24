package CROSS.Orders;

import CROSS.Exceptions.InvalidOrder;
import CROSS.OrderBook.Market;
import CROSS.Users.User;
import java.util.TreeSet;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;

/**
 * 
 * Orders class is an abstract one.
 * That's because I assume that I don't want to handle different orders dabatases at the same time.
 * So it will use only static methods and fields.
 * 
 * This class will rapresent the orders database file in RAM.
 * It will be synchronized (best effort will be made to do so) with the orders database file on disk.
 * That's will be done with the support of the DBOrdersInterface class.
 * 
 * ALL OPERATIONS MUST BE DONE THROUGH THIS CLASS, THE DBOrdersInterface CLASS IS NOT TO BE USED DIRECTLY.
 * 
 * It uses a TreeSet to store the orders in memory to add and search in complexity O(log n).
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Order
 * 
 * @see DBOrdersInterface
 * 
 * @see InvalidOrder
 * 
 */
public abstract class Orders {
    
    // Add and search in complexity O(log n).
    private static TreeSet<Order> orders = new TreeSet<Order>();

    // ORDERS HANDLING
    /**
     * 
     * Add an order to the database.
     * The order is added BOTH to the TreeSet in memory and to the orders database file if not present.
     * 
     * Synchronized ON CLASS method to prevent multiple threads to add orders at the same time.
     * Synchonized ON ORDER to prevent multiple threads to modify the order's properties during the execution of this method.
     * 
     * @param order The order to add.
     * @param <GenericOrder> The type of the order to add that extends the Order class, could be a LimitOrder, MarketOrder or a StopMarketOrder.
     * @param noOrderPresenceCheck If true, the order already present in the database check is not performed. Used to load orders from the demo file.
     * 
     * @throws InvalidOrder If the order already exists.
     * @throws NullPointerException If the order or the no order presence check is null.
     * @throws Exception If an error occurs while writing the order on the orders database file.
     * 
     */
    public static <GenericOrder extends Order> void addOrder(GenericOrder order, Boolean noOrderPresenceCheck) throws InvalidOrder, NullPointerException, Exception {

        synchronized (Orders.class) {

            // Null checks.
            if (order == null) {
                throw new NullPointerException("Order to add to the database cannot be null.");
            }
            if (noOrderPresenceCheck == null) {
                throw new NullPointerException("No order presence check flag in adding an order to the database cannot be null.");
            }

            synchronized (order) {

                // Already exists check.
                if (noOrderPresenceCheck == false && orders.contains(order)) {
                    throw new InvalidOrder("Order to add to the database already exists.");
                }

                // Adds order to the TreeSet.
                orders.add(order);

                // Prevent double file writes when the method is called from DBOrdersInterface.loadOrders().
                // That's because:
                // DBOrdersInterface.loadOrders() read from file order X -> call addOrder() to add it in RAM -> writeOrderOnFile() write order X on file AGAIN.
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                String className = DBOrdersInterface.class.getName();
                for (StackTraceElement stackTraceElement : stackTrace) {
                    if (stackTraceElement.getClassName().equals(className)) {
                        // Exit before writing on file.
                        return;
                    }
                }

                // Write order on file.
                try {
                    DBOrdersInterface.writeOrderOnFile(order);
                } catch (Exception ex) {
                    // Remove order from TreeSet.
                    orders.remove(order);

                    // Forwarding the exception's message.
                    throw new Exception(ex.getMessage());
                }

            }

        }

    }
    /**
     * 
     * Load all the orders from the JSON orders database file.
     * 
     * It's a synchronized ON CLASS method to prevent multiple threads to load orders from the file at the same time.
     * 
     * The orders are loaded in the TreeSet in memory from the orders database file.
     * 
     * It's a wrapper method for the DBOrdersInterface.loadOrders() method.
     * 
     * @param noPriceCoherenceChecks If true, the price coherence checks are not performed during the orders creation. Used to load orders from the demo file.
     * @param noOrderPresenceCheck If true, the order already present in the database check is not performed. Used to load orders from the demo file.
     * 
     * @throws Exception If an error occurs while loading the orders from the orders database file.
     * 
     */
    public static void loadOrders(Boolean noPriceCoherenceChecks, Boolean noOrderPresenceCheck) throws Exception {

        synchronized (Orders.class) {

            try {
                DBOrdersInterface.loadOrders(noPriceCoherenceChecks, noOrderPresenceCheck);
            } catch (Exception ex) {
                // Forwarding the exception's message.
                throw new Exception(ex.getMessage());
            }

        }

    }

    // GETTERS
    /**
     * 
     * Find an order with its order's id as Integer in the orders database.
     * 
     * @param id The order's id of the order to find as an Integer.
     * 
     * @return A Order object found with the given order's id if the order is found, null otherwise.
     * 
     * @throws NullPointerException If the order's id is null.
     * @throws RuntimeException If the orders are not loaded from the database orders file yet. If the main market has not been setted yet.
     * 
     */
    public static Order getOrder(Integer id) throws NullPointerException, RuntimeException {

        // Null check.
        // Since the orders database (at the moment) only support the main market, it is used and not requested as parameter.
        if (id == null) {
            throw new NullPointerException("Order's id of the order to search in the orders database cannot be null.");
        }

        // Orders not loaded from the database orders file yet.
        if (DBOrdersInterface.ordersLoaded() == false) {
            throw new RuntimeException("Orders not loaded from the database orders file yet. Call loadOrders() before.");
        }
        
        Market market = Market.getMainMarket();

        // Used a lot of placeholders here, but it's not important since the comparison is done only on the id.
        LimitOrder toSearchLimit = new LimitOrder(new SpecificPrice(1, PriceType.BID, market), new Quantity(1), new User("placeholder", "placeholder"));
        toSearchLimit.setId(id);

        Order toSearch = toSearchLimit;
        Order result = orders.ceiling(toSearch);
        if (result != null && result.getId().compareTo(id) == 0) {
            return result;
        }
        
        return null;
        
    }
    /**
     * 
     * Get the size of the database.
     * 
     * @return The size of the database (number of orders) as an Integer.
     * 
     */
    public static Integer getOrdersSize() {

        // If the database is not loaded yet, return 0, do not throw an exception, otherwise problems during the orders loading.

        return orders.size();

    }

    /**
     * 
     * Get a string with the whole orders database. Each order is on a new line.
     * 
     * Synchronized ON CLASS method to prevent multiple threads to modify the orders database during the execution of this method.
     * 
     * @return The orders database as list of string lines, joined in an unique string by '\n'.
     * 
     * @throws RuntimeException If the orders are not loaded from the database orders file yet.
     * 
     */
    public static String toStringOrders() throws RuntimeException {

        synchronized (Orders.class) {

            // Orders not loaded from the database orders file yet.
            if (DBOrdersInterface.ordersLoaded() == false) {
                throw new RuntimeException("Orders not loaded from the database orders file yet. Call loadOrders() before.");
            }

            String result = "";
            for (Order order : orders) {
                // To string is itself synchronized on the order.
                result += order.toString() + "\n";
            }
            
            return result;

        }

    }

}
