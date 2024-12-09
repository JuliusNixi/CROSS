package CROSS.Users;

import java.util.TreeSet;

public abstract class Users {
    
    // Add and search in complexity O(log n).
    private static TreeSet<User> users = new TreeSet<User>();

    public static void addUser(User user) {
        users.add(user);
    }

    public static User getUser(User user) {
        
        User result = null;
        result = users.ceiling(user);
        if (result != null && result.equals(user)) {
            return result;
        }
        return null;
        
    }

}
