package CROSS.Client;

import java.util.HashMap;
import java.util.LinkedList;
import CROSS.API.Requests.Orders.Limit;
import CROSS.API.Requests.Orders.Market;
import CROSS.API.Requests.Orders.Stop;
import CROSS.API.Requests.User.Login;
import CROSS.API.Requests.User.Logout;
import CROSS.API.Requests.User.Register;
import CROSS.API.Requests.User.Update;
import CROSS.API.Responses.ResponseAndMessage;
import CROSS.API.Responses.ResponseCode;
import CROSS.API.Responses.ResponseCode.AllResponses;
import CROSS.API.Responses.ResponseCode.ResponseType;
import CROSS.Orders.LimitOrder;
import CROSS.Orders.MarketOrder;
import CROSS.Orders.StopMarketOrder;
import CROSS.Types.Quantity;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.User;
import CROSS.Types.Price.GenericPrice;

/**
 * This class contains some utility functions for the client actions.
 * With "actions" we mean the commands that the client can send to the server.
 * 
 * It is used to parse and check the commands from the client's CLI.
 * 
 * It is not instantiable. It only contains static methods, for this reason it is abstract.
 * 
 * @version 1.0
 * @see CROSS.Client.ClientActionsUtils.ClientActions
 * 
 * @see CROSS.Orders.LimitOrder
 * @see CROSS.Orders.MarketOrder
 * @see CROSS.Orders.StopMarketOrder
 * 
 * @see CROSS.API.Requests.Orders.Limit
 * @see CROSS.API.Requests.Orders.Market
 * @see CROSS.API.Requests.Orders.Stop
 * @see CROSS.API.Requests.User.Login
 * @see CROSS.API.Requests.User.Logout
 * @see CROSS.API.Requests.User.Register
 * @see CROSS.API.Requests.User.Update
 * 
 * @see CROSS.Users.User
 * 
 * @see CROSS.Types.Price.SpecificPrice
 * @see CROSS.Types.Price.PriceType
 * @see CROSS.Types.Price.GenericPrice
 * @see CROSS.Types.Price.Quantity
 */
public abstract class ClientActionsUtils {
    
    // This enum represents the available actions for the client.
    public enum ClientActions {
        REGISTER,
        LOGIN,
        UPDATE_CREDENTIALS,
        LOGOUT,
        INSERT_MARKET_ORDER,
        INSERT_LIMIT_ORDER,
        INSERT_STOP_ORDER,
        CANCEL_ORDER,
        GET_PRICE_HISTORY,
        EXIT
    }

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
        put(ClientActions.EXIT, "exit()");
    }};

    // ACTIONS.
    // String command from enum.
    /**
     * 
     * Get the command string from the action enum.
     * 
     * @param action The action enum.
     * 
     * @return The command string.
     * 
     * @throws IllegalArgumentException If the action is not associated with a command.
     * @throws NullPointerException If the action is null.
     * 
     */
    public static String getCommand(ClientActions action) throws IllegalArgumentException, NullPointerException {
        
        // Null check.
        if (action == null){
            throw new NullPointerException("Action cannot be null.");
        }

        // Invalid action.
        if (!commands.containsKey(action)){
            throw new IllegalArgumentException("Invalid action.");
        }

        return commands.get(action).toLowerCase().trim();

    }
    // String command from enum, but without the parenthesis, only the keyword.
    /**
     * 
     * Get the keyword command from the action enum.
     * With keyword command we mean the command without the parenthesis and the parameters.
     * 
     * @param action The action enum.
     * 
     * @return The keyword command as a string.
     * 
     * @throws IllegalArgumentException If the action is not associated with a command.
     * @throws NullPointerException If the action is null.
     * 
     */
    public static String getKeywordCommand(ClientActions action) throws IllegalArgumentException, NullPointerException {
        
        // Null check.
        if (action == null){
            throw new NullPointerException("Action cannot be null.");
        }

        if (!commands.containsKey(action)){
            throw new IllegalArgumentException("Invalid string action.");
        }

        return ClientActionsUtils.getCommand(action).split("\\(")[0].toLowerCase();

    }
    // Action enum from string, ONLY CHECK THE COMMAND, NOT THE SYNTAX/PARAMETERS.
    /**
     * 
     * Get the action enum from a string action.
     * 
     * Note that this method only checks the action as keyword, not the syntax or the parameters of the whole command.
     * 
     * @param action The string action.
     * 
     * @return The action enum.
     * 
     * @throws IllegalArgumentException If the string action is invalid.
     * @throws NullPointerException If the string action is null.
     * 
     */
    public static ClientActions actionFromString(String action) throws IllegalArgumentException, NullPointerException {

        // Null check.
        if (action == null){
            throw new NullPointerException("String action cannot be null.");
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
    // Price type (order type direction buy/sell).
    // Price type enum from string.
    /**
     * 
     * Get the price type enum from a string.
     * 
     * @param priceType The string priceType.
     * 
     * @return The PriceType enum.
     * 
     * @throws IllegalArgumentException If the string priceType is invalid.
     * @throws NullPointerException If the string priceType is null.
     */
    public static PriceType priceTypeFromString(String priceType) throws IllegalArgumentException, NullPointerException {
        
        // Null check.
        if (priceType == null){
            throw new NullPointerException("String price type (order type buy/sell) cannot be null.");
        }

        priceType = priceType.toLowerCase().trim();

        // Check the string.
        for (PriceType priceTypeEn : PriceType.values()) {
            if (priceType.equals(priceTypeEn.name().toLowerCase())){
                return priceTypeEn;
            }
        }

        throw new IllegalArgumentException("Invalid string priceType (order type buy/sell).");
    }

    // PRICE.
    // GenericPrice from string.
    /**
     * 
     * Get the price from a string.
     * 
     * @param price The string price.
     * 
     * @return The price as a GenericPrice object.
     * 
     * @throws IllegalArgumentException If the string price is invalid.
     * @throws NullPointerException If the string price is null.
     * 
     */
    public static GenericPrice getPriceFromString(String price) throws IllegalArgumentException, NullPointerException {
        
        // Null check.
        if (price == null){
            throw new NullPointerException("String price cannot be null.");
        }

        try {
            Integer priceI = Integer.parseInt(price);
            return new GenericPrice(priceI);
        }catch (NumberFormatException ex){
                throw new IllegalArgumentException("Invalid string price.");
        }catch (IllegalArgumentException ex){
            // If the price is negative.
            throw new IllegalArgumentException("Invalid negative string price.");
        }
    }

    // QUANTITY.
    // Quantity from string.
    /**
     * 
     * Get the size (quantity) from a string.
     * 
     * @param size The string size.
     * 
     * @return The size as a Quantity object.
     * 
     * @throws IllegalArgumentException If the string size is invalid.
     * @throws NullPointerException If the string size is null.
     * 
     */
    public static Quantity getSizeFromString(String size) throws IllegalArgumentException, NullPointerException {
        
        // Null check.
        if (size == null){
            throw new NullPointerException("String size cannot be null.");
        }

        try {
            Integer sizeI = Integer.parseInt(size);
            return new Quantity(sizeI);
        }catch (NumberFormatException ex){
            throw new IllegalArgumentException("Invalid size.");
        }catch (IllegalArgumentException ex){
            // If the size is negative.
            throw new IllegalArgumentException("Invalid negative size.");
        }

    }
    
    // MONTH/YEAR.
    /**
     * 
     * Parse the month/year string with the format MMYYYY.
     * 
     * Does not returns anything, only throws an exception if the string is invalid.
     * 
     * @param monthyear The month/year string.
     * 
     * @throws IllegalArgumentException If the month/year string is invalid.
     * @throws NullPointerException If the month/year string is null.
     * 
     */
    public static void parseMonthFromString(String monthyear) throws IllegalArgumentException, NullPointerException {
        
        // Null check.
        if (monthyear == null){
            throw new NullPointerException("String month/year cannot be null.");
        }

        try {
            // Format:
            // MMYYYY
            if (monthyear.length() != 2 + 4)
                throw new NumberFormatException("Invalid string month/year format.");

            String month = monthyear.substring(0, 2);
            String year = monthyear.substring(2);

            Integer yearI = Integer.parseInt(year);
            if (yearI < 0){
                throw new NumberFormatException("Invalid string month/year format.");
            }

            Integer monthI = Integer.parseInt(month);
            if (monthI < 1 || monthI > 12){
                throw new NumberFormatException("Invalid string month/year format.");
            }

        }catch (NumberFormatException | IndexOutOfBoundsException ex){
            throw new IllegalArgumentException("Invalid string month/year format.");
        }

    }
    
    // ORDER ID.
    // Integer order ID from string.
    /**
     * 
     * Get the order ID from a string.
     * 
     * @param orderID The order ID string.
     * 
     * @throws IllegalArgumentException If the order ID string is invalid.
     * @throws NullPointerException If the order ID string is null.
     * 
     * @return The order ID as a Integer.
     * 
     */
    public static Integer getOrderIDFromString(String orderID) throws IllegalArgumentException, NullPointerException {
        
        // Null check.
        if (orderID == null){
            throw new NullPointerException("String order id cannot be null.");
        }

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

    // COMMAND AND ARGUMENTS.
    // Linked list is not a performance issue, the arguments are few.
    /**
     * 
     * Parse the command from a string input, checking the syntax and the parameters.
     * 
     * The action should be the correct associated with the command string, anyway it will be checked.
     * 
     * NB: The validation of the arguments is done in the parseArgs method.
     * 
     * @param command The string command, with the action and the parameters.
     * @param action The action enum associated with the command.
     * 
     * @return A linked list of strings parsed with the parameters without the initial command. I.E. The parameters inside the parenthesis divided by commas.
     * 
     * @throws IllegalArgumentException If the string command is invalid.
     * @throws NullPointerException If the string command or the action are null.
     * 
     */
    public static LinkedList<String> parseCommandFromString(String command, ClientActions action) throws IllegalArgumentException, NullPointerException {
        
        // Null checks.
        if (command == null){
            throw new NullPointerException("String command cannot be null.");
        }
        if (action == null){
            throw new NullPointerException("Action cannot be null.");
        }
        
        command = command.toLowerCase().trim();

        // Preliminary checks.
        ClientActions internal;
        try {
            internal = ClientActionsUtils.actionFromString(command);
            // Parsing syntax.
            // Checking parenthesis.
            if (command.charAt(ClientActionsUtils.getKeywordCommand(action).length()) != '(' || command.charAt(command.length() - 1) != ')'){
                throw new IllegalArgumentException("Invalid parenthesis.");
            }
        }catch (IllegalArgumentException | IndexOutOfBoundsException ex){
            throw new IllegalArgumentException("Invalid string command.");
        }

        if (internal != action){
            throw new IllegalArgumentException("Invalid string/action match.");
        }

        // Checking commas.
        Integer exactNumberOfCommas = ClientActionsUtils.getCommand(action).split(",").length - 1;
        Integer detectedCommas = command.split(",").length - 1;
        if (exactNumberOfCommas != detectedCommas){
            throw new IllegalArgumentException("Invalid number of commas.");
        }

        // Removing initial command and parenthesis.
        try {
            command = command.split("\\(")[1];
        }catch (IndexOutOfBoundsException ex){
            throw new IllegalArgumentException("Invalid string command.");
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
            parameters.add(arg.trim());
        }

        return parameters;

    }
    /**
     * 
     * Parse the arguments from a list of strings.
     * This list should be obtained before from the parseCommandFromString method.
     * 
     * Throws an exception if the arguments are invalid.
     * Otherwise, it will return nothing.
     * 
     * @param args The list of strings with the arguments.
     * @param action The action enum associated with the arguments.
     * 
     * @throws IllegalArgumentException If the arguments are invalid.
     * @throws NullPointerException If the arguments or the action are null.
     * 
     * @see ClientActionsUtils#parseCommandFromString(String, ClientActions)
     * 
     */
    public static void parseArgs(LinkedList<String> args, ClientActions action) throws IllegalArgumentException, NullPointerException {

        // Null checks.
        if (args == null){
            throw new NullPointerException("Arguments list cannot be null.");
        }
        if (action == null){
            throw new NullPointerException("Action cannot be null.");
        }

        Integer neededArgsNumber = null;
        neededArgsNumber = getCommand(action).split(",").length;
        if (args.size() != neededArgsNumber){
            throw new IllegalArgumentException("Invalid number of arguments.");
        }

        // In case of invalid arguments, the exceptions will be thrown by the ClientActionsUtils other methods.
        // Number of args is already checked in the parseCommandFromString step.
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
                throw new IllegalArgumentException("Invalid action.");
        }

    }

    /**
     * 
     * Get the JSON request as string to send to the server from the action and the arguments strings list.
     * 
     * Action and arguments should be already checked before with parseCommandFromString and then parseArgs.
     * 
     * ATTENTION, THE FLOW IS: ARGS AS STRING -> JAVA CROSS OBJECTS -> API JAVA CROSS OBJECTS -> JSON STRING. 
     * 
     * It doesn't throw exceptions, it only prints the errors and returns null.
     * 
     * @param client The client object that will send the request.
     * @param action The action enum of the request.
     * @param args The arguments list of strings.
     * 
     * @return The JSON request as a string if everything is correct, otherwise null.
     * 
     */
    public static String getJSONRequest(Client client, ClientActions action, LinkedList<String> args) {

        // Null checks.
        if (client == null){
            System.out.println("Client object cannot be null.");
            return null;
        }
        if (action == null){
            System.out.println("Action cannot be null.");
            return null;
        }
        if (args == null){
            System.out.println("Arguments string list cannot be null.");
            return null;
        }

        String jsonToSend = "";

          switch (action) {

                case REGISTER:

                    if (client.getLoggedUser() != null) {
                        System.out.println("You are already logged in.");
                        return null;
                    }

                    User registerUser = null;
                    try {
                        registerUser = new User(args.get(0), args.get(1));
                    } catch (IllegalArgumentException | NullPointerException ex) {
                        System.out.println("Error in registration: " + ex.getMessage());
                        return null;
                    }

                    Register registerRequest = new Register(registerUser);

                    jsonToSend = registerRequest.toJSON(false);
                    return jsonToSend;
                
                case LOGIN:

                    if (client.getLoggedUser() != null) {
                        System.out.println("You are already logged in.");
                        return null;
                    }

                    User loginUser = null;
                    try {
                        loginUser = new User(args.get(0), args.get(1));
                    } catch (IllegalArgumentException | NullPointerException ex) {
                        System.out.println("Error in login: " + ex.getMessage());
                        return null;
                    }

                    Login loginRequest = new Login(loginUser);

                    jsonToSend = loginRequest.toJSON(false);
                    return jsonToSend;

                case UPDATE_CREDENTIALS:

                    if (client.getLoggedUser() == null) {
                        System.out.println("You are not logged in.");
                        return null;
                    }

                    User currentUser = null;
                    try {
                        currentUser = new User(args.get(0), args.get(1));
                    } catch (IllegalArgumentException | NullPointerException ex) {
                        System.out.println("Error in update (username or old password): " + ex.getMessage());
                        return null;
                    }

                    User updatedUser = null;
                    try {
                        updatedUser = new User(args.get(0), args.get(2));
                    } catch (IllegalArgumentException | NullPointerException ex) {
                        System.out.println("Error in update (new password): " + ex.getMessage());
                        return null;
                    }

                    Update updateRequest = null;
                    try {
                        updateRequest = new Update(currentUser, updatedUser.getPassword());
                    }catch (IllegalArgumentException ex){
                        System.out.println("Error in update: " + ex.getMessage());
                        return null;
                    }

                    jsonToSend = updateRequest.toJSON(false);
                    return jsonToSend;

                case LOGOUT:

                    if (client.getLoggedUser() == null) {
                        System.out.println("You are not logged in.");
                        return null;
                    }

                    Logout logoutRequest = new Logout();

                    jsonToSend = logoutRequest.toJSON(false);
                    return jsonToSend;

                case INSERT_LIMIT_ORDER:

                    if (client.getLoggedUser() == null) {
                        System.out.println("You are not logged in.");
                        return null;
                    }

                    // CROSS Objects.
                    LimitOrder limitOrder = null;
                    // No checks needed, already done in parseArgs.
                    CROSS.OrderBook.Market marketLimit = CROSS.OrderBook.Market.getMainMarket();
                    PriceType priceTypeLimit = ClientActionsUtils.priceTypeFromString(args.get(0));
                    Quantity quantityLimit = ClientActionsUtils.getSizeFromString(args.get(1));
                    GenericPrice genericPriceLimit = ClientActionsUtils.getPriceFromString(args.get(2));
                    SpecificPrice priceLimit = new SpecificPrice(genericPriceLimit.getValue(), priceTypeLimit, marketLimit);
                    try {
                        limitOrder = new LimitOrder(marketLimit, priceLimit, quantityLimit, client.getLoggedUser());
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Error in limit order: " + ex.getMessage());
                        return null;
                    }

                    // API Objects.
                    Limit limitRequest = new Limit(limitOrder);
                    
                    jsonToSend = limitRequest.toJSON(false);
                    return jsonToSend;

                case INSERT_MARKET_ORDER:

                    if (client.getLoggedUser() == null) {
                        System.out.println("You are not logged in.");
                        return null;
                    }

                    // CROSS Objects.
                    MarketOrder marketOrder = null;
                    // No checks needed, already done in parseArgs.
                    CROSS.OrderBook.Market marketMarket = CROSS.OrderBook.Market.getMainMarket();
                    PriceType priceTypeMarket = ClientActionsUtils.priceTypeFromString(args.get(0));
                    Quantity quantityMarket = ClientActionsUtils.getSizeFromString(args.get(1));
                    try {
                        marketOrder = new MarketOrder(marketMarket, priceTypeMarket, quantityMarket, client.getLoggedUser());
                    } catch (IllegalArgumentException | NullPointerException ex) {
                        System.out.println("Error in market order: " + ex.getMessage());
                        return null;
                    }

                    // API Objects.
                    Market marketRequest = new Market(marketOrder);

                    jsonToSend = marketRequest.toJSON(false);
                    return jsonToSend;

                case INSERT_STOP_ORDER:

                    if (client.getLoggedUser() == null) {
                        System.out.println("You are not logged in.");
                        return null;
                    }

                    // CROSS Objects.
                    StopMarketOrder stopOrder = null;
                    // No checks needed, already done in parseArgs.
                    CROSS.OrderBook.Market marketStop = CROSS.OrderBook.Market.getMainMarket();
                    PriceType priceTypeStop = ClientActionsUtils.priceTypeFromString(args.get(0));
                    Quantity quantityStop = ClientActionsUtils.getSizeFromString(args.get(1));
                    GenericPrice genericPriceStop = ClientActionsUtils.getPriceFromString(args.get(2));
                    SpecificPrice priceStop = new SpecificPrice(genericPriceStop.getValue(), priceTypeStop, marketStop);
                    try {
                        stopOrder = new StopMarketOrder(marketStop, priceStop, quantityStop, client.getLoggedUser());
                    } catch (IllegalArgumentException | NullPointerException ex) {
                        System.out.println("Error in stop order: " + ex.getMessage());
                        return null;
                    }

                    // API Objects.
                    Stop stopRequest = new Stop(stopOrder);

                    jsonToSend = stopRequest.toJSON(false);
                    return jsonToSend;

                case CANCEL_ORDER:  

                    if (client.getLoggedUser() == null) {
                        System.out.println("You are not logged in.");
                        return null;
                    }

                    // CROSS Objects.
                    // No checks needed, already done in parseArgs.
                    Integer orderID = ClientActionsUtils.getOrderIDFromString(args.get(0));

                    // API Objects.
                    CROSS.API.Requests.Orders.CancelOrderID cancelRequest = new CROSS.API.Requests.Orders.CancelOrderID(orderID);

                    jsonToSend = cancelRequest.toJSON(false);
                    return jsonToSend;

                case GET_PRICE_HISTORY: 

                    if (client.getLoggedUser() == null) {
                        System.out.println("You are not logged in.");
                        return null;
                    }

                    // CROSS Objects.
                    // No checks needed, already done in parseArgs.

                    // API Objects.
                    CROSS.API.Requests.PriceHistory getPriceHistoryRequest;
                    // Below some additional checks are done, not really needed, but it's a good practice.
                    try {
                        getPriceHistoryRequest = new CROSS.API.Requests.PriceHistory(args.get(0));
                    }catch (IllegalArgumentException | NullPointerException ex){
                        System.out.println("Error in get price history: " + ex.getMessage());
                        return null;
                    }

                    jsonToSend = getPriceHistoryRequest.toJSON(false);
                    return jsonToSend;

                case EXIT:

                    AllResponses responseContent = AllResponses.EXIT;
                    ResponseType responseType = ResponseType.EXIT;
                    ResponseCode responseCode = new ResponseCode(responseType, responseContent);
                    ResponseAndMessage response = new ResponseAndMessage(responseCode, "Client exiting.");
                    
                    jsonToSend = response.toJSON(false);
                    return jsonToSend;

                default:

                    // This should never happens.
                    System.err.println("Error in getJSONRequest.");
                    System.exit(-1);
                    return null;
                    
            }

    }

}

