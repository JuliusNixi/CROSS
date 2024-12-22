package CROSS.TMP;

import com.google.gson.Gson;

import CROSS.Enums.ClientActions;
import CROSS.Users.User;

// This class is used as an interface to/from JSON to/from Java objects.
// It's use the GSON library.
public abstract class JSONInterface {

    // All the data that may be present in a JSON request is declared here.
    // But not all the data is used in all the requests.
    // I prefer to declare all the data here to avoid the creation of multiple little classes.
    private static User user = null;
    public static User getUser() throws NullPointerException {
        if (user == null) {
            throw new NullPointerException("User is null.");
        }

        // To prevent forgetting to parse a new request and use the old one value saved,
        // I clear the saved value.
        User userCopy = JSONInterface.user;
        user = null;

        return userCopy;
    }

    // All the methods below are used to create a JSON string from the object's data.
    // The check for null is done in the method User that is called inside these methods.
    public static String userRegister(String username, String password) {
        User user = new User(username, password);
        Gson gson = new Gson();
        String json = gson.toJson(user, User.class);
        return json.trim().replace("\n", "") + "\n";
    }
    public static String userLogin(String username, String password) {
        User user = new User(username, password);
        Gson gson = new Gson();
        String json = gson.toJson(user, User.class);
        return json.trim().replace("\n", "") + "\n";
    }

    public static ClientActions parseRequest(String json) {
        Gson gson = new Gson();
        ClientActions action = null;
        User u = null;
        try {
            u = gson.fromJson(json, User.class);
            action = ClientActions.REGISTER;
            user = u;
        } catch (Exception e) {
            // TODO: Handle this exception.
        }
        return action;
    }

}