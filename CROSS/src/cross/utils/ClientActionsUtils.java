package cross.utils;

import cross.client.Client;
import cross.orders.OrderType;
import cross.server.Server;
import cross.types.Quantity;
import cross.types.price.GenericPrice;
import cross.types.price.PriceType;
import cross.users.User;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.DateTimeException;

/**
 * 
 * This class contains some utility methods and strctures for parsing, checking and converting the client actions.
 * With "actions" we mean the commands that the client can send to the server as requests with the associated syntax and parameters.
 * 
 * It is used to parse and check the string commands given by the user from the client's CLI in the ClientCLIThread class.
 * 
 * It's also used to parse the requests from the server.
 * 
 * But it's also used in the APIs.
 * 
 * It is not instantiable. It only contains static methods, for this reason it is abstract.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Client.ClientCLIThread
 * @see API.Requests.Request
 * @see Server.ClientThread
 * @see Client.Client
 * 
 * @see ClientActions
 * 
 * @see PriceType
 * @see GenericPrice
 * @see Quantity
 * @see OrderType
 * @see User
 * 
 */
public abstract class ClientActionsUtils {

    /**
     * 
     * NEEDED PARSING, CHECKING AND CONVERSIONS:
     * 
     * TIMESTAMP -> NOT NEEDED
     * ORDERID -> OK
     * MONTHYEAR -> OK
     * OPERATION -> OK
     * SIZE -> OK
     * PRICE VALUE -> OK
     * PRICE TYPE ASK OR BID -> OK
     * ORDER TYPE LIMIT OR MARKET OR STOP -> OK
     * USERNAME -> OK
     * PASSWORD -> OK
     * 
     */

    // ACTIONS.

    // This enum represents the available actions for the client.
    // Public because it's used in the ClientCLIThread class but also in the API.
    public static enum ClientActions {
        // As described in the assignment.
        // User's data requests.
        REGISTER,
        LOGIN,
        UPDATE_CREDENTIALS,
        LOGOUT,

        // Orders requests.
        INSERT_MARKET_ORDER,
        INSERT_LIMIT_ORDER,
        INSERT_STOP_ORDER,
        CANCEL_ORDER,

        // Price history requests.
        GET_PRICE_HISTORY,

        // Added by me. To exit gracefully.
        EXIT
    }

    // Some mapping from client actions enum to command strings with the associated syntax.
    // Used by the below methods, these used in the ClientCLIThread class.
    private final static HashMap<ClientActions, String> commands = new HashMap<ClientActions, String>()
    {
        {

            put(ClientActions.REGISTER, "register(username, password)");
            put(ClientActions.LOGIN, "login(username, password)");
            put(ClientActions.UPDATE_CREDENTIALS, "updateCredentials(username, currentPassword, newPassword)");
            put(ClientActions.LOGOUT, "logout(username)"); 

            put(ClientActions.INSERT_LIMIT_ORDER, "insertLimitOrder(type, size, priceLimit)");
            put(ClientActions.INSERT_MARKET_ORDER, "insertMarketOrder(type, size)");
            put(ClientActions.INSERT_STOP_ORDER, "insertStopOrder(type, size, stopPrice)");
            put(ClientActions.CANCEL_ORDER, "cancelOrder(orderID)");

            put(ClientActions.GET_PRICE_HISTORY, "getPriceHistory(month)");

            put(ClientActions.EXIT, "exit()");
            
        }
    };

    /**
    * 
    * Check if the registered string commands associated with the client actions enum are valid.
    * 
    * Does not return anything, only throws an exception if the commands are invalid.
    *
    * Private because it's used only in this class.
    *
    * THIS IN NOT A "RUNTIME CHECK", IT'S A STATIC SYNTAX CHECK TO PREVENT HARD-CODING ABOVE INVALID STRING COMMANDS.
    * 
    * @throws IllegalStateException If the registered string commands associated with the client actions enum are invalid.
    * 
    */
    private static void syntaxRegisteredCommandsChecks() throws IllegalStateException {

        for (ClientActions actionEn : ClientActions.values()) {

            // Complete mapping check.
            if (!commands.containsKey(actionEn)){
                throw new IllegalStateException(String.format("Missing string command associated to %s client actions enum in the commands mapping.", actionEn.name()));
            }

            // Not empty command check.
            String cmd = commands.get(actionEn).toLowerCase().trim();
            if (cmd == null || cmd.isEmpty()){
                throw new IllegalStateException(String.format("Empty string command associated to %s client actions enum in the commands mapping.", actionEn.name()));
            }

            // Parenthesis checks.
            if (!cmd.endsWith(")")){
                throw new IllegalStateException(String.format("Invalid string command associated to %s client actions enum in the commands mapping, no ')' at the end.", actionEn.name()));
            }
            // A command cannot start with '('.
            if (cmd.startsWith("(")){
                throw new IllegalStateException(String.format("Invalid string command associated to %s client actions enum in the commands mapping, a command cannot start with '('.", actionEn.name()));
            }
            Integer openParenthesis = cmd.split("\\(").length - 1;
            // +1 because the split method returns 1 in this case, with a split on the last string character.
            Integer closeParenthesis = cmd.split("\\)").length - 1 + 1;
            if (openParenthesis != 1 || closeParenthesis != 1){
                throw new IllegalStateException(String.format("Invalid string command associated to %s client actions enum in the commands mapping, wrong number of parenthesis.", actionEn.name()));
            }

        }

    }
    
    // String command from client action enum.
    /**
     * 
     * Get the string command from the client action enum.
     * 
     * @param action The client action enum.
     * 
     * @return The command string associated with the client action enum.
     * 
     * @throws NullPointerException If the client action enum is null.
     * @throws IllegalStateException If the registered string commands associated with the client actions enum are invalid.
     * 
     */
    public static String getCommand(ClientActions action) throws NullPointerException, IllegalStateException {
        
        // Null check.
        if (action == null){
            throw new NullPointerException("Client action enum to be converted in string command cannot be null.");
        }

        // IMPORTANT: The checks are done here, remember to call this method before using the commands map.
        syntaxRegisteredCommandsChecks();

        // After the previous checks, the action is in the map for sure.
        return commands.get(action).trim();

    }

    // String command from client action enum, but without the parenthesis, only the keyword.
    /**
     * 
     * Get the keyword command string from the client action enum.
     * With keyword string command we mean the command string without the parenthesis and the parameters.
     * 
     * Used in the API's Request class.
     * 
     * @param action The client action enum.
     * 
     * @return The keyword string command as a string, associated with the client action enum.
     * 
     * @throws NullPointerException If the client action enum is null.
     * @throws IllegalStateException If the registered string commands associated with the client actions enum are invalid.
     * 
     */
    public static String getKeywordCommand(ClientActions action) throws NullPointerException, IllegalStateException {
        
        // Null check.
        if (action == null){
            throw new NullPointerException("Client action enum to be converted in string keyword command cannot be null.");
        }

        return ClientActionsUtils.getCommand(action).split("\\(")[0].trim();

    }
    
    // PARSING.

    // Client action enum from user given string command, ONLY CHECK THE COMMAND, NOT THE SYNTAX / PARAMETERS, FOR THAT USE THE BELOW METHODS.
    /**
     * 
     * Get the client action enum from a user given string command.
     * 
     * Note that this method only checks the command as keyword, not the syntax or the parameters of the whole command. For that, use the below methods.
     * 
     * @param command The string command.
     * 
     * @return The client action enum associated with the string command.
     * 
     * @throws NullPointerException If the string command is null.
     * @throws IllegalArgumentException If the string command is invalid.
     * @throws IllegalStateException If the registered string commands associated with the client actions enum are invalid.
     * 
     */
    public static ClientActions actionFromString(String command) throws NullPointerException, IllegalArgumentException, IllegalStateException {

        // Null check.
        if (command == null){
            throw new NullPointerException("String command to convert in client action enum cannot be null.");
        }

        command = command.toLowerCase().trim();
        for (ClientActions actionEn : ClientActions.values()) {
            if (command.startsWith(ClientActionsUtils.getKeywordCommand(actionEn).toLowerCase())){
                return actionEn;
            }
        }

        throw new IllegalArgumentException("Invalid string command to convert in client action enum, unknown command.");
        
    }

    // PRICE TYPE.
    // Price type (order type direction buy / sell).
    // Price type enum from price type string.
    /**
     * 
     * Get the price type enum from a price type string.
     * It matches the string with 'ask' or 'bid'.
     * 
     * @param priceType The price type string.
     * 
     * @return The PriceType enum.
     * 
     * @throws NullPointerException If the string price type is null.
     * @throws IllegalArgumentException If the string price type is invalid.
     * 
     */
    public static PriceType getPriceTypeFromString(String priceType) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (priceType == null){
            throw new NullPointerException("String price type (order type buy / sell) to convert from cannot be null.");
        }

        priceType = priceType.toLowerCase().trim();

        // Check the string.
        for (PriceType priceTypeEn : PriceType.values()) {
            if (priceType.compareTo(priceTypeEn.name().toLowerCase()) == 0){
                return priceTypeEn;
            }
        }

        throw new IllegalArgumentException("Invalid string price type (order type buy / sell) to convert from, allowed values are 'ask' and 'bid'.");
    
    }

    // PRICE.
    // GenericPrice object from price string.
    /**
     * 
     * Get the price as GenericPrice object from a price string.
     * 
     * @param price The string price.
     * 
     * @return The price as a GenericPrice object.
     * 
     * @throws NullPointerException If the string price is null.
     * @throws IllegalArgumentException If the string price is invalid.
     * 
     */
    public static GenericPrice getPriceFromString(String price) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (price == null){
            throw new NullPointerException("String price to convert from cannot be null.");
        }

        price = price.trim();

        // Conversion.
        try {
            Integer priceI = Integer.valueOf(price);

            // GenericPrice because the price is not associated with a type yet.
            return new GenericPrice(priceI);
        }catch (NumberFormatException ex){
            throw new IllegalArgumentException("Invalid string price format to convert from.");
        }catch (IllegalArgumentException ex){
            // If the price is negative.
            // Throwed by the GenericPrice constructor.
            throw new IllegalArgumentException("Invalid negative string price to convert from.");
        }

    }

    // QUANTITY.
    // Quantity object from quantity string.
    /**
     * 
     * Get the size (quantity) object from a quantity string.
     * 
     * @param size The string size.
     * 
     * @return The size as a Quantity object.
     * 
     * @throws NullPointerException If the string size is null.
     * @throws IllegalArgumentException If the string size is invalid.
     * 
     */
    public static Quantity getSizeFromString(String size) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (size == null){
            throw new NullPointerException("String size to convert from cannot be null.");
        }

        size = size.trim();

        // Conversion.
        try {
            Integer sizeI = Integer.valueOf(size);

            return new Quantity(sizeI);
        }catch (NumberFormatException ex){
            throw new IllegalArgumentException("Invalid string size format to convert from.");
        }catch (IllegalArgumentException ex){
            // If the size is negative.
            // Throwed by the Quantity constructor.
            throw new IllegalArgumentException("Invalid negative size to convert from.");
        }

    }

    // ORDER TYPE.
    // Order type enum from order type string.
    /**
     * 
     * Get the order type enum from an order type string.
     * 
     * The order type string is the type of the order, it can be "limit", "market" or "stop".
     * 
     * @param orderType The order type string.
     * 
     * @return The order type enum.
     * 
     * @throws NullPointerException If the string order type is null.
     * @throws IllegalArgumentException If the string order type is invalid.
     * 
     */
    public static OrderType getOrderTypeFromString(String orderType) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (orderType == null){
            throw new NullPointerException("String order type to convert from cannot be null.");
        }

        orderType = orderType.toLowerCase().trim();

        // Check the string.
        for (OrderType orderTypeEn : OrderType.values()) {
            if (orderType.compareTo(orderTypeEn.name().toLowerCase()) == 0){
                return orderTypeEn;
            }
        }

        throw new IllegalArgumentException("Invalid string order type to convert from, allowed values are 'limit', 'market' and 'stop'.");

    }

    // USER.
    // User object from username and password strings.
    /**
     * 
     * Get the user object from a username and password strings.
     * 
     * @param username The username string.
     * @param password The password string.
     * 
     * @return The user object.
     * 
     * @throws NullPointerException If the string username or password are null.
     * @throws IllegalArgumentException If the string username or password are invalid.
     * 
     */
    public static User getUserFromString(String username, String password) throws NullPointerException, IllegalArgumentException {

        // Null check.
        if (username == null || password == null){
            throw new NullPointerException("String username or password to convert from cannot be null.");
        }

        User user = null;
        try {
            user = new User(username, password);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid string username or password to convert from.");
        }

        return user;

    }
    
    // MONTH / YEAR.
    // Parse the month / year string in the format MMYYYY.
    /**
     * 
     * Parse the month / year string in the format MMYYYY.
     * 
     * Used to get the price history.
     * 
     * Does not returns anything, only throws an exception if the string format is invalid.
     * 
     * @param monthyear The month / year as string.
     * 
     * @throws NullPointerException If the month / year string is null.
     * @throws IllegalArgumentException If the month / year string is invalid.
     * 
     */
    public static void parseMonthFromString(String monthyear) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (monthyear == null){
            throw new NullPointerException("String month / year cannot be null.");
        }

        monthyear = monthyear.trim();

        // Parsing and checks.
        try {
            // Format:
            // MMYYYY

            // Manuals checks.
            if (monthyear.length() != 2 + 4)
                throw new NumberFormatException("Invalid string month / year format length.");

            String month = monthyear.substring(0, 2);
            String year = monthyear.substring(2);

            Integer yearI = Integer.valueOf(year);
            if (yearI < 0){
                throw new NumberFormatException("Invalid string month / year format, negative year.");
            }

            Integer monthI = Integer.valueOf(month);
            if (monthI < 1 || monthI > 12){
                throw new NumberFormatException("Invalid string month / year format, invalid month range.");
            }

            // Parsing the month string.
            // Automatic checks.
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("MMyyyy")
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter();

            try {
                // Parse the string to a LocalDate, assuming the first day of the month.
                LocalDate date = LocalDate.parse(monthyear, formatter);
                // Not used.
                date.get(ChronoField.MONTH_OF_YEAR);
            } catch (DateTimeException | ArithmeticException ex) {
                throw new NumberFormatException("Invalid string month / year format.");
            }

        }catch (NumberFormatException ex){
            // Forwards the exception's message.
            throw new IllegalArgumentException(ex.getMessage());
        }catch (IndexOutOfBoundsException ex){
            throw new IllegalArgumentException("Invalid string month / year format.");
        }

    }

    // ORDER ID.
    // Integer order ID from string order ID.
    /**
     * 
     * Get the order ID as a Number from a string order ID.
     * 
     * @param orderID The order ID as string.
     * 
     * @return The order ID as a Number.
     * 
     * @throws NullPointerException If the order ID string is null.
     * @throws IllegalArgumentException If the order ID string is invalid.
     * 
     */
    public static Number getOrderIDFromString(String orderID) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (orderID == null){
            throw new NullPointerException("String order ID to convert from cannot be null.");
        }

        orderID = orderID.trim();

        // Parsing and checks.
        Number orderIDI;
        NumberFormat format = NumberFormat.getInstance();
        try {
            orderIDI = format.parse(orderID);
        }catch (ParseException ex){
            throw new IllegalArgumentException("Invalid string order ID to convert from.");
        }

        return orderIDI;

    }
    
}
