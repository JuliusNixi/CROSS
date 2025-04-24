package CROSS.API.Requests;

import CROSS.API.JSON;
import CROSS.Client.ClientActionsUtils;

/**
 * 
 * This class is responsible for creating a request Java object.
 * It's rapresents a JSON request string that will be sent to the server from the client through the socket.
 * 
 * It extends the JSON class to use the JSON string conversion method.
 * 
 * It contains the operation, that is the action (request's type) requested by the client.
 * The operation is a string to be encoded in JSON.
 * But, it's created through the constructor with the ClientActions enum.
 * 
 * It contains the values, that are the arguments for the operation. These values are request dependent.
 * So, it's a generic Object.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see JSON
 * 
 * @see ClientActionsUtils.ClientActions
 * 
 */
public class Request extends JSON {

    // Keys for the JSON object as specified in the protocol in the assignment's text.
    private final String operation;
    private final Object values;

    /**
     * 
     * Constructor of the class.
     * 
     * @param action The action requested by the client as a ClientActions enum. Will be converted to a string.
     * @param values The values for the action as generic Object.
     * 
     * @throws NullPointerException If the action or values are null.
     * 
     */
    public Request(ClientActionsUtils.ClientActions action, Object values) throws NullPointerException {

        // Null check.
        if (action == null)
            throw new NullPointerException("Action in the request cannot be null.");
        if (values == null)
            throw new NullPointerException("Values in the request cannot be null.");

        // Get the (NO TO LOWERCASE, BUT AS WRITTEN IN THE MAP AND IN THE ASSIGNMENT'S TEXT) string command from the action.
        String command = ClientActionsUtils.getKeywordCommand(action);

        this.operation = command;
        this.values = values;

    }
    
    // GETTERS
    /**
     * 
     * Getter for the operation.
     * 
     * @return The operation as string.
     * 
     */
    public String getOperation() {

        return this.operation;

    }
    /**
     * 
     * Getter for the values.
     * 
     * @return The values as generic Object.
     * 
     */
    public Object getValues() {

        return this.values;
        
    }

}
