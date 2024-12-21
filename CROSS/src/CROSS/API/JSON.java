package CROSS.API;

import com.google.gson.Gson;

public abstract class JSON {
    
    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
