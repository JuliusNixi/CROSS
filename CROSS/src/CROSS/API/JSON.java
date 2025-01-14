package CROSS.API;

// The Gson library is a Google's library to work with JSON.
// It's used to convert the Java objects to JSON.
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import CROSS.API.Responses.ResponseCode.ResponseType; 

/**
 * JSON is an abstract class.
 * It is used as a superclass for all the requests and responses classes.
 * It contains the method toJSON, that converts the object to a JSON string to be sent through the network.
 * 
 * @version 1.0
 */
public abstract class JSON {
    
    private final ResponseType operation;

    public JSON(ResponseType operation) {

        // Null check.
        if (operation == null)
            throw new NullPointerException("Operation cannot be null.");

        this.operation = operation;
        
    }

    /**
     * Method that converts the object to a JSON string.
     * 
     * @return The JSON string of the object.
     */
    public String toJSON() {

        Gson gson = new Gson();
        JsonObject json = new JsonObject();

        // Main object preparation.
        json.addProperty("operation", this.operation.toString());
        json.addProperty("values", gson.toJson(this));
        json.remove("values");

        // Internal object preparation.
        JsonObject internal = json.getAsJsonObject("values");
        internal.remove("operation");

        // Final main object restore.
        json.add("values", internal);

        return json.toString().replace("\n", "").replace("\r", "").replace("\t", "").trim() + "\n";

    }

}
