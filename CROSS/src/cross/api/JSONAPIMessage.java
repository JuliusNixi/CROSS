package cross.api;

// The Gson library is a Google's library to work with JSON.
// It's used to convert the Java objects to JSON strings.
// The JSON strings are used in the project to send / receive data through the network like an API with TCP sockets from / to client and server.
import com.google.gson.Gson;

/**
 *
 * JSONAPIMessage is an abstract class, not intended to be instantiated.
 *
 * It is used as a superclass for all the requests and responses classes.
 *
 * It contains a method that converts the Java object to a JSON string to be sent through the network like an API with TCP sockets from / to client and server.
 *
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Request
 * @see Response
 * @see Notification
 *
 */
public abstract class JSONAPIMessage {

    /**
     *
     * Method that converts the Java object (request or response) itself to a JSON string to be sent through the network like an API with TCP sockets from / to client and server.
     *
     * @return The JSON string of the Java object.
     *
     */
    public String toJSONString() {

        Gson gson = new Gson();

        // Get as one line string, '\n' terminated.
        String json = gson.toJson(this).replace("\n", "").replace("\r", "").replace("\t", "").trim() + "\n";

        // Replace "orderIdL" with "orderId" in the JSON string.
        json = json.replace("\"orderIdL\":", "\"orderId\":");

        return json;

    }
    
}
