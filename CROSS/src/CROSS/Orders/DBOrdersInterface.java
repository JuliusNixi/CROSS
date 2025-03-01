package CROSS.Orders;

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
import CROSS.Types.Quantity;
import CROSS.Types.Price.GenericPrice;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;
import CROSS.Utils.FileHandler;
import CROSS.Client.ClientActionsUtils;
import CROSS.OrderBook.*;

/**
 * 
 * This class is an interface to handle orders database file.
 * It's used by the Orders class as support to load and save orders from and to a JSON orders database file.
 * 
 * Abstract class because I assume that I don't want to handle different orders databases at the same time.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Order
 * 
 * @see Orders
 * 
 */
public abstract class DBOrdersInterface {

    // Database file path.
    private static String filePath = null;

    // Orders database file to handle with streams.
    private static File file = null;
    private static FileInputStream fileIn = null;
    private static FileOutputStream fileOut = null;

    // Orders database file content as String.
    private static String fileContent = null;

    // Costants for file handling.
    private static final String FILE_READED = "loaded";
    private static final String FILE_INIT = "{\"trades\": [\n]\n}";

    // Orders loaded, true if the orders has been loaded from the database orders file, false otherwise.
    // Used in the Orders class to check if the orders has been already loaded before getting / searching an order.
    private static Boolean ordersLoaded = false;

    // FILE HANDLING
    /**
     * 
     * Set the orders database file to handle.
     * 
     * Synchronized to avoid multiple threads to set the file at the same time.
     * 
     * @param filePath The path to the orders database file as String.
     * 
     * @throws IllegalArgumentException If the file is not a JSON file.
     * @throws NullPointerException If the file path is null.
     * @throws RuntimeException If the file is already attached.
     * @throws IOException If there's an I/O error creating the file when not found.
     * 
     */
    public static synchronized void setFile(String filePath) throws IllegalArgumentException, NullPointerException, RuntimeException, IOException {

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
            throw new RuntimeException("Database orders file already attached.");
        }

        try {
            file = new File(filePath);
            fileIn = new FileInputStream(file);
            fileOut = new FileOutputStream(file, true);

            DBOrdersInterface.filePath = filePath;

            System.out.printf("DB Orders file %s attached.\n", filePath);
        } catch (FileNotFoundException ex) {

            System.out.printf("DB Orders file %s not found. Creating it.\n", filePath);

            // Create an empty file.
            try {
                file.createNewFile();
                fileIn = new FileInputStream(file);
                fileOut = new FileOutputStream(file, true);

                DBOrdersInterface.filePath = filePath;

                System.out.printf("DB Orders file %s created and attached.\n", filePath);
            } catch (IOException ex2) {
                throw new IOException("Error creating the database orders file.");
            }

        }

    }   
    /**
     * 
     * Read the file attached previously with setFile().
     * 
     * This fills the file content variable with the file content as String.
     * 
     * Synchronized to avoid multiple threads to read the file at the same time.
     * 
     * @throws RuntimeException If the file is not attached or the file content is already readed.
     * @throws IOException If there's an I/O error reading the file.
     * 
     */
    public static synchronized void readFile() throws IOException, RuntimeException {

        // File not attached.
        if (file == null || fileIn == null || DBOrdersInterface.filePath == null) {
            throw new RuntimeException("Database orders file not attached. Set file before with setFile().");
        }

        // File content already readed.
        if (DBOrdersInterface.fileContent != null) {
            throw new RuntimeException("Database orders file already readed.");
        }

        // Read file.
        // Buffered to improve performance.
        BufferedInputStream fileBuffered = new BufferedInputStream(fileIn);
        StringBuilder fileContentBuilder = new StringBuilder();
        Integer buffSize = 1024;
        byte[] buffer = new byte[buffSize];
        try {

            while (true) {
                int bytesRead = fileBuffered.read(buffer, 0, buffSize);
                if (bytesRead == -1) {
                    break;
                }
                fileContentBuilder.append(new String(buffer, 0, bytesRead));
            }

            DBOrdersInterface.fileContent = fileContentBuilder.toString();

            System.out.printf("DB Orders file %s readed.\n", filePath);
        } catch (IOException ex) {
            throw new IOException("Error reading the database orders file.");
        } 

    }

    // ON FILE ORDERS OPERATIONS
    /**
     * 
     * Write an order on the orders database file attached.
     * 
     * This appends the order to the orders database file, at the end, without rewriting all the file.
     * 
     * Synchronized to avoid multiple threads to write on the file at the same time.
     * 
     * @param order The Order to write.
     * @param <GenericOrder> The type of the Order to write. Limit, Market, StopMarket.
     * 
     * @throws RuntimeException If the file content is not loaded.
     * @throws NullPointerException If the order is null.
     * @throws IOException If there's an I/O error.
     * @throws JsonSyntaxException If there's an error parsing the JSON order to write in the database orders file.
     * 
     */
    public static synchronized <GenericOrder extends Order> void writeOrderOnFile(GenericOrder order) throws RuntimeException, NullPointerException, IOException, JsonSyntaxException {

        // Null check.
        if (order == null) {
            throw new NullPointerException("Order to write on file cannot be null.");
        }

        // File not attached.
        if (DBOrdersInterface.fileContent == null) {
            throw new RuntimeException("Orders database file content is needed to write an order on file. Call readFile() before.");
        }

        // Remove last 2 lines.
        try {
            FileHandler.removeLastLine(DBOrdersInterface.file);
            FileHandler.removeLastLine(DBOrdersInterface.file);
        } catch (IOException ex) {
            // Forwarding exception's message.
            throw new IOException(ex.getMessage());
        }

        // Write order on file.
        // Buffered to improve performance.
        try {
            BufferedOutputStream fileOutBuffered = new BufferedOutputStream(fileOut);

            String jsonOrder = new Gson().toJson(order);
            JsonObject jsonObject = null;

            // Removing the unnecessary / to modify fields from the JSON object.
            jsonObject = JsonParser.parseString(jsonOrder).getAsJsonObject();

            // Removing the "price" field.
            jsonObject.remove("price");

            // Removing the "quantity" field.
            jsonObject.remove("quantity");

            // Removing the "market" field.
            jsonObject.remove("market");

            // Removing the "user" field.
            jsonObject.remove("user");

            jsonOrder = jsonObject.toString();

            // Adding the missing fields to the JSON object.
            jsonObject = JsonParser.parseString(jsonOrder).getAsJsonObject();

            // Setting "type" field of the JSON object.
            String type = order.getPrice().getType().name().toLowerCase();
            jsonObject.addProperty("type", type);
            jsonOrder = jsonObject.toString();

            // Setting the "orderType" field of the JSON object.
            String orderType = order.getClass().getSimpleName().toLowerCase().replaceAll("order", "");
            if (orderType.contains("stop")) {
                orderType = orderType.replace("market", "");
            }
            jsonObject.addProperty("orderType", orderType);
            jsonOrder = jsonObject.toString();

            // Setting the "size" field of the JSON object.
            Integer size = order.getQuantity().getValue();
            jsonObject.addProperty("size", size);
            jsonOrder = jsonObject.toString();

            // Setting the "price" field of the JSON object.
            Integer price = order.getPrice().getValue();
            jsonObject.addProperty("price", price);
            jsonOrder = jsonObject.toString();

            jsonOrder = String.join("", jsonOrder.trim().split("\n"));
            if (DBOrdersInterface.fileContent.equals(FILE_INIT)) {
                /*
                 * {\n
                 *  "trades": [\n
                 * 
                 */
                jsonOrder = jsonOrder + "\n" + "]" + "\n}";
            } else {
                /* 
                 * [
                 *  {order1}\n
                 * 
                 */
                FileHandler.removeLastChar(DBOrdersInterface.file);
                jsonOrder = "," + "\n" + jsonOrder + "\n]" + "\n}";

            }

            // Append to the file.
            fileOutBuffered.write(jsonOrder.getBytes());
            fileOutBuffered.close();

            fileOut = new FileOutputStream(file, true);

        } catch (JsonSyntaxException | IndexOutOfBoundsException ex) {
            throw new JsonSyntaxException("Error parsing the JSON order to write in the orders database file.");
        } catch (IOException ex) {
            throw new IOException("Error writing the new order in the orders database file.");
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
     * Check if the orders has been already loaded from the orders database file.
     * 
     * @return True if the orders has been loaded from the orders database file, false otherwise.
     * 
     */
    public static Boolean ordersLoaded() {

        return DBOrdersInterface.ordersLoaded;
        
    }

    // MAIN SUPPORT METHOD
    /**
     * 
     * Load orders from the file (previously readed and stored in the file content variable) to Orders class (in RAM).
     * 
     * Synchronized to avoid multiple threads to load orders at the same time.
     * 
     * Before using this method, the file must be readed with readFile().
     * 
     * THIS METHOD IS MEANT TO BE USED ONLY AS SUPPORT FROM THE Orders CLASS.
     * CALL THIS METHOD FROM THE Orders CLASS.
     * 
     * @throws RuntimeException If the file is not readed or the orders are already loaded or if the main market is not set.
     * @throws JsonSyntaxException If there's an error parsing the JSON orders database file content.
     * @throws Exception If there's an error loading the orders from the JSON orders database file to the Orders class.
     * @throws IOException If there's an I/O error initializing the empty orders database file.
     * 
     */
    public static synchronized void loadOrders() throws RuntimeException, JsonSyntaxException, Exception, IOException {

        // Orders database file content not readed check.
        if (DBOrdersInterface.fileContent == null) {
            throw new RuntimeException("Database orders file not read. Read it before with readFile().");
        }

        // Orders already loaded.
        if (DBOrdersInterface.ordersLoaded() == true) {
            throw new RuntimeException("Orders database already loaded.");
        }

        // Empty file, initialize it.
        if (DBOrdersInterface.fileContent.isEmpty()) {
            try {
                BufferedOutputStream fileOutBuffered = new BufferedOutputStream(fileOut);
                fileOutBuffered.write(FILE_INIT.getBytes());

                fileOutBuffered.close();
                fileOut = new FileOutputStream(file, true);

                DBOrdersInterface.fileContent = FILE_INIT;
                DBOrdersInterface.ordersLoaded = true;

                System.out.printf("Empty DB Orders file %s. Initailized it.\n", filePath);

                return;
            } catch (IOException ex) {
                throw new IOException("Error initializing empty orders database file.");
            }
        }

        // IMPORTANT: The file could be already initialized before from the program, but no orders has been added yet.
        if (DBOrdersInterface.fileContent.equals(FILE_INIT)) {
            DBOrdersInterface.fileContent = FILE_INIT;

            System.out.printf("Orders loaded from file %s.\n", filePath);

            DBOrdersInterface.ordersLoaded = true;

            return;
        }

        // Not empty file.
        try {

            // Will contains the final order objects.
            LinkedList<Order> orders = new LinkedList<Order>();

            // Parse the JSON file content to a JSON object.
            JsonObject jsonObject = JsonParser.parseString(DBOrdersInterface.fileContent).getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("trades");

            // Iterate over the JsonArray object.
            for (JsonElement element : jsonArray) {
                // Convert each element to a JsonObject.
                jsonObject = element.getAsJsonObject();

                // Convert each JSON string to the corresponding object.
                String timestampStr = jsonObject.get("timestamp").getAsString();
                Long timestamp = null;
                try {
                    timestamp = Long.parseLong(timestampStr);
                } catch (NumberFormatException ex) {
                    throw new JsonSyntaxException("Error parsing the timestamp from the JSON orders database file.");
                }

                Market market = Market.getMainMarket();

                String type = jsonObject.get("type").getAsString();
                PriceType priceType = ClientActionsUtils.getPriceTypeFromString(type);

                String price = jsonObject.get("price").getAsString();
                GenericPrice genericPrice = ClientActionsUtils.getPriceFromString(price);
                SpecificPrice specificPrice = new SpecificPrice(genericPrice.getValue(), priceType, market);

                String size = jsonObject.get("size").getAsString();
                Quantity quantity = ClientActionsUtils.getSizeFromString(size);

                String orderId = jsonObject.get("orderId").getAsString();
                Integer id = ClientActionsUtils.getOrderIDFromString(orderId);

                // In the database orders file, the user is not stored.
                // Using a placeholder user.
                User user = new User("anonymous", "anonymous");

                Order o = null;
                String orderType = jsonObject.get("orderType").getAsString();
                switch (orderType) {

                    case "limit":
                        
                        LimitOrder limitOrder = new LimitOrder(specificPrice, quantity, user);
                        limitOrder.setId(id);
                        limitOrder.setTimestamp(timestamp);
                        o = limitOrder;
                        break;

                    case "market":

                        MarketOrder marketOrder = new MarketOrder(market, priceType, quantity, user);
                        marketOrder.setId(id);
                        marketOrder.setTimestamp(timestamp);
                        o = marketOrder;
                        break;

                    case "stop":

                        StopMarketOrder stopMarketOrder = new StopMarketOrder(specificPrice, quantity, user);
                        stopMarketOrder.setId(id);
                        stopMarketOrder.setTimestamp(timestamp);
                        o = stopMarketOrder;

                        break;
                
                    default:
                        break;

                }

                orders.add(o);

            }

            // Add orders to Orders (RAM).
            for (Order order : orders) {
                Orders.addOrder(order);
            }

            // File content is no longer needed.
            // To save memory.
            DBOrdersInterface.fileContent = FILE_READED;

            DBOrdersInterface.ordersLoaded = true;

            System.out.printf("Orders loaded from file %s.\n", filePath);
        } catch (JsonSyntaxException ex) {
            throw new JsonSyntaxException("Error parsing the JSON orders database file.");
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new JsonSyntaxException("Error parsing the JSON orders database file.");
        } catch (Exception ex) {
            // Forwarding exception's message.
            throw new Exception(ex.getMessage());
        }

    }

}
