package cross.client;

import cross.api.requests.Request;
import cross.api.requests.orders.CancelRequest;
import cross.api.requests.orders.CreateRequest;
import cross.api.requests.pricehistory.PriceHistoryRequest;
import cross.api.requests.user.LogoutRequest;
import cross.api.requests.user.RegisterLoginRequest;
import cross.api.requests.user.UpdateCredentialsRequest;
import cross.orders.LimitOrder;
import cross.orders.MarketOrder;
import cross.orders.Order;
import cross.orders.StopOrder;
import cross.types.Currency;
import cross.types.Quantity;
import cross.types.price.GenericPrice;
import cross.types.price.PriceType;
import cross.types.price.SpecificPrice;
import cross.users.User;
import cross.utils.ClientActionsUtils;
import cross.utils.ClientActionsUtils.ClientActions;
import java.util.LinkedList;
import java.util.UnknownFormatConversionException;

/**
 * 
 * This class is an abstract one.
 * It's an abstract class because the methods are all static and it hasn't a constructor.
 * So it's not possible to instantiate it.
 * 
 * It contains the methods to parse the command from the user's input and it's used in the ClientCLIThread class.
 * It also contains the methods to get the JSON request as string to send it to the server trough the TCP socket.
 * 
 * It uses the ClientActionsUtils as helper class to parse the client's commands.
 * 
 * The flow of the data is:
 * COMMAND STRING -> ARGS AS STRINGS LIST -> JAVA CROSS OBJECTS -> API JAVA CROSS OBJECTS -> JSON STRING.
 * Each step is done in a method of this class.
 * So the methods are 4 and must be called in the correct following order:
 * 1) parseCommandFromString()
 * 2) parseArgs()
 * 3) getRequest()
 * 4) getJSONRequest()
 * 
 * @see ClientActionsUtils
 * @see ClientCLIThread
 * 
 */
abstract class ClientCLICommandParser {

    // COMMANDS AND ARGUMENTS.
    // Linked list is not a performance issue here, the arguments are few.
    /**
     * 
     * Parse a command from a string input (given by the user in the client's CLI), checking its syntax.
     * 
     * NB: The validation of the arguments is done in the parseArgs() method, not here, this method only checks the syntax and must be called before.
     * 
     * The client action enum is obtained inside the method.
     * 
     * ATTENTION, THE FLOW USED IS:
     * COMMAND STRING -> ARGS AS STRINGS LIST -> JAVA CROSS OBJECTS -> API JAVA CROSS OBJECTS -> JSON STRING.
     * THIS METHOD IS USED TO GET THE ARGS AS STRINGS LIST FROM THE COMMAND STRING.
     * 
     * @param command The whole string command, with the keyword and the parameters.
     * 
     * @return A linked list of strings parsed with the command parameters without the initial keyword command and without the parenthesis. So, a string list with the command parameters, those inside the parenthesis divided by commas.
     * 
     * @throws NullPointerException If the string command is null.
     * @throws IllegalArgumentException If the string command is invalid.
     * @throws IllegalStateException If the registered string commands associated with the client actions enum are invalid.
     * 
     */
    public static LinkedList<String> parseCommandFromString(String command) throws NullPointerException, IllegalArgumentException, IllegalStateException {
        
        // Null check.
        if (command == null){
            throw new NullPointerException("String command to parse from cannot be null.");
        }
        
        // No toLowerCase here, the arguments are case sensitive and must be added to the list as they are, e.g: password.
        command = command.trim();

        // Preliminary checks.
        ClientActions action = null;
        try {
            action = ClientActionsUtils.actionFromString(command);

            // Parsing INPUT COMMAND syntax.
            // Checking parenthesis position, NOT NUMBER.
            if (command.charAt(ClientActionsUtils.getKeywordCommand(action).length()) != '(' || command.charAt(command.length() - 1) != ')'){
                throw new UnknownFormatConversionException("Invalid parenthesis position in the string command.");
            }

        }catch (IndexOutOfBoundsException ex){
            throw new IllegalArgumentException("Invalid string command, parenthesis missing.");
        }catch (IllegalStateException ex){
            // Forwards the exception's message (actionFromString() throws a IllegalStateException).
            throw new IllegalStateException(ex.getMessage());
        }catch (UnknownFormatConversionException ex){
            // Forwards the exception's message.
            throw new IllegalArgumentException(ex.getMessage().replace("Conversion = ", "").replace("'", ""));
        }catch (IllegalArgumentException ex){
            // Not forwards the exception's message to customize it better for the user.
            throw new IllegalArgumentException("Invalid string command, command not recognized.");
        }

        // Checking commas number.
        // Exception of getCommand() are caught before.
        Integer exactNumberOfCommas = ClientActionsUtils.getCommand(action).split(",").length - 1;
        Integer detectedCommas = command.split(",").length - 1;
        if (exactNumberOfCommas.compareTo(detectedCommas) != 0){
            throw new IllegalArgumentException("Invalid number of commas as arguments separator in the string command.");
        }

        // Removing initial command and parenthesis.
        try {
            // Checking parenthesis NUMBER, could be more than 2.
            if (command.split("\\(").length != 2){
                throw new IllegalArgumentException("Invalid string command, wrong parenthesis.");
            }
            // Only 1, since the split method returns 1 in this case, with a split on the last string character.
            if (command.split("\\)").length != 1){
                throw new IllegalArgumentException("Invalid string command, wrong parenthesis.");
            }

            // No exception here, the command is already checked, '(' is present, so also the [1] element.
            command = command.split("\\(")[1];
        }catch (IllegalArgumentException ex){
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
        LinkedList<String> parameters = new LinkedList<>();
        for (String arg : command.split(",")){
            // No toLowerCase here, the arguments are case sensitive (e.g: password) and must be added to the list as they are.
            parameters.add(arg.trim());
        }

        // Number of parameters check done in the parseArgs() method.
        return parameters;

    }

    /**
     * 
     * Parse the command arguments from a list of strings. Each string is a command argument.
     * This strings list should be obtained before from the parseCommandFromString() method.
     * 
     * The client action enum corresponding to the command (and so to its arguments) is also needed.
     * This client action enum should be obtained before from the actionFromString() method in the ClientActionsUtils class.
     * 
     * Throws an exception if the arguments are invalid, at least one.
     * 
     * Synchronized on the args list, to avoid concurrent modification from different threads.
     * 
     * WARNING: Others checks are done server-side to validate the arguments in the system (e.g: users auth, orders execution).
     * In case of failure, an API error is sent back to the client from the server as described in the assignment's text.
     * So, some checks are not done here, but in the server-side, e.g: login with non-existent user.
     * 
     * ATTENTION, THE FLOW USED IS:
     * COMMAND STRING -> ARGS AS STRINGS LIST -> JAVA CROSS OBJECTS -> API JAVA CROSS OBJECTS -> JSON STRING.
     * THIS METHOD IS USED TO PARSE THE ARGUMENTS FROM THE STRINGS LIST TO THE JAVA CROSS OBJECTS (RETURNED AS GENERIC OBJECTS LIST).
     * 
     * @param args The list of strings with the arguments. Each string is a command argument.
     * @param action The client action enum associated with the arguments.
     * 
     * @return A linked list of generic objects obtained from the command arguments parsing. For example, in case of a registration command, with as strings list the username and password to be used, the returned objects list will contain only an User object casted as Object. Note that number of strings in the arguments list DOES NOT ALWAYS correspond to the number of objects in the returned objects list as in this case.
     * 
     * @throws NullPointerException If the arguments strings list or the client action enum are null.
     * @throws IllegalArgumentException If the arguments strings list is invalid, at least one argument is invalid.
     * @throws IndexOutOfBoundsException If the number of elements in the arguments strings list is invalid.
     * @throws IllegalStateException If the registered string commands associated with the client actions enum are invalid.
     * 
     */
    public static LinkedList<Object> parseArgs(LinkedList<String> args, ClientActions action) throws IllegalArgumentException, NullPointerException, IllegalStateException, IndexOutOfBoundsException {

        // Null checks.
        if (args == null){
            throw new NullPointerException("Arguments strings list used to parse a string command cannot be null.");
        }
        if (action == null){
            throw new NullPointerException("Client action enum associated with the strings list args used to parse a string command cannot be null.");
        }

        LinkedList<Object> parsedArgs = new LinkedList<>();

        synchronized (args) {

            // Number of args check.
            Integer neededArgsNumber = null;
            try {
                neededArgsNumber = ClientActionsUtils.getCommand(action).split(",").length;
            }catch (IllegalStateException ex){
                // Forwards the exception's message.
                throw new IllegalStateException(ex.getMessage());
            }

            if (args.size() != neededArgsNumber){
                throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
            }

            // In case of invalid arguments, the exceptions will be thrown by the ClientActionsUtils other methods.
            // Some cases are blank since there are no checks to apply to the corresponding arguments.
            User user = null;
            Quantity size = null;
            GenericPrice price = null;
            PriceType priceType = null;
            switch (action) {
                case REGISTER:
                case LOGIN:
                    try {
                        user = new User(args.get(0), args.get(1), true);
                    }catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }
                    parsedArgs.add(user);
                    break;

                case UPDATE_CREDENTIALS:
                    // Current user.
                    try {
                        user = new User(args.get(0), args.get(1), true);
                    }catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }
                    parsedArgs.add(user);

                    // New password user.
                    User userNew = null;
                    try {
                        userNew = new User(args.get(0), args.get(2), true);
                    }catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }
                    parsedArgs.add(userNew);
                    break;
                    
                // The logout command as described in the assignment's text needs the username to be logged out.
                case LOGOUT:
                    try {
                        user = new User(args.get(0), "placeholder");
                    }catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }
                    parsedArgs.add(user);
                    break;

                case INSERT_STOP_ORDER:
                case INSERT_LIMIT_ORDER:
                    // Price type (order type direction buy / sell).
                    try {
                        priceType = ClientActionsUtils.getPriceTypeFromString(args.get(0));
                    }catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }
                    parsedArgs.add(priceType);

                    // Size (quantity).
                    try {
                        size = ClientActionsUtils.getSizeFromString(args.get(1));
                    }catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }
                    parsedArgs.add(size);

                    // Price.
                    try {
                        price = ClientActionsUtils.getPriceFromString(args.get(2));
                    }catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }
                    parsedArgs.add(price);

                    break;
                    
                case INSERT_MARKET_ORDER:
                    // Price type (order type direction buy / sell).
                    try {
                        priceType = ClientActionsUtils.getPriceTypeFromString(args.get(0));
                    }catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }
                    parsedArgs.add(priceType);

                    // Size (quantity).
                    try {
                        size = ClientActionsUtils.getSizeFromString(args.get(1));
                    }catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }
                    parsedArgs.add(size);
                    break;

                case CANCEL_ORDER:
                    Number orderID = null;
                    try {
                        orderID = ClientActionsUtils.getOrderIDFromString(args.get(0));
                    }catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }
                    parsedArgs.add(orderID);
                    break;

                case GET_PRICE_HISTORY:
                    String monthyear = args.get(0);
                    try {
                        ClientActionsUtils.parseMonthFromString(monthyear);
                    }catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }
                    parsedArgs.add(monthyear);
                    break;

                case EXIT:
                    // No arguments to parse.

            }

        }

        return parsedArgs;

    }

    /**
     * 
     * Get the APIs' Request object from the client action enum and the already parsed arguments objects list.
     * 
     * WARNING: Client action and parsed arguments should be already checked before with the parseCommandFromString() and then parseArgs() methods.
     * 
     * Synchronized on the parsed args list, to avoid concurrent modification.
     * 
     * ATTENTION, THE FLOW USED IS:
     * COMMAND STRING -> ARGS AS STRINGS LIST -> JAVA CROSS OBJECTS -> API JAVA CROSS OBJECTS -> JSON STRING.
     * THIS METHOD IS USED TO CONVERT THE ARGUMENTS FROM THE JAVA CROSS OBJECTS LIST (STORED AS GENERIC OBJECTS) TO THE API JAVA CROSS OBJECTS.
     * SO THE FIRST PART OF THE FLOW IS ALREADY DONE BY THE parseCommandFromString() AND parseArgs() METHODS.
     * 
     * @param action The client action enum of the request.
     * @param parsedArgs The arguments list of parsed objects associated with the client action (request).
     * 
     * @return The APIs' Request object.
     * 
     * @throws NullPointerException If the client action enum or the parsed arguments objects list are null.
     * @throws IllegalArgumentException If some of the arguments (at least one) are SEMANTICALLY (e.g: wrong prices) invalid.
     * @throws IndexOutOfBoundsException If the number of elements in the arguments list is invalid.
     * @throws IllegalStateException If the client action enum is invalid.
     * 
     */
    public static Request getRequest(ClientActions action, LinkedList<Object> parsedArgs) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException, IllegalStateException {

        // Null checks.
        if (action == null){
            throw new NullPointerException("Client action used to obtain the client's Request cannot be null.");
        }
        if (parsedArgs == null){
            throw new NullPointerException("Arguments parsed generic objects list used to obtain the client's Request cannot be null.");
        }

        synchronized (parsedArgs) {

            User user = null;

            Request request = null;
            CreateRequest createRequest;

            Quantity size = null;
            GenericPrice price = null;
            PriceType priceType = null;
            Currency defaultPrimaryCurrency = Currency.getDefaultPrimaryCurrency();
            Currency defaultSecondaryCurrency = Currency.getDefaultSecondaryCurrency();
            SpecificPrice specificPrice;

            switch (action) {
                case REGISTER:
                case LOGIN:
                    try {
                        user = (User) parsedArgs.get(0);
                    } catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }

                    RegisterLoginRequest registerLoginRequest = new RegisterLoginRequest(user);
                    request = new Request(action, registerLoginRequest);
                    break;

                case UPDATE_CREDENTIALS:
                    User newUser = null;
                    try {
                        user = (User) parsedArgs.get(0);
                        newUser = (User) parsedArgs.get(1);
                    } catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }

                    UpdateCredentialsRequest updateCredentialsRequest;
                    try {
                        updateCredentialsRequest = new UpdateCredentialsRequest(user, newUser);
                    } catch (IllegalArgumentException ex){
                        throw new IllegalArgumentException(ex.getMessage());
                    }
                    request = new Request(action, updateCredentialsRequest);
                    break;

                case LOGOUT:
                    try {
                        user = (User) parsedArgs.get(0);
                        if (user == null);
                    } catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }

                    LogoutRequest logoutRequest = new LogoutRequest();
                    request = new Request(action, logoutRequest);
                    break;

                case INSERT_LIMIT_ORDER:
                    try {
                        priceType = (PriceType) parsedArgs.get(0);
                        size = (Quantity) parsedArgs.get(1);
                        price = (GenericPrice) parsedArgs.get(2);
                    } catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }

                    try {
                        specificPrice = new SpecificPrice(price.getValue(), priceType, defaultPrimaryCurrency, defaultSecondaryCurrency);
                    } catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }

                    LimitOrder limitOrder = null;
                    try {
                        limitOrder = new LimitOrder(specificPrice, size, true);
                    } catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }

                    createRequest = new CreateRequest((Order) limitOrder);
                    request = new Request(ClientActions.INSERT_LIMIT_ORDER, createRequest);
                    break;

                case INSERT_MARKET_ORDER:
                    try {
                        priceType = (PriceType) parsedArgs.get(0);
                        size = (Quantity) parsedArgs.get(1);
                    } catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }

                    MarketOrder marketOrder = new MarketOrder(priceType, defaultPrimaryCurrency, defaultSecondaryCurrency, size);;

                    createRequest = new CreateRequest((Order) marketOrder);
                    request = new Request(ClientActions.INSERT_MARKET_ORDER, createRequest);
                    break;

                case INSERT_STOP_ORDER:
                    try {
                        priceType = (PriceType) parsedArgs.get(0);
                        size = (Quantity) parsedArgs.get(1);
                        price = (GenericPrice) parsedArgs.get(2);
                    } catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }

                    try {
                        specificPrice = new SpecificPrice(price.getValue(), priceType, defaultPrimaryCurrency, defaultSecondaryCurrency);
                    } catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }

                    StopOrder stopOrder = null;
                    try {
                        stopOrder = new StopOrder(specificPrice, size, true);
                    } catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }

                    createRequest = new CreateRequest((Order) stopOrder);
                    request = new Request(ClientActions.INSERT_STOP_ORDER, createRequest);
                    break;

                case CANCEL_ORDER:
                    Number orderID = null;
                    try {
                        orderID = (Number) parsedArgs.get(0);
                    } catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }

                    CancelRequest cancelRequest = new CancelRequest(orderID);
                    request = new Request(ClientActions.CANCEL_ORDER, cancelRequest);
                    break;

                case GET_PRICE_HISTORY:
                    String monthyear = null;
                    try {
                        monthyear = (String) parsedArgs.get(0);
                    } catch (IndexOutOfBoundsException ex){
                        throw new IndexOutOfBoundsException("Invalid number of arguments for this action / command.");
                    }
                    
                    PriceHistoryRequest priceHistoryRequest = null;
                    try {
                        priceHistoryRequest = new PriceHistoryRequest(monthyear);
                    } catch (IllegalArgumentException ex){
                        // Forwards the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());
                    }
                    request = new Request(ClientActions.GET_PRICE_HISTORY, priceHistoryRequest);
                    break;

                case EXIT:
                    // No arguments to parse.
                    request = new Request(ClientActions.EXIT, null);
                    break;

                default: // This should never happen.
                    throw new IllegalStateException("Illegal client action enum.");
            }

            return request;

        }

    }

    /**
     * 
     * Get the JSON request as string to send it to the server trough the TCP socket from the APIs' Request object.
     * 
     * This method is very simple, is just a return of the toJSONString() method of the Request object.
     * So it could be done in the previous getRequest() method, but it's done here to separate the concerns and for clarity.
     * 
     * ATTENTION, THE FLOW USED IS:
     * COMMAND STRING -> ARGS AS STRINGS LIST -> JAVA CROSS OBJECTS -> API JAVA CROSS OBJECTS -> JSON STRING.
     * THIS METHOD IS USED TO CONVERT THE API JAVA CROSS OBJECTS TO THE JSON STRING.
     * SO THE FIRST PART OF THE FLOW IS ALREADY DONE BY THE parseCommandFromString() AND parseArgs() AND getRequest() METHODS.
     * 
     * @param request The APIs' Request object.
     * 
     * @return The JSON request as a string.
     * 
     * @throws NullPointerException If the request object is null.
     * 
     */
    public static String getJSONRequest(Request request) throws NullPointerException {

        // Null check.
        if (request == null){
            throw new NullPointerException("Request object used to obtain the JSON request cannot be null.");
        }

        // Returning the JSON request as string.
        return request.toJSONString();

    }

}
