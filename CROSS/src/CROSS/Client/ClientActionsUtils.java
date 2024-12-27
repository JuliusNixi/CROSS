package CROSS.Client;

import java.util.HashMap;
import java.util.LinkedList;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.GenericPrice;

/**
 * This class contains some utility functions for the client actions.
 * It is used to parse and check the commands from the client's CLI.
 * It is not instantiable. It only contains static methods.
 * @version 1.0
 * @see ClientActions
 * @see PriceType
 * @see GenericPrice
 * @see Quantity
 */
public abstract class ClientActionsUtils {
    
    // Some mapping from enums to command strings.
    private static HashMap<ClientActions, String> commands = new HashMap<ClientActions, String>(){{
        put(ClientActions.REGISTER, "register(username, password)");
        put(ClientActions.LOGIN, "login(username, password)");
        put(ClientActions.UPDATE_CREDENTIALS, "updateCredentials(username, currentPassword, newPassword)");
        put(ClientActions.LOGOUT, "logout(username)"); 
        put(ClientActions.INSERT_LIMIT_ORDER, "insertLimitOrder(type, size, priceLimit)");
        put(ClientActions.INSERT_MARKET_ORDER, "insertMarketOrder(type, size)");
        put(ClientActions.INSERT_STOP_ORDER, "insertStopOrder(type, size, stopPrice)");
        put(ClientActions.CANCEL_ORDER, "cancelOrder(orderID)");
        put(ClientActions.GET_PRICE_HISTORY, "getPriceHistory(month)");
    }};

    // ACTIONS.
    // String command from enum.
    /**
     * Get the command string from the action enum.
     * @param action The action enum.
     * @return The command string.
     * @throws IllegalArgumentException If the action is invalid.
     * @throws NullPointerException If the action is null.
     */
    public static String getCommand(ClientActions action) throws IllegalArgumentException, NullPointerException {
        if (action == null){
            throw new NullPointerException("Null string action.");
        }
        if (!commands.containsKey(action)){
            throw new IllegalArgumentException("Invalid string action.");
        }
        return commands.get(action);
    }
    // String from enum, but without the parenthesis, only the keyword, thus, the command.
    /**
     * Get the keyword command from the action enum.
     * @param action The action enum.
     * @return The keyword command as a string.
     * @throws IllegalArgumentException If the action is invalid.
     * @throws NullPointerException If the action is null.
     */
    public static String getKeywordCommand(ClientActions action) throws IllegalArgumentException, NullPointerException {
        if (action == null){
            throw new NullPointerException("Null string action.");
        }
        if (!commands.containsKey(action)){
            throw new IllegalArgumentException("Invalid string action.");
        }
        return ClientActionsUtils.getCommand(action).split("\\(")[0];
    }
    // Not used but could be useful.
    /**
     * Get all the commands.
     * @return A copy of the commands as strings with the action enums as keys.
     */
    public static HashMap<ClientActions, String> getCommands(){
        // Returns a copy.
        HashMap<ClientActions, String> commandsl = new HashMap<>();
        for (ClientActions action : ClientActions.values()) {
            commandsl.put(action, ClientActionsUtils.getCommand(action));
        }
        return commandsl;
    }
    // Action enum from string, ONLY CHECK THE COMMAND, NOT THE SYNTAX/PARAMETERS.
    // Parse.
    /**
     * Get the action enum from a string action.
     * Note that this method only checks the action as keyword, not the syntax or the parameters of the whole command.
     * @param action The string action.
     * @return The action enum.
     * @throws IllegalArgumentException If the string action is invalid.
     * @throws NullPointerException If the string action is null.
     */
    public static ClientActions actionFromString(String action) throws IllegalArgumentException, NullPointerException {

        if (action == null){
            throw new NullPointerException("Null string action.");
        }
        action = action.toLowerCase().trim();
        for (ClientActions actionEn : ClientActions.values()) {
            if (action.startsWith(ClientActionsUtils.getKeywordCommand(actionEn))){
                return actionEn;
            }
        }
        throw new IllegalArgumentException("Invalid string action.");
        
    }

    // PRICE TYPE.
    // Price type (order type).
    // String from enum.
    /**
     * Get the price type string from the price type enum.
     * @param priceType The price type enum.
     * @return The price type as string.
     * @throws NullPointerException If the price type is null.
     */
    public static String getPriceType(PriceType priceType) throws NullPointerException {
        if (priceType == null){
            throw new NullPointerException("Null priceType (order type).");
        }
        return priceType.name();
    }
    // Price type enum from string.
    // Parse.
    /**
     * Get the price type enum from a string.
     * @param priceType The string priceType.
     * @return The PriceType enum.
     * @throws IllegalArgumentException If the string priceType is invalid.
     * @throws NullPointerException If the string priceType is null.
     */
    public static PriceType priceTypeFromString(String priceType) throws IllegalArgumentException, NullPointerException {
        if (priceType == null){
            throw new NullPointerException("Null string priceType (order type).");
        }
        priceType = priceType.toLowerCase().trim();
        for (PriceType priceTypeEn : PriceType.values()) {
            if (priceType.equals(ClientActionsUtils.getPriceType(priceTypeEn).toLowerCase())){
                return priceTypeEn;
            }
        }
        throw new IllegalArgumentException("Invalid string priceType (order type).");
    }

    // PRICE.
    // Parse.
    /**
     * Get the price from a string.
     * @param price The string price.
     * @return The price as a GenericPrice object.
     * @throws IllegalArgumentException If the string price is invalid.
     * @throws NullPointerException If the string price is null.
     */
    public static GenericPrice getPriceFromString(String price) throws IllegalArgumentException, NullPointerException {
        if (price == null){
            throw new NullPointerException("Null string price.");
        }
        try {
            Integer priceI = Integer.parseInt(price);
            return new GenericPrice(priceI);
        }catch (NumberFormatException e){
                throw new IllegalArgumentException("Invalid string price.");
        }catch (IllegalArgumentException e){
            // If the price is negative.
            throw new IllegalArgumentException("Invalid string price.");
        }
    }

    // QUANTITY.
    // Parse.
    /**
     * Get the size (quantity) from a string.
     * @param size The string size.
     * @return The size as a Quantity object.
     * @throws IllegalArgumentException If the string size is invalid.
     * @throws NullPointerException If the string size is null.
     */
    public static Quantity getSizeFromString(String size) throws IllegalArgumentException, NullPointerException {
        if (size == null){
            throw new NullPointerException("Null string size.");
        }
        try {
            Integer sizeI = Integer.parseInt(size);
            return new Quantity(sizeI);
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid size.");
        }catch (IllegalArgumentException e){
            // If the size is negative.
            throw new IllegalArgumentException("Invalid size.");
        }
    }
    
    // MONTH/YEAR.
    // Parse.
    /**
     * Parse the month/year string.
     * Does not returns anything, only throws an exception if the string is invalid.
     * @param monthyear The month/year string.
     * @throws IllegalArgumentException If the month/year string is invalid.
     * @throws NullPointerException If the month/year string is null.
     */
    public static void parseMonthFromString(String monthyear) throws IllegalArgumentException, NullPointerException {
        if (monthyear == null){
            throw new NullPointerException("Null string month/year.");
        }
        try {
            // Format:
            // MMYYYY
            if (monthyear.length() != 2 + 4)
                throw new NumberFormatException("Invalid month/year format.");
            String month = monthyear.substring(0, 2);
            String year = monthyear.substring(2);
            Integer yearI = Integer.parseInt(year);
            if (yearI < 0){
                throw new NumberFormatException("Invalid month/year format.");
            }
            Integer monthI = Integer.parseInt(month);
            if (monthI < 1 || monthI > 12){
                throw new NumberFormatException("Invalid month/year format.");
            }
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid month/year format.");
        }
    }
    
    // ORDER ID.
    // Parse.
    /**
     * Parse the order ID string.
     * Does not returns anything, only throws an exception if the string is invalid.
     * @param orderID The order ID string.
     * @throws IllegalArgumentException If the order ID string is invalid.
     * @throws NullPointerException If the order ID string is null.
     */
    public static void parseOrderIDFromString(String orderID) throws IllegalArgumentException, NullPointerException {
        if (orderID == null){
            throw new NullPointerException("Null string order ID.");
        }
        try {
            Long orderIDI = Long.parseLong(orderID);
            if (orderIDI < 0){
                throw new NumberFormatException("Invalid order ID.");
            }
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid order ID.");
        }
    }

    // COMMAND AND ARGUMENTS.
    // Parse.
    // Linked list is not a performance issue, the arguments are few.
    /**
     * Parse the command from a string input, checking the syntax and the parameters.
     * The action should be the correct associated with the command string, anyway it will be checked.
     * @param command The string command, with the action and the parameters.
     * @param action The action enum associated with the command.
     * @return A list of strings parsed with the parameters without the initial command.
     * @throws IllegalArgumentException If the string command is invalid.
     * @throws NullPointerException If the string command or the action are null.
     */
    public static LinkedList<String> parseCommandFromString(String command, ClientActions action) throws IllegalArgumentException, NullPointerException {
        
        if (command == null){
            throw new NullPointerException("Null string command.");
        }
        if (action == null){
            throw new NullPointerException("Null action.");
        }
        
        command = command.toLowerCase().trim();

        // Preliminary checks.
        ClientActions internal;
        try {
            internal = ClientActionsUtils.actionFromString(command);
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Invalid string command.");
        }

        if (internal != action){
            throw new IllegalArgumentException("Invalid string/action match.");
        }

        // Parsing syntax.
        // Checking parenthesis.
        if (command.charAt(ClientActionsUtils.getKeywordCommand(action).length()) != '(' || command.charAt(command.length() - 1) != ')'){
            throw new IllegalArgumentException("Invalid parenthesis.");
        }

        // Checking commas.
        Integer exactNumberOfCommas = ClientActionsUtils.getCommand(action).split(",").length - 1;
        Integer detectedCommas = command.split(",").length - 1;
        if (exactNumberOfCommas != detectedCommas){
            throw new IllegalArgumentException("Invalid number of commas.");
        }

        // Removing initial command and parenthesis.
        command = command.split("\\(")[1];

        // Removing final parenthesis.
        command = command.substring(0, command.length() - 1);

        command = command.trim();

        // Returning parameters.
        LinkedList<String> parameters = new LinkedList<String>();
        for (String arg : command.split(",")){
            parameters.add(arg.trim());
        }

        return parameters;
    }
    /**
     * Parse the arguments from a list of strings.
     * This list should be obtained from the parseCommandFromString method.
     * Throws an exception if the arguments are invalid.
     * Otherwise, it will return nothing.
     * @param args The list of strings with the arguments.
     * @param action The action enum associated with the arguments.
     * @throws IllegalArgumentException If the arguments are invalid.
     * @throws NullPointerException If the arguments or the action are null.
     * @see ClientActionsUtils#parseCommandFromString(String, ClientActions)
     */
    public static void parseArgs(LinkedList<String> args, ClientActions action) throws IllegalArgumentException, NullPointerException {

        if (args == null){
            throw new NullPointerException("Null arguments.");
        }
        if (action == null){
            throw new NullPointerException("Null action.");
        }

        Integer neededArgsNumber = null;
        switch (action) {
            case REGISTER:
                neededArgsNumber = 2;
                break;
            case LOGIN:
                neededArgsNumber = 2;
                break;
            case UPDATE_CREDENTIALS:
                neededArgsNumber = 3;
                break;
            case LOGOUT:
                neededArgsNumber = 1;
                break;
            case INSERT_LIMIT_ORDER:
                neededArgsNumber = 3;
                break;
            case INSERT_MARKET_ORDER:
                neededArgsNumber = 2;
                break;
            case INSERT_STOP_ORDER:
                neededArgsNumber = 3;
                break;
            case CANCEL_ORDER:  
                neededArgsNumber = 1;
                break;
            case GET_PRICE_HISTORY:
                neededArgsNumber = 1;
                break;
            default:
                throw new IllegalArgumentException("Invalid action.");
        }
        if (args.size() != neededArgsNumber){
            throw new IllegalArgumentException("Invalid number of arguments.");
        }

        // In case of invalid arguments, the exceptions will be thrown by the ClientActionsUtils methods.
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
                ClientActionsUtils.parseOrderIDFromString(args.get(0));
                break;
            case GET_PRICE_HISTORY:
                ClientActionsUtils.parseOrderIDFromString(args.get(0));
                break;
            default:
                throw new IllegalArgumentException("Invalid action.");
        }

    }

}

