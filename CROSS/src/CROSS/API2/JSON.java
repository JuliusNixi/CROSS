package CROSS.API2;

// The Gson library is a Google's library to work with JSON.
// It's used to convert the Java objects to JSON strings.
// The JSON objects are used to send / receive data through the network like an API.
import com.google.gson.Gson;

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
 */
public abstract class JSON {
    
    /**
     * 
     * Method that converts the object itself to a JSON string to send through the network.
     * 
     * @return The JSON string of the object.
     * 
     */
    public String toJSON() {

        Gson gson = new Gson();

        return gson.toJson(this).replace("\n", "").replace("\r", "").replace("\t", "").trim() + "\n";
        
    }


}
