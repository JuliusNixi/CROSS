package CROSS.API;

// The Gson library is a Google's library to work with JSON.
// It's used to convert the Java objects to JSON strings.
// The JSON strings are used to send / receive data through the network like an API with sockets from / to client and server.
import com.google.gson.Gson;

/**
 * 
 * JSON is an abstract class.
 * 
 * It is used as a superclass for all the requests and responses classes.
 * 
 * It contains a method that converts the Java object to a JSON string to be sent through the network like an API with sockets from / to client and server.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 */
public abstract class JSON {
    
    /**
     * 
     * Method that converts the Java object itself to a JSON string.
     * 
     * @return The JSON string of the Java object.
     * 
     */
    public String toJSONString() {
        
        Gson gson = new Gson();

        return gson.toJson(this).replace("\n", "").replace("\r", "").replace("\t", "").trim() + "\n";
        
    }

}
