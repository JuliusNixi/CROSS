package CROSS.API;

// The Gson library is a Google's library to work with JSON.
// It's used to convert the Java objects to JSON.
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import CROSS.API.Responses.ResponseCode.ResponseType; 

/**
 * JSON is an abstract class.
 * It is used as a superclass for all the requests and responses classes.
 * It contains the method toJSON, that converts the object to a JSON string to be sent through the network.
 * 
 * @version 1.0
 */
public abstract class JSON {
    
    private ResponseType operation;

    /**
     * Constructor of the class.
     * 
     * @param operation The operation of the JSON object. Could be null to handle the generic classes.
     */
    public JSON(ResponseType operation) {

        this.operation = operation;
        
    }

    public void setOperation(ResponseType operation) {

        if (operation == null) {
            throw new NullPointerException("The operation cannot be null.");
        }

        this.operation = operation;

    }

    public ResponseType getOperation() {

        return ResponseType.valueOf(this.operation.name());

    }

    /**
     * Method that converts the object itself to a JSON string.
     * 
     * @return The JSON string of the object.
     */
    public String toJSON(Boolean serverSender) {

        if (this.operation == null) {
            throw new NullPointerException("The operation cannot be null.");
        }

        if (serverSender == null) {
            throw new NullPointerException("The serverSender cannot be null.");
        }

        Gson gson = new Gson();
        if (serverSender == false) {

            JsonObject internalValues = JsonParser.parseString(gson.toJson(this)).getAsJsonObject();
            internalValues.remove("operation");

            JsonObject newJSON = new JsonObject();  

            newJSON.addProperty("operation", this.operation.toString());

            // Merging the two JSON objects.
            newJSON.add("values", internalValues);

            return newJSON.toString().replace("\n", "").replace("\r", "").replace("\t", "").trim() + "\n";
        } else {

            return gson.toJson(this).replace("\n", "").replace("\r", "").replace("\t", "").trim() + "\n";

        }
    }

}
