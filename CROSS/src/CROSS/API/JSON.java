package CROSS.API;

// The Gson library is a Google's library to work with JSON.
// It's used to convert the Java objects to JSON strings.
// The JSON objects are used to send / receive data through the network like an API.
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import CROSS.API.RequestResponse.RequestResponseType;

/**
 * 
 * JSON is an abstract class.
 * 
 * It is used as a superclass for all the requests and responses classes.
 * 
 * It contains the method toJSON, that converts the object to a JSON string to be sent through the network.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see RequestResponseType
 * 
 */
public abstract class JSON {
    
    // The response type of the JSON object, so the type of the request / response.
    private RequestResponseType requestResponseType = null;

    /**
     * 
     * Constructor of the JSON class.
     * 
     * @param requestResponseType The request / response type of the JSON object, so the type of the request / response.
     * 
     * @throws NullPointerException If the request / response type is null.
     * 
     */
    public JSON(RequestResponseType requestResponseType) throws NullPointerException {

        // Null check.
        if (requestResponseType == null) {
            throw new NullPointerException("The request / response type cannot be null.");
        }

        this.requestResponseType = requestResponseType;
        
    }

    /**
     * 
     * Getter of the request / response type of the JSON object.
     * 
     * @return The request / response type of the JSON object.
     * 
     */
    public RequestResponseType getRequestResponseType() {

        return RequestResponseType.valueOf(this.requestResponseType.name());

    }

    /**
     * 
     * Method that converts the object itself to a JSON string to send through the network.
     * 
     * @return The JSON string of the object.
     * 
     * @throws NullPointerException If the isServerResponse is null.
     * 
     */
    public String toJSON(Boolean isServerResponse) throws NullPointerException {

        // Null check.
        if (isServerResponse == null) {
            throw new NullPointerException("The isServerResponse cannot be null.");
        }

        Gson gson = new Gson();
        if (isServerResponse == false) {

            // Client request.

            JsonObject internalValues = JsonParser.parseString(gson.toJson(this)).getAsJsonObject();
            internalValues.remove("operation");

            JsonObject newJSON = new JsonObject();  

            newJSON.addProperty("operation", this.responseType.toString());

            // Merging the two JSON objects.
            newJSON.add("values", internalValues);

            return newJSON.toString().replace("\n", "").replace("\r", "").replace("\t", "").trim() + "\n";
        
        } else {

            return gson.toJson(this).replace("\n", "").replace("\r", "").replace("\t", "").trim() + "\n";

        }
        
    }

}
