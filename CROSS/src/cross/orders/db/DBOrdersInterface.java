package cross.orders.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import cross.exceptions.InvalidOrder;
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
import cross.utils.ClientActionsUtils;
import cross.utils.FileHandler;

/**
 *
 * This class is an interface to handle orders database file.
 * It's used by the Orders class as support to load and save orders from and to a JSON orders database file.
 *
 * Abstract class because I assume that I don't want to handle different orders databases at the same time.
 * So I will use only static methods and variables.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Order
 *
 * @see Orders
 *
 * @see FileHandler
 *
 */
public abstract class DBOrdersInterface {

    // Orders database file path.
    private static String filePath = null;

    // Orders database file to handle with streams.
    private static File file = null;
    private static FileInputStream fileIn = null;
    private static FileOutputStream fileOut = null;

    // Buffered to improve performance.
    private static BufferedInputStream fileInBuffered = null;
    private static BufferedOutputStream fileOutBuffered = null;

    // Orders database file content as String.
    private static String fileContent = null;

    // Costant for file handling.
    private static final String FILE_INIT = "{\"trades\": [\n]\n}";

    // Orders loaded, true if the orders has been loaded from the database orders file, false otherwise.
    // Used in the Orders class to check if the orders has been already loaded before getting / searching an order.
    private static Boolean ordersLoaded = false;

    // FILE HANDLING
    /**
     *
     * Sets the orders database file to handle.
     *
     * Synchronized ON CLASS to avoid multiple threads to set the file at the same time.
     * 
     * If the file is not found, it will be created with the initial content.
     *
     * @param filePath The path to the orders database file as String.
     *
     * @throws IllegalArgumentException If the file is not a JSON file.
     * @throws NullPointerException If the file path is null.
     * @throws IllegalStateException If the file is already attached.
     * @throws IOException If there's an I/O error creating the file when not found.
     *
     */
    public static void setFile(String filePath) throws IllegalArgumentException, NullPointerException, IllegalStateException, IOException {

        synchronized (DBOrdersInterface.class) {

            // Null check.
            if (filePath == null) {
                throw new NullPointerException("Database orders file path to set cannot be null.");
            }


            // Check if the file is a JSON file.
            if (!filePath.endsWith(".json")) {
                throw new IllegalArgumentException("Database orders file to set must be a JSON file.");
            }

            // Database file already attached.
            if (DBOrdersInterface.filePath != null) {
                throw new IllegalStateException("Database orders file already attached.");
            }

            try {
                file = new File(filePath);

                fileIn = new FileInputStream(file);
                fileInBuffered = new BufferedInputStream(fileIn);

                fileOut = new FileOutputStream(file, true);
                fileOutBuffered = new BufferedOutputStream(fileOut);

                DBOrdersInterface.filePath = filePath;

                System.out.printf("DB Orders file %s attached.\n", filePath);
            } catch (FileNotFoundException ex) {

                System.out.printf("DB Orders file %s not found. Creating it.\n", filePath);

                // Create an empty file.
                try {
                    file.createNewFile();

                    fileIn = new FileInputStream(file);
                    fileInBuffered = new BufferedInputStream(fileIn);

                    fileOut = new FileOutputStream(file, true);
                    fileOutBuffered = new BufferedOutputStream(fileOut);

                    fileOutBuffered.write(FILE_INIT.getBytes());
                    fileOutBuffered.flush();

                    DBOrdersInterface.filePath = filePath;

                    System.out.printf("DB Orders file %s created and attached.\n", filePath);
                } catch (IOException ex2) {
                    throw new IOException("Error creating the database orders file.");
                }

            }


        }

    }
    /**
     *
     * Reads the file attached, previously setted with setFile().
     *
     * This fills the file content variable with the file content as String.
     *
     * Synchronized ON CLASS to avoid multiple threads to read the file at the same time.
     *
     * @throws IllegalStateException If the file is not attached or the file content is already readed.
     * @throws IOException If there's an I/O error reading the file.
     *
     */
    public static void readFile() throws IOException, IllegalStateException {

        synchronized (DBOrdersInterface.class) {

            // File not attached.
            if (file == null || fileIn == null || DBOrdersInterface.filePath == null) {
                throw new IllegalStateException("Database orders file not attached. Set file before with setFile().");
            }

            // File content already readed.
            if (DBOrdersInterface.fileContent != null) {
                throw new IllegalStateException("Database orders file already readed.");
            }

            // Read file.
            StringBuilder fileContentBuilder = new StringBuilder();
            Integer buffSize = 1024;
            byte[] buffer = new byte[buffSize];
            try {

                while (true) {
                    int bytesRead = fileInBuffered.read(buffer, 0, buffSize);
                    if (bytesRead == -1) {
                        // End of file.
                        break;
                    }
                    fileContentBuilder.append(new String(buffer, 0, bytesRead));
                }

                DBOrdersInterface.fileContent = fileContentBuilder.toString();

                System.out.printf("DB Orders file %s readed.\n", filePath);
            } catch (IOException | IndexOutOfBoundsException ex) {
                throw new IOException("Error reading the database orders file.");
            }

        }

    }

    // ON FILE ORDERS OPERATIONS
    /**
     *
     * Appends an order on the orders database file attached.
     *
     * This appends the order to the orders database file, at the end, without rewriting all the file.
     *
     * Synchronized ON CLASS to avoid multiple threads to write on the file at the same time.
     * Syncronized ON ORDER to avoid multiple threads to modify the order's properties during the execution of this method.
     *
     * @param order The Order to write (append) to the orders database file.
     *
     * @throws IllegalStateException If the file content is not loaded.
     * @throws NullPointerException If the order is null.
     * @throws IOException If there's an I/O error.
     * @throws JsonSyntaxException If the order is not a valid JSON object or if the order's properties are not valid.
     *
     */
    public static void writeOrderOnFile(Order order) throws NullPointerException, IOException, JsonSyntaxException {

        synchronized (DBOrdersInterface.class) {

            // Null check.
            if (order == null) {
                throw new NullPointerException("Order to append to the orders database file cannot be null.");
            }

            synchronized (order) {

                // File not attached.
                if (DBOrdersInterface.fileContent == null) {
                    throw new IllegalStateException("Orders database file content is needed to append an order to the orders database file. Call readFile() before.");
                }

                String newFileContent = String.format("%s", DBOrdersInterface.fileContent);

                // Remove last 2 lines.
                try {
                    FileHandler.removeLastLine(DBOrdersInterface.file);
                    FileHandler.removeLastLine(DBOrdersInterface.file);

                    // Updating also the file content to keep it in sync with the file on disk without reading it again.
                    int lastNewLine = newFileContent.lastIndexOf('\n');
                    lastNewLine = newFileContent.substring(0, lastNewLine).lastIndexOf('\n');
                    if (lastNewLine == -1) {
                        throw new IOException();
                    }
                    newFileContent = newFileContent.substring(0, lastNewLine);
                } catch (IOException | IndexOutOfBoundsException ex) {
                    throw new IOException("Error removing the last line from the orders database file.");
                }

                // Append order on file.
                try {

                    String jsonOrder = new Gson().toJson(order);
                    
                    // {"orderId": 3, "type": "bid" , "orderType": "market", "size": 614, "price": 56000000, "timestamp": 1725149122}
                    JsonObject jsonObject = JsonParser.parseString(jsonOrder).getAsJsonObject();
                    int size = jsonObject.get("size").getAsJsonObject().get("quantity").getAsInt();
                    jsonObject.remove("size");
                    jsonObject.remove("initialFixedSize");
                    jsonObject.addProperty("size", size);

                    String orderType = jsonObject.get("orderType").getAsString().toLowerCase();
                    jsonObject.remove("orderType");
                    if (orderType.compareTo("market") == 0 && order instanceof MarketOrder) {
                        if (((MarketOrder) order).getComingFromStopOrderId() == null)
                            // market.
                            jsonObject.addProperty("orderType", orderType);
                        else
                            jsonObject.addProperty("orderType", "stop");
                    }else{
                        // limit.
                        jsonObject.addProperty("orderType", orderType);
                    }

                    int price = jsonObject.get("price").getAsJsonObject().get("price").getAsInt();
                    String type = jsonObject.get("price").getAsJsonObject().get("type").getAsString().toLowerCase();
                    jsonObject.addProperty("type", type);
                    jsonObject.remove("price");
                    if (orderType.compareTo("market") == 0 && order instanceof MarketOrder) {
                        MarketOrder marketOrder = (MarketOrder) order;
                        price = marketOrder.getExecutionPrice().getValue();
                    }
                    jsonObject.addProperty("price", price);

                    JsonElement orderIdeleL = jsonObject.get("orderIdL");
                    Number orderId = null;
                    if (orderIdeleL != null) {
                        jsonObject.remove("orderIdL");
                    } else {
                        jsonObject.remove("orderId");
                    }
                    if (orderType.compareTo("market") == 0 && order instanceof MarketOrder) {
                        MarketOrder marketOrder = (MarketOrder) order;
                        if (marketOrder.getComingFromStopOrderId() != null) {
                            // stop now market order.
                            orderId = marketOrder.getComingFromStopOrderId().longValue();
                        } else {
                            // normal market order.
                            orderId = marketOrder.getId();
                        }
                    }else{
                        // limit or stop order.
                        orderId = order.getId();
                    }
                    jsonObject.addProperty("orderId", orderId);

                    jsonOrder = new Gson().toJson(jsonObject);

                    // Remove all the '\n' from the JSON order.
                    jsonOrder = String.join("", jsonOrder.trim().split("\n"));
                    if (DBOrdersInterface.fileContent.compareTo(FILE_INIT) == 0) {
                        // First order.
                        /*
                        * {\n
                        *  "trades": [\n
                        *
                        */
                        jsonOrder = jsonOrder + "\n" + "]" + "\n}";
                    } else {
                        // Other orders.
                        /*
                        * [
                        *  {order1}\n
                        *
                        */

                        // Need to remove the last char "\n" before writing the ','.
                        FileHandler.removeLastChar(DBOrdersInterface.file);
                        try {
                            newFileContent = newFileContent.substring(0, newFileContent.length() - 1);
                        } catch (IndexOutOfBoundsException ex) {
                            throw new IOException();
                        }

                        jsonOrder = """
                                    ,
                                    """ + jsonOrder + "\n]" + "\n}";

                    }

                    // Append to the file.
                    fileOutBuffered.write(jsonOrder.getBytes());
                    fileOutBuffered.flush();

                    // Update the file content by adding the new user.
                    newFileContent = newFileContent + jsonOrder;

                    // Update the main file content variable.
                    DBOrdersInterface.fileContent = newFileContent;

                } catch (IOException ex) {
                    throw new IOException("Error appending the new order to the orders database file.");
                } catch (JsonSyntaxException | IllegalStateException | NullPointerException | UnsupportedOperationException | NumberFormatException ex) {
                    throw new JsonSyntaxException("Error parsing the order to JSON.");
                }

            }

        }

    }

    // GETTERS
    /**
     *
     * Get the orders database file path.
     *
     * @return The orders database file path as String.
     *
     */
    public static String getFilePath() {

        return String.format("%s", filePath);

    }
    /**
     *
     * Check if the orders have been already loaded from the orders database file in the Orders class.
     *
     * @return True if the orders have been loaded from the orders database file, false otherwise.
     *
     */
    public static Boolean ordersLoaded() {

        return DBOrdersInterface.ordersLoaded;


    }

    // MAIN SUPPORT (CALLED FROM THE Orders CLASS) METHOD
    /**
     *
     * Load orders from the orders database file (previously readed and stored in the file content variable) to Orders class (in RAM).
     *
     * Synchronized ON CLASS to avoid multiple threads to load orders at the same time.
     *
     * Before using this method, the file must be readed with readFile().
     *
     * THIS METHOD IS MEANT TO BE USED ONLY AS SUPPORT FROM THE Orders CLASS.
     * CALL THIS METHOD FROM THE Orders CLASS.
     *
     * ALL ORDERS LOADED FROM FILE ARE ASSIGNED TO THE MAIN MARKET AND TO AN ANONYMOUS USER.
     *
     * @param noPriceCoherenceChecks If true, the price coherence checks are not performed during the orders creation. Used to load orders from the demo file.
     * @param noOrderPresenceCheck If true, the order already present in the database check is not performed. Used to load orders from the demo file.
     *
     * @throws IllegalStateException If the file is not readed or the orders are already loaded or if the no price coherence checks flag is false and no order book is found for the default currencies or if there is an error parsing the JSON orders database file content.
     * @throws JsonSyntaxException If there's an error parsing the JSON orders database file content.
     * @throws InvalidOrder If the order already exists in the database and the no order presence check flag is false.
     * @throws IOException If an error occurs while writing the order on the orders database file.
     * @throws NullPointerException If the no price coherence checks or no order presence check flag are null.
     * @throws IllegalArgumentException If the file content is not a valid JSON object.
     * @throws NoSuchMethodException If the loadOrders() method is not found or if the writeOrderOnFile() method in this class is not found in the DBOrdersInterface class.
     *
     */
    public static void loadOrders(Boolean noPriceCoherenceChecks, Boolean noOrderPresenceCheck) throws IllegalStateException, JsonSyntaxException, InvalidOrder, IOException, NullPointerException, IllegalArgumentException, NoSuchMethodException {

        synchronized (DBOrdersInterface.class) {

            // Null check.
            if (noPriceCoherenceChecks == null) {
                throw new NullPointerException("No price coherence check flag in the orders loading cannot be null.");
            }
            if (noOrderPresenceCheck == null) {
                throw new NullPointerException("No order presence check flag in the orders loading cannot be null.");
            }

            // Orders database file content not readed check.
            if (DBOrdersInterface.fileContent == null) {
                throw new IllegalStateException("Database orders file not read. Read it before with readFile().");
            }

            // Orders already loaded.
            if (DBOrdersInterface.ordersLoaded() == true) {
                throw new IllegalStateException("Orders database already loaded.");
            }

            // IMPORTANT: The file could be already initialized before from the program, but no orders has been added yet.
            if (DBOrdersInterface.fileContent.compareTo(FILE_INIT) == 0) {

                DBOrdersInterface.ordersLoaded = true;

                System.out.printf("Orders loaded from DB Orders file %s.\n", DBOrdersInterface.filePath);

                return;
            }

            // Not empty file.
            Currency primaryCurrency = Currency.getDefaultPrimaryCurrency();
            Currency secondaryCurrency = Currency.getDefaultSecondaryCurrency();
            if (noPriceCoherenceChecks == false) {
                OrderBook orderBook = OrderBook.getOrderBookByCurrencies(primaryCurrency, secondaryCurrency);
                if (orderBook == null) {
                    orderBook = OrderBook.getMainOrderBook();
                }
                if (orderBook == null) {
                    throw new IllegalStateException("No order book found for the default currencies.");
                }
                if (orderBook.getPrimaryCurrency() != null) {
                    primaryCurrency = orderBook.getPrimaryCurrency();
                    secondaryCurrency = orderBook.getSecondaryCurrency();
                }
            }
            try {

                // Will contains the final order objects.
                LinkedList<Order> orders = new LinkedList<>();

                // {"orderId": 3, "type": "bid" , "orderType": "market", "size": 614, "price": 56000000, "timestamp": 1725149122}

                // Parse the JSON file content to a JSON object.
                JsonObject jsonObject = JsonParser.parseString(DBOrdersInterface.fileContent).getAsJsonObject();
                JsonArray jsonArray = jsonObject.getAsJsonArray("trades");

                // Iterate over the JsonArray object.
                for (JsonElement element : jsonArray) {
                    // Convert each element to a JsonObject.
                    jsonObject = element.getAsJsonObject();

                    // Convert each JSON string to the corresponding object.
                    Long timestamp = null;
                    if (jsonObject.get("timestamp") != null) {
                        String timestampStr = jsonObject.get("timestamp").getAsString();
                        try {
                            timestamp = Long.valueOf(timestampStr);
                        } catch (NumberFormatException ex) {
                            throw new NumberFormatException("Error parsing the timestamp from the JSON orders database file.");
                        }
                    }

                    String type = jsonObject.get("type").getAsString();
                    PriceType priceType = ClientActionsUtils.getPriceTypeFromString(type);

                    String price = jsonObject.get("price").getAsString();
                    GenericPrice genericPrice = ClientActionsUtils.getPriceFromString(price);
                    SpecificPrice specificPrice = new SpecificPrice(genericPrice.getValue(), priceType, primaryCurrency, secondaryCurrency);

                    String size = jsonObject.get("size").getAsString();
                    Quantity quantity = ClientActionsUtils.getSizeFromString(size);

                    String orderId = jsonObject.get("orderId").getAsString();
                    Number id = ClientActionsUtils.getOrderIDFromString(orderId);

                    Order order;
                    String orderType = jsonObject.get("orderType").getAsString();
                    switch (orderType) {

                        case "limit":

                            LimitOrder limitOrder = new LimitOrder(specificPrice, quantity, noPriceCoherenceChecks);
                            limitOrder.setQuantity(new Quantity(0));
                            limitOrder.setId(id.longValue());
                            if (timestamp != null) {
                                limitOrder.setTimestamp(timestamp);
                            }
                            order = limitOrder;
                            
                            break;

                        case "market":

                            PriceType invertedPriceType = priceType == PriceType.BID ? PriceType.ASK : PriceType.BID;
                            specificPrice = new SpecificPrice(genericPrice.getValue(), invertedPriceType, primaryCurrency, secondaryCurrency);
                            MarketOrder marketOrder = new MarketOrder(priceType, primaryCurrency, secondaryCurrency, quantity);
                            marketOrder.setQuantity(new Quantity(0));
                            marketOrder.setId(id.longValue());
                            if (timestamp != null) {
                                marketOrder.setTimestamp(timestamp);
                            }
                            marketOrder.setExecutionPrice(specificPrice);
                            order = marketOrder;

                            break;

                        case "stop":

                            StopOrder stopOrder = new StopOrder(specificPrice, quantity, noPriceCoherenceChecks);
                            stopOrder.setQuantity(new Quantity(0));
                            stopOrder.setId(id.longValue());
                            if (timestamp != null) {
                                stopOrder.setTimestamp(timestamp);
                            }
                            order = stopOrder;

                            break;

                        default:

                            // NumberFormatException is thrown to forward the error message.
                            throw new NumberFormatException("Invalid order type in the JSON orders database file.");

                    }

                    orders.add(order);

                }

                // Add orders to Orders (RAM).
                for (Order order : orders) {
                    Orders.addOrder(order, noOrderPresenceCheck, true);
                }

                // Exceptions throwed by the addOrder() method are backwarded to the caller.

                DBOrdersInterface.ordersLoaded = true;

                System.out.printf("Orders loaded from DB Orders file %s.\n", DBOrdersInterface.filePath);
            } catch (NumberFormatException ex) {
                // Forwarding exception's message.
                throw new JsonSyntaxException(ex.getMessage());
            } catch (NullPointerException | JsonSyntaxException | UnsupportedOperationException ex) {
                throw new JsonSyntaxException("Error parsing the JSON orders database file.");
            } catch (InvalidOrder ex) {
                throw new InvalidOrder(ex.getMessage());
            } catch (IOException ex) {
                throw new IOException("Error loading the orders from the orders database file.");
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(ex.getMessage());
            } catch (IllegalStateException ex) {
                throw new IllegalStateException(ex.getMessage());
            } catch (NoSuchMethodException ex) {
                throw new NoSuchMethodException("Error getting the order ID from the JSON orders database file.");
            }

        }

    }

}
