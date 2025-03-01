package CROSS.Client;

import java.util.HashMap;
import java.util.LinkedList;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.GenericPrice;

/**
 * 
 * This class contains some utility methods for parsing and checking the client actions.
 * With "actions" we mean the commands that the client can send to the server as requests.
 * 
 * It is used to parse and check the string commands given by the user from the client's CLI.
 * It's used in the ClientCLIThread class.
 * 
 * It's also used to parse the requests / responses from / to the server.
 * 
 * It is not instantiable. It only contains static methods, for this reason it is abstract.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see ClientCLIThread
 * 
 * @see ClientActions
 * 
 * @see PriceType
 * @see GenericPrice
 * @see Quantity
 * 
 */
public abstract class ClientActionsUtils {
    
    // This enum represents the available actions for the client.
    // Public because it's used in the ClientCLIThread class.
    public static enum ClientActions {
        // As described in the assignment.
        REGISTER,
        LOGIN,
        UPDATE_CREDENTIALS,
        LOGOUT,

        INSERT_MARKET_ORDER,
        INSERT_LIMIT_ORDER,
        INSERT_STOP_ORDER,
        CANCEL_ORDER,

        GET_PRICE_HISTORY,

        // Added by me. To exit gracefully.
        EXIT
    }

    // Some mapping from actions enum to command strings with the associated syntax.
    private static HashMap<ClientActions, String> commands = new HashMap<ClientActions, String>()
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
    * Check if the registered string commands associated with the actions enum are valid.
    * 
    * Does not return anything, only throws an exception if the commands are invalid.
    *
    * Private because it's used only in this class.
    * 
    * @throws RuntimeException If the registered string commands associated with the actions enum are invalid.
    * 
    */
    private static void syntaxRegisteredCommandsChecks() throws RuntimeException {

        for (ClientActions actionEn : ClientActions.values()) {

            // Complete mapping check.
            if (!commands.containsKey(actionEn)){
                throw new RuntimeException("Missing string command associated to actions enum in the commands mapping.");
            }

            // Not empty command check.
            String cmd = commands.get(actionEn).toLowerCase().trim();
            if (cmd == null || cmd.isEmpty()){
                throw new RuntimeException("Empty string command associated to actions enum in the commands mapping.");
            }

            // Parenthesis checks.
            if (!cmd.endsWith(")")){
                throw new RuntimeException("Invalid string command associated to actions enum in the commands mapping, no ')' at the end.");
            }
            Integer openParenthesis = cmd.split("\\(").length - 1;
            Integer closeParenthesis = cmd.split("\\)").length - 1;
            if (openParenthesis != 1 || closeParenthesis != 1){
                throw new RuntimeException("Invalid string command associated to actions enum in the commands mapping, wrong number of parenthesis.");
            }

        }

    }

    // ACTIONS.
    // String command from action enum.
    /**
     * 
     * Get the string command from the action enum.
     * 
     * @param action The action enum.
     * 
     * @return The command string associated with the action enum.
     * 
     * @throws NullPointerException If the action enum is null.
     * @throws RuntimeException If the registered string commands associated with the actions enum are invalid.
     * 
     */
    public static String getCommand(ClientActions action) throws NullPointerException, RuntimeException {
        
        // Null check.
        if (action == null){
            throw new NullPointerException("Action enum to be converted in string command cannot be null.");
        }

        // IMPORTANT: The checks are done here, remember to call this method before using the commands map.
        syntaxRegisteredCommandsChecks();

        // After the previous checks, the action is in the map for sure.
        return commands.get(action).trim();

    }
    // String command from action enum, but without the parenthesis, only the keyword.
    /**
     * 
     * Get the keyword command string from the action enum.
     * With keyword string command we mean the command string without the parenthesis and the parameters.
     * 
     * @param action The action enum.
     * 
     * @return The keyword string command as a string, associated with the action enum.
     * 
     * @throws NullPointerException If the action enum is null.
     * @throws RuntimeException If the registered string commands associated with the actions enum are invalid.
     * 
     */
    public static String getKeywordCommand(ClientActions action) throws NullPointerException, RuntimeException {
        
        // Null check.
        if (action == null){
            throw new NullPointerException("Action enum to be converted in string keyword cannot be null.");
        }

        return ClientActionsUtils.getCommand(action).split("\\(")[0].trim();

    }
    // Action enum from user given string command, ONLY CHECK THE COMMAND, NOT THE SYNTAX/PARAMETERS, FOR THAT USE THE BELOW METHODS.
    /**
     * 
     * Get the action enum from a user given string command.
     * 
     * Note that this method only checks the command as keyword, not the syntax or the parameters of the whole command. For that, use the below methods.
     * 
     * @param command The string command.
     * 
     * @return The action enum associated with the string command.
     * 
     * @throws NullPointerException If the string command is null.
     * @throws IllegalArgumentException If the string command is invalid.
     * @throws RuntimeException If the registered string commands associated with the actions enum are invalid.
     * 
     */
    public static ClientActions actionFromString(String command) throws NullPointerException, IllegalArgumentException, RuntimeException {

        // Null check.
        if (command == null){
            throw new NullPointerException("String command to convert in action enum cannot be null.");
        }

        command = command.toLowerCase().trim();
        for (ClientActions actionEn : ClientActions.values()) {
            if (command.startsWith(ClientActionsUtils.getKeywordCommand(actionEn).toLowerCase() + "(")){
                return actionEn;
            }
        }

        throw new IllegalArgumentException("Invalid string command.");
        
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
            throw new NullPointerException("String price type (order type buy / sell) cannot be null.");
        }

        priceType = priceType.toLowerCase().trim();

        // Check the string.
        for (PriceType priceTypeEn : PriceType.values()) {
            if (priceType.equals(priceTypeEn.name().toLowerCase())){
                return priceTypeEn;
            }
        }

        throw new IllegalArgumentException("Invalid string price type (order type buy / sell).");
    
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
            throw new NullPointerException("String price cannot be null.");
        }

        price = price.trim();

        // Conversion.
        try {
            Integer priceI = Integer.parseInt(price);

            // GenericPrice because the price is not associated with a type yet.
            return new GenericPrice(priceI);
        }catch (NumberFormatException ex){
            throw new IllegalArgumentException("Invalid string price.");
        }catch (IllegalArgumentException ex){
            // If the price is negative.
            // Throwed by the GenericPrice constructor.
            throw new IllegalArgumentException("Invalid negative string price.");
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
            throw new NullPointerException("String size cannot be null.");
        }

        size = size.trim();

        // Conversion.
        try {
            Integer sizeI = Integer.parseInt(size);

            return new Quantity(sizeI);
        }catch (NumberFormatException ex){
            throw new IllegalArgumentException("Invalid string size.");
        }catch (IllegalArgumentException ex){
            // If the size is negative.
            throw new IllegalArgumentException("Invalid negative size.");
        }

    }
    
    // MONTH/YEAR.
    /**
     * 
     * Parse the month / year string with the format MMYYYY.
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
            if (monthyear.length() != 2 + 4)
                throw new NumberFormatException("Invalid string month / year format length.");

            String month = monthyear.substring(0, 2);
            String year = monthyear.substring(2);

            Integer yearI = Integer.parseInt(year);
            if (yearI < 0){
                throw new NumberFormatException("Invalid string month / year format, negative year.");
            }

            Integer monthI = Integer.parseInt(month);
            if (monthI < 1 || monthI > 12){
                throw new NumberFormatException("Invalid string month / year format, invalid month.");
            }

        }catch (NumberFormatException | IndexOutOfBoundsException ex){
            // Forwards the exception's message.
            throw new IllegalArgumentException(ex.getMessage());
        }

    }
    
    // ORDER ID.
    // Integer order ID from string order ID.
    /**
     * 
     * Get the order ID as Integer from a string order id.
     * Used to cancel an order.
     * 
     * @param orderID The order ID as string.
     * 
     * @throws NullPointerException If the order ID string is null.
     * @throws IllegalArgumentException If the order ID string is invalid.
     * 
     * @return The order ID as an Integer.
     * 
     */
    public static Integer getOrderIDFromString(String orderID) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (orderID == null){
            throw new NullPointerException("String order id cannot be null.");
        }

        orderID = orderID.trim();

        // Parsing and checks.
        try {
            Integer orderIDI = Integer.parseInt(orderID);
            if (orderIDI < 0){
                throw new IllegalArgumentException("Negative string order ID.");
            }

            return orderIDI;
        }catch (NumberFormatException ex){
            throw new IllegalArgumentException("Invalid or negative string order ID.");
        }

    }

    // COMMANDS AND ARGUMENTS.
    // Linked list is not a performance issue here, the arguments are few.
    /**
     * 
     * Parse a command from a string input (given by the user in the client CLI), checking its syntax and the parameters.
     * 
     * NB: The validation of the arguments is done in the parseArgs method, not here.
     * 
     * The action enum is obtained inside the method.
     * 
     * @param command The whole string command, with the keyword and the parameters.
     * 
     * @return A linked list of strings parsed with the command parameters without the initial keyword command. So, a string list with the command parameters, those inside the parenthesis divided by commas.
     * 
     * @throws NullPointerException If the string command is null.
     * @throws IllegalArgumentException If the string command is invalid.
     * 
     */
    public static LinkedList<String> parseCommandFromString(String command) throws NullPointerException, IllegalArgumentException {
        
        // Null check.
        if (command == null){
            throw new NullPointerException("String command to parse cannot be null.");
        }
        
        // No toLowerCase here, the arguments are case sensitive and must be added to the list as they are.
        command = command.trim();

        // Preliminary checks.
        ClientActions action = null;
        try {
            action = ClientActionsUtils.actionFromString(command);

            try {
                command.charAt(command.length() - 1);
            } catch (IndexOutOfBoundsException ex){
                throw new IllegalArgumentException("Invalid string command.");
            }

            // Parsing INPUT COMMAND syntax.
            // Checking parenthesis position, NOT NUMBER.
            if (command.charAt(ClientActionsUtils.getKeywordCommand(action).length()) != '(' || command.charAt(command.length() - 1) != ')'){
                throw new IllegalArgumentException("Invalid parenthesis position.");
            }

        }catch (Exception ex){
            // Forwards the exception's message.
            throw new IllegalArgumentException(ex.getMessage());
        }

        // Checking commas number.
        // Exception of getCommand() are caught before.
        Integer exactNumberOfCommas = ClientActionsUtils.getCommand(action).split(",").length - 1;
        Integer detectedCommas = command.split(",").length - 1;
        if (exactNumberOfCommas != detectedCommas){
            throw new IllegalArgumentException("Invalid number of commas as arguments separator.");
        }

        // Removing initial command and parenthesis.
        try {
            // Checking parenthesis NUMBER.
            if (command.split("\\(").length != 2){
                throw new IllegalArgumentException("Invalid string command, wrong parenthesis.");
            }
            if (command.split("\\)").length != 2){
                throw new IllegalArgumentException("Invalid string command, wrong parenthesis.");
            }

            command = command.split("\\(")[1];
        }catch (IndexOutOfBoundsException | IllegalArgumentException ex){
            // Forwards the exception's message.
            throw new IllegalArgumentException(ex.getMessage());
        }

        // Removing final parenthesis.
        try {
            command = command.substring(0, command.length() - 1).trim();
        }catch (IndexOutOfBoundsException ex){
            throw new IllegalArgumentException("Invalid string command.");
        }

        // Returning parameters.
        LinkedList<String> parameters = new LinkedList<String>();
        for (String arg : command.split(",")){
            // No toLowerCase here, the arguments are case sensitive and must be added to the list as they are.
            parameters.add(arg.trim());
        }

        return parameters;

    }
    /**
     * 
     * Parse the command arguments from a list of strings. Each string is a command argument.
     * This list should be obtained before from the parseCommandFromString method.
     * 
     * The action enum should be obtained before from the actionFromString method.
     * 
     * Throws an exception if the arguments are invalid.
     * Otherwise, it will return nothing.
     * 
     * @param args The list of strings with the arguments. Each string is a command argument.
     * @param action The action enum associated with the arguments.
     * 
     * @throws NullPointerException If the arguments strings list or the action enum are null.
     * @throws IllegalArgumentException If the arguments strings list is invalid. At least one argument is invalid.
     * @throws RuntimeException If the registered string commands associated with the actions enum are invalid.
     * 
     * @see ClientActionsUtils#parseCommandFromString(String, ClientActions)
     * @see ClientActionsUtils#actionFromString(String)
     * 
     */
    public static void parseArgs(LinkedList<String> args, ClientActions action) throws IllegalArgumentException, NullPointerException, RuntimeException {

        // Null checks.
        if (args == null){
            throw new NullPointerException("Arguments strings list cannot be null.");
        }
        if (action == null){
            throw new NullPointerException("Action enum associated with the strings list args cannot be null.");
        }

        // Number of args check.
        Integer neededArgsNumber = null;
        neededArgsNumber = ClientActionsUtils.getCommand(action).split(",").length;
        if (args.size() != neededArgsNumber){
            throw new IllegalArgumentException("Invalid number of arguments for this action.");
        }

        // In case of invalid arguments, the exceptions will be thrown by the ClientActionsUtils other methods.
        switch (action) {
            case REGISTER:
                break;
            case LOGIN:
                break;
            case UPDATE_CREDENTIALS:
                break;
            case LOGOUT:
                break;

            case INSERT_LIMIT_ORDER:
                ClientActionsUtils.priceTypeFromString(args.get(0));
                ClientActionsUtils.getSizeFromString(args.get(1));
                ClientActionsUtils.getPriceFromString(args.get(2));
                break;
            case INSERT_MARKET_ORDER:
                ClientActionsUtils.priceTypeFromString(args.get(0));
                ClientActionsUtils.getSizeFromString(args.get(1));
                break;
            case INSERT_STOP_ORDER:
                ClientActionsUtils.priceTypeFromString(args.get(0));
                ClientActionsUtils.getSizeFromString(args.get(1));
                ClientActionsUtils.getPriceFromString(args.get(2));
                break;
            case CANCEL_ORDER:  
                ClientActionsUtils.getOrderIDFromString(args.get(0));
                break;
            case GET_PRICE_HISTORY:
                ClientActionsUtils.parseMonthFromString(args.get(0));
                break;

            case EXIT:
                break;

            default:
                // This should never happen.
                System.exit(-1);
        }

    }

    /**
     * 
     * Get the JSON request as string to send it to the server from the client action enum and the already parsed arguments strings list.
     * 
     * NB: Action and arguments should be already checked before with parseCommandFromString() and then parseArgs().
     * 
     * ATTENTION, THE FLOW USED BY THIS METHOD IS:
     * ARGS AS STRING LIST -> JAVA CROSS OBJECTS -> API JAVA CROSS OBJECTS -> JSON STRING. 
     * 
     * @param action The client action enum of the request.
     * @param args The arguments list of strings associated with the action (request).
     * 
     * @return The JSON request as a string.
     * 
     * @throws NullPointerException If the client action enum or the arguments strings list are null.
     * 
     */
    public static String getJSONRequest(ClientActions action, LinkedList<String> args) throws NullPointerException {

        // Null checks.
        if (action == null){
            throw new NullPointerException("Client action used to obtain the JSON string request cannot be null.");
        }
        if (args == null){
            throw new NullPointerException("Arguments strings list used to obtain the JSON string request cannot be null.");
        }
        
        String jsonToSend = "";
        switch (action.toString()) {

            // TODO: Convert the arguments strings to the requests objects and then to JSON string.
            case "42":
                jsonToSend = "24" + jsonToSend;
                break;
                
        }
        
        return "42";

    }

}

