package CROSS.API.Responses;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import CROSS.API.JSON;

/**
 * 
 * This class is responsible for creating a response Java object.
 * It's rapresents a JSON response string that will be sent to the client from the server through the socket.
 * 
 * It extends the JSON class to use the JSON string conversion method.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see JSON
 * 
 */
public class Response extends JSON {

    private final Object response;

    /**
     * 
     * Constructor of the class.
     * 
     * @param response The response as Object.
     * 
     * @throws NullPointerException If the response is null.
     * 
     */
    public Response(Object response) throws NullPointerException {

        // Null check.
        if (response == null)
            throw new NullPointerException("Response object in the response cannot be null.");

        this.response = response;

    }
    
    @Override
    public String toJSONString() {
        
        /*
         *
         * {
         *      {
         *      }
         * }
         * 
         * Need to remove the first parenthesis couple.
         *
         */
        String nested = super.toJSONString();
        JsonObject jsonObject = JsonParser.parseString(nested).getAsJsonObject();
        return jsonObject.get("response").getAsJsonObject().toString().replace("\n", "").replace("\r", "").replace("\t", "").trim() + "\n";

    }

    // GETTERS
    /**
     * 
     * Getter for the response object.
     * 
     * @return The response object as Object.
     * 
     */
    public Object getResponse() {

        return this.response;

    }

}
