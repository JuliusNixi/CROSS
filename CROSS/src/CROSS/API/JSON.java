package CROSS.API;

// The Gson library is a Google's library to work with JSON.
// It's used to convert the Java objects to JSON.
import com.google.gson.Gson;

/**
 * JSON is an abstract class.
 * It is used as a superclass for all the requests and responses classes.
 * It contains the method toJSON, that converts the object to a JSON string.
 * 
 * @version 1.0
 */
public abstract class JSON {
    
    /**
     * Method that converts the object to a JSON string.
     * 
     * @return The JSON string of the object.
     */
    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
