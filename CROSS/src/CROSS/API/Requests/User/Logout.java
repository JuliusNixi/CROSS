package CROSS.API.Requests.User;

import CROSS.API.JSON;
import CROSS.API.Responses.ResponseCode.ResponseType;

/**
 * Logout is a class that is used to logout.
 * 
 * It is used to represent the request that is about the user's data.
 * 
 * It's empty.
 * 
 * It's extended from the JSON class.
 * 
 * @version 1.0
 * @see JSON
 */
public class Logout extends JSON {
    
    public Logout() {
        super(ResponseType.LOGOUT);
    }

}
