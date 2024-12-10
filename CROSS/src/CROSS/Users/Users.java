package CROSS.Users;

import java.util.TreeSet;
import java.util.Arrays;

// Abstract class. I assume that i don't want to handle different users dabatases.
public abstract class Users {
    
    // Add and search in complexity O(log n).
    private static TreeSet<User> users = new TreeSet<User>();

    public static void addUser(User user) throws Exception {

        if (users.contains(user)) {
            throw new Exception("User already exists.");
        }

        users.add(user);

        // Prevent double file writes.
        // That because:
        // loadUsers() read from file user X -> call addUser() to add it in RAM -> writeUserOnFile() write user X on file AGAIN.
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getClassName().equals(DBUsersInterface.class.getName()) && Arrays.asList(DBUsersInterface.class.getMethods()).stream().map(m -> m.getName()).toList().contains(stackTraceElement.getMethodName())) {
                return;
            }
        }
        DBUsersInterface.writeUserOnFile(user);

    }

    public static User getUser(User user) {
        
        User result = null;
        result = users.ceiling(user);
        if (result != null && result.equals(user)) {
            return result;
        }
        return null;
        
    }

    public static Integer getUsersSize() {
        return users.size();
    }

    // The original TreeSet is not returned.
    // Is returned a copy of the TreeSet.
    // The idea is to avoid the modification of the TreeSet from outside.
    // This to keep the DB file and the TreeSet in sync.
    public static TreeSet<User> getUsersCopy() {
        TreeSet<User> usersCopy = new TreeSet<User>();
        for (User user : users) {
            usersCopy.add(user);
        }
        return usersCopy;
    }

}
