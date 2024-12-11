package CROSS.Client;
import java.util.HashMap;
import java.util.LinkedList;

import CROSS.Enums.ClientActions;
import CROSS.Enums.Direction;
import CROSS.Types.GenericPrice;
import CROSS.Types.Quantity;

// Not instantiable, only for the static things.
// This class contains some utility functions for the client actions.
// It is used to parse and check the commands from the client's CLI.
public abstract class ClientActionsUtils {
    
    // Some mapping from enums to strings.
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
    private static HashMap<Direction, String> directions = new HashMap<Direction, String>(){{
        put(Direction.BUY, "ask");
        put(Direction.SELL, "bid");
    }};

    // ACTIONS.
    // String from enum.
    public static String getCommand(ClientActions action){
        return commands.get(action);
    }
    // String from enum, but without the parenthesis, only the keyword, thus, the command.
    public static String getKeywordCommand(ClientActions action){
        return commands.get(action).split("\\(")[0];
    }
    public static HashMap<ClientActions, String> getCommands(){
        return commands;
    }
    // Action enum from string, ONLY CHECK THE COMMAND, NOT THE SYNTAX/PARAMETERS.
    public static ClientActions actionFromString(String command) throws IllegalArgumentException {

        command = command.toLowerCase().trim();
        for (ClientActions action : ClientActions.values()) {
            if (command.startsWith(getKeywordCommand(action))){
                return action;
            }
        }
        throw new IllegalArgumentException("Invalid string command.");
        
    }

    // Direciton (order type).
    // String from enum.
    public static String getDirection(Direction direction){
        return directions.get(direction);
    }
    public static HashMap<Direction, String> getDirections(){
        return directions;
    }
    // Order type direction from string.
    public static Direction directionFromString(String command) throws IllegalArgumentException {
        command = command.toLowerCase().trim();
        for (Direction direction : Direction.values()) {
            if (command.equals(getDirection(direction))){
                return direction;
            }
        }
        throw new IllegalArgumentException("Invalid string direction (order type).");
    }

    public static GenericPrice getPriceFromString(String price, Direction direction) throws IllegalArgumentException {
        try {
            Integer priceD = Integer.parseInt(price);
            return new GenericPrice(priceD);
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid price.");
        }
    }
    public static Quantity getSizeFromString(String size) throws IllegalArgumentException {
        try {
            Double sizeD = Double.parseDouble(size);
            return new Quantity(sizeD);
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid size.");
        }
    }
    // Only parse the month/year string.
    // Does not returns anything, only throws an exception if the string is invalid.
    public static void parseMonthString(String monthyear) throws IllegalArgumentException {
        try {
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
    // Only parse the order ID string.
    // Does not returns anything, only throws an exception if the string is invalid.
    public static void parseOrderIDString(String orderID) throws IllegalArgumentException {
        try {
            Integer orderIDI = Integer.parseInt(orderID);
            if (orderIDI < 0){
                throw new NumberFormatException("Invalid order ID.");
            }
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid order ID.");
        }
    }

    // Parse the command from a string input, checking the syntax and the parameters.
    // Returns a list of strings with the parameters without the command.
    // The action should be the correct associated with the string, anyway it will be checked.
    public static LinkedList<String> parseCommandFromString(String command, ClientActions action) throws IllegalArgumentException {
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
        if (command.charAt(getKeywordCommand(action).length()) != '(' || command.charAt(command.length() - 1) != ')'){
            throw new IllegalArgumentException("Invalid parenthesis.");
        }
        // Checking commas.
        Integer exactNumberOfCommas = getCommand(action).split(",").length - 1;
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
        LinkedList<String> parameters = new LinkedList<>();
        for (String arg : command.split(",")){
            parameters.add(arg.trim());
        }
        return parameters;
    }

    // Parse the arguments from the list of strings.
    // Throws an exception if the arguments are invalid.
    // Otherwise, it will return nothing.
    public static void parseArgs(LinkedList<String> args, ClientActions action) {

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

        Direction direction = null;
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
                direction = ClientActionsUtils.directionFromString(args.get(0));
                ClientActionsUtils.getSizeFromString(args.get(1));
                ClientActionsUtils.getPriceFromString(args.get(2), direction);
                break;
            case INSERT_MARKET_ORDER:
                direction = ClientActionsUtils.directionFromString(args.get(0));
                ClientActionsUtils.getSizeFromString(args.get(1));
                break;
            case INSERT_STOP_ORDER:
                direction = ClientActionsUtils.directionFromString(args.get(0));
                ClientActionsUtils.getSizeFromString(args.get(1));
                ClientActionsUtils.getPriceFromString(args.get(2), direction);
                break;
            case CANCEL_ORDER:  
                ClientActionsUtils.parseOrderIDString(args.get(0));
                break;
            case GET_PRICE_HISTORY:
                ClientActionsUtils.parseMonthString(args.get(0));
                break;
            default:
                throw new IllegalArgumentException("Invalid action.");
        }

    }

}

