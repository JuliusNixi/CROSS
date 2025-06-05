package cross.orders.db;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import com.google.gson.JsonSyntaxException;
import cross.api.responses.pricehistory.DailyPriceStats;
import cross.api.responses.pricehistory.PriceHistoryResponse;
import cross.exceptions.InvalidOrder;
import cross.orderbook.OrderBook;
import cross.orders.LimitOrder;
import cross.orders.MarketOrder;
import cross.orders.Order;
import cross.orders.OrderType;
import cross.types.Currency;
import cross.types.Quantity;
import cross.types.price.PriceType;
import cross.types.price.SpecificPrice;

/**
 *
 * Orders class is an abstract one.
 * That's because I assume that I don't want to handle different orders dabatases at the same time.
 * So I will use all static methods and variables.
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
    private static TreeSet<Order> orders = new TreeSet<>();
    // If the orders are loaded from the demo file, the orders have duplicates, so I need to store them in a different collection.
    private static LinkedList<Order> ordersDuplicates = new LinkedList<>();

    // These methods names are used to check who calls some functions of this class, so to perform some different operations in case the call comes from one of them.
    private static final String LOADORDERS_METHOD_NAME = "loadOrders";

    // ORDERS HANDLING
    /**
     *
     * Adds an order to the orders database.
     * The order is added BOTH to the TreeSet in memory and to the orders database file if not present.
     *
     * Synchronized ON CLASS method to prevent multiple threads to add orders at the same time.
     * Synchonized ON ORDER object to prevent multiple threads to modify the order's properties during the execution of this method.
     *
     * @param order The order to add to the orders database.
     * @param noOrderPresenceCheck If true, the order already present in the database check is not performed. Used to load orders from the demo file.
     * @param writeOnFile If true, the order is written on the orders database file.
     *
     * @throws InvalidOrder If the order already exists in the database.
     * @throws NullPointerException If the order or the no order presence check are null.
     * @throws NoSuchMethodException If the loadOrders() method is not found or if the writeOrderOnFile() method in this class is not found in the DBOrdersInterface class.
     * @throws IOException If an error occurs while writing the order on the orders database file.
     * @throws IllegalStateException If the orders database file content is not loaded.
     * @throws JsonSyntaxException If the orders database file content is not valid JSON.
     *
     */
    public static void addOrder(Order order, Boolean noOrderPresenceCheck, Boolean writeOnFile) throws InvalidOrder, NullPointerException, NoSuchMethodException, IOException, IllegalStateException, JsonSyntaxException {

        synchronized (Orders.class) {

            // Null checks.
            if (order == null) {
                throw new NullPointerException("Order to add to the database cannot be null.");
            }
            if (noOrderPresenceCheck == null) {
                throw new NullPointerException("No order presence check flag in adding an order to the orders database cannot be null.");
            }
            if (writeOnFile == null) {
                throw new NullPointerException("Write on file flag in adding an order to the orders database cannot be null.");
            }

            synchronized (order) {

                // Already exists check.
                if (noOrderPresenceCheck == false && orders.contains(order)) {
                    throw new InvalidOrder("Order to add to the orders database already exists.");
                }

                // Adds order to the TreeSet.
                Boolean added = orders.add(order);
                if (added == false) {
                    ordersDuplicates.add(order);
                }

                // Prevent double file writes when the method is called from DBOrdersInterface.loadOrders().
                // That's because:
                // DBOrdersInterface.loadOrders() read from file order X -> call addOrder() to add it in RAM -> writeOrderOnFile() write order X on file AGAIN.
                String method = null;
                try {   
                    method = DBOrdersInterface.class.getMethod(LOADORDERS_METHOD_NAME, Boolean.class, Boolean.class).getName();
                }catch (NoSuchMethodException ex) {
                    throw new NoSuchMethodException(String.format("Method %s in the Orders class not found.", method));
                }

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
                    if (writeOnFile)
                        DBOrdersInterface.writeOrderOnFile(order);
                } catch (IllegalStateException ex) {

                    // Remove order from TreeSet.
                    orders.remove(order);
                    if (added == false) {
                        ordersDuplicates.remove(order);
                    }

                    // Forwarding the exception's message.
                    throw new IllegalStateException(ex.getMessage());

                } catch (IOException ex) {

                    // Remove order from TreeSet.
                    orders.remove(order);
                    if (added == false) {
                        ordersDuplicates.remove(order);
                    }

                    // Forwarding the exception's message.
                    throw new IOException(ex.getMessage());

                } catch (JsonSyntaxException ex) {

                    // Remove order from TreeSet.
                    orders.remove(order);
                    if (added == false) {
                        ordersDuplicates.remove(order);
                    }

                    // Forwarding the exception's message.
                    throw new JsonSyntaxException(ex.getMessage());

                }

            }

        }

    }

    /**
     *
     * Loads all the orders from the JSON orders database file in the TreeSet in memory.
     *
     * Synchronized ON CLASS method to prevent multiple threads to load orders from the file at the same time.
     *
     * It's a wrapper method for the DBOrdersInterface.loadOrders() method.
     *
     * @param noPriceCoherenceChecks If true, the price coherence checks are not performed during the orders creation. Used to load orders from the demo file.
     * @param noOrderPresenceCheck If true, the order already present in the database check is not performed. Used to load orders from the demo file.
     *
     * @throws IllegalStateException If the file is not readed or the orders are already loaded.
     * @throws JsonSyntaxException If there's an error parsing the JSON orders database file content.
     * @throws InvalidOrder If the order already exists in the database and the no order presence check flag is false.
     * @throws IOException If an error occurs while writing the order on the orders database file.
     * @throws NullPointerException If the no price coherence checks or no order presence check flag are null.
     *
     */
    public static void loadOrders(Boolean noPriceCoherenceChecks, Boolean noOrderPresenceCheck) throws IllegalStateException, JsonSyntaxException, InvalidOrder, IOException, NoSuchMethodException, NullPointerException {

        synchronized (Orders.class) {

            // Simply backward the exceptions to the caller.
            DBOrdersInterface.loadOrders(noPriceCoherenceChecks, noOrderPresenceCheck);

        }

    }

    // GETTERS
    /**
     *
     * Finds an order with its order's id as Long in the orders database.
     *
     * @param orderId The order's id of the order to find as a Long in the orders database.
     *
     * @return An Order object found with the given order's id if the order is found in the orders database, null otherwise.
     *
     * @throws NullPointerException If the order's id is null.
     * @throws IllegalStateException If the orders are not loaded from the database orders file yet.
     *
     */
    public static Order getOrderById(Long orderId) throws NullPointerException, IllegalStateException {

        // Null check.
        if (orderId == null) {
            throw new NullPointerException("Order's id of the order to search in the orders database cannot be null.");
        }

        // Orders not loaded from the database orders file yet check.
        if (DBOrdersInterface.ordersLoaded() == false) {
            throw new IllegalStateException("Orders not loaded from the database orders file yet, needed to search for an order by its id. Call loadOrders() before.");
        }

        Currency primaryCurrency = Currency.getDefaultPrimaryCurrency();
        Currency secondaryCurrency = Currency.getDefaultSecondaryCurrency();
        OrderBook orderBook = OrderBook.getOrderBookByCurrencies(Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());
        if (orderBook == null) {
            orderBook = OrderBook.getMainOrderBook();
        }
        if (orderBook != null) {
            primaryCurrency = orderBook.getPrimaryCurrency();
            secondaryCurrency = orderBook.getSecondaryCurrency();
        }

        // Used a lot of placeholders here, but it's not important since the comparison is done only on the id.
        SpecificPrice price = new SpecificPrice(1, PriceType.ASK, primaryCurrency, secondaryCurrency);
        Quantity quantity = new Quantity(1);
        LimitOrder toSearchLimit = new LimitOrder(price, quantity);
        toSearchLimit.setId(orderId);

        Order toSearch = toSearchLimit;
        Order result = orders.ceiling(toSearch);
        if (result != null && result.getId().longValue() == orderId) {
            return result;
        }

        return null;

    }

    /**
     *
     * Get the size of the orders database.
     *
     * @return The size of the orders database (number of orders) as an Integer.
     *
     */
    public static Integer getOrdersSize() {

        // If the database is not loaded yet, return 0, do not throw an exception, otherwise problems during the orders loading.

        return (Integer) orders.size() + ordersDuplicates.size();

    }

    // TO STRING
    /**
     *
     * Get a string with the whole orders database. Each order is on a new line.
     *
     * Synchronized ON CLASS method to prevent multiple threads to modify the orders database during the execution of this method.
     *
     * @return The orders database as list of string lines, joined in an unique string by '\n'.
     *
     * @throws IllegalStateException If the orders are not loaded from the database orders file yet.
     *
     */
    public static String toStringOrders() throws IllegalStateException {

        synchronized (Orders.class) {

            // Orders not loaded from the database orders file yet.
            if (DBOrdersInterface.ordersLoaded() == false) {
                throw new IllegalStateException("Orders not loaded from the database orders file yet. Call loadOrders() before.");
            }

            String result = "";
            for (Order order : orders) {
                // To string is itself synchronized on the order.
                result += order.toString() + "\n";
            }
            for (Order order : ordersDuplicates) {
                // To string is itself synchronized on the order.
                result += order.toString() + "\n";
            }

            return result;

        }

    }

    public static PriceHistoryResponse getPriceHistory(String month) {

        PriceHistoryResponse priceHistoryResponse = new PriceHistoryResponse();

        synchronized (Orders.class) {

            // Orders not loaded from the database orders file yet.
            if (DBOrdersInterface.ordersLoaded() == false) {
                throw new IllegalStateException("Orders not loaded from the database orders file yet. Call loadOrders() before.");
            }

            LinkedList<Order> allOrders = new LinkedList<>(orders);
            allOrders.addAll(ordersDuplicates);

            // removing orders with no timestamp (if present).
            List<Order> filtered = allOrders.stream().filter(order -> order.getTimestamp() != null).collect(Collectors.toList());

            // removing orders that are not market orders and all ask market orders, to process only bid prices.
            filtered = filtered.stream().filter(order -> order.getOrderType() != OrderType.LIMIT).collect(Collectors.toList());
            filtered = filtered.stream().filter(order -> order.getOrderType() != OrderType.STOP).collect(Collectors.toList());
            List<MarketOrder> marketOrders = new LinkedList<MarketOrder>();
            for (Order order : filtered) {
                if (order instanceof MarketOrder && ((MarketOrder)order).getMarketOrderPriceType() != PriceType.ASK) {
                    marketOrders.add((MarketOrder) order);
                }
            }

            ZoneId gmtZone = ZoneId.of("GMT");
            Map<LocalDate, List<MarketOrder>> groupedByDay = marketOrders.stream().collect(Collectors.groupingBy(order -> Instant.ofEpochSecond(order.getTimestamp()).atZone(gmtZone).toLocalDate()));

            // filter by month and year
            int monthint = Integer.parseInt(month.substring(0, 2));
            int yearint = Integer.parseInt(month.substring(2));
            Iterator<Map.Entry<LocalDate, List<MarketOrder>>> iterator = groupedByDay.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<LocalDate, List<MarketOrder>> entry = iterator.next();
                LocalDate date = entry.getKey();
                if (date.getMonthValue() != monthint || date.getYear() != yearint) {
                    iterator.remove();
                }
            }

            groupedByDay.forEach((date, ordersList) -> {

                ordersList.sort(Comparator.comparing(Order::getTimestamp));

                // inverted max min since the compareTo of prices is inverted.
                MarketOrder minPriceOrder = ordersList.stream().max(Comparator.comparing(MarketOrder::getExecutionPrice)).orElse(null);
                MarketOrder maxPriceOrder = ordersList.stream().min(Comparator.comparing(MarketOrder::getExecutionPrice)).orElse(null);
                MarketOrder openPriceOrder = ordersList.getFirst();
                MarketOrder closePriceOrder = ordersList.getLast();

                DailyPriceStats dailyPriceStats = new DailyPriceStats(date.atStartOfDay(gmtZone).toInstant().toEpochMilli(), maxPriceOrder.getExecutionPrice(), minPriceOrder.getExecutionPrice(), openPriceOrder.getExecutionPrice(), closePriceOrder.getExecutionPrice());
                priceHistoryResponse.addDailyPriceStats(dailyPriceStats);
                
            });

            return priceHistoryResponse; 

        }
        
    }

    public static Boolean removeOrderById(Long orderId) {

        Order order = getOrderById(orderId);
        if (order == null)
            return false;
        orders.remove(order);
        if (ordersDuplicates.contains(order)) {
            ordersDuplicates.remove(order);
        }
        return true;

    }

}
