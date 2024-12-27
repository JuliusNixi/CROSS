package CROSS.Users;

import java.util.TreeSet;
import CROSS.Exceptions.InvalidUser;
import java.util.Arrays;

/**
 * Users class.
 * Abstract class. I assume that i don't want to handle different users dabatases at the same time.
 * This class will rapresent the users database in RAM.
 * @version 1.0
 * @see User
 */
public abstract class Users {
    
    // Add and search in complexity O(log n).
    private static TreeSet<User> users = new TreeSet<User>();

    /**
     * Add a user to the database.
     * The user is added to the TreeSet and to the file.
     * @param user The user to add.
     * @throws InvalidUser If the user already exists.
     * @throws NullPointerException If the user is null.
     */
    public static void addUser(User user) throws InvalidUser, NullPointerException {

        if (user == null) {
            throw new NullPointerException("User is null.");
        }

        if (users.contains(user)) {
            throw new InvalidUser("User already exists.");
        }

        users.add(user);

        // Prevent double file writes when the method is called from DBUsersInterface.
        // That because:
        // loadUsers() read from file user X -> call addUser() to add it in RAM -> writeUserOnFile() write user X on file AGAIN.
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            // If the current method is called from DBUsersInterface class.
            if (stackTraceElement.getClassName().equals(DBUsersInterface.class.getName())
            &&
            // And the method is one of the methods of DBUsersInterface.
            Arrays.asList(DBUsersInterface.class.getMethods()).stream().map(m -> m.getName()).toList().contains(stackTraceElement.getMethodName())) {
                return;
            }
        }

        // Write user on file.
        try {
            DBUsersInterface.writeUserOnFile(user);
        } catch (RuntimeException ex) {
            users.remove(user);
            throw new InvalidUser("Error writing user on file.");
        }

    }

    /**
     * Update a user in the database.
     * The user is updated in the TreeSet and in the file.
     * @param user The user to update.
     * @throws InvalidUser If the user does not exist.
     * @throws NullPointerException If the user is null.
     */
    public static void updateUser(User user) throws InvalidUser, NullPointerException {

        if (user == null) {
            throw new NullPointerException("User is null.");
        }

        if (!users.contains(user)) {
            throw new InvalidUser("User does not exist.");
        }

        users.remove(user);
        users.add(user);

        // Prevent double file writes when the method is called from DBUsersInterface.
        // That because:
        // loadUsers() read from file user X -> call addUser() to add it in RAM -> writeUserOnFile() write user X on file AGAIN.
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            // If the current method is called from DBUsersInterface class.
            if (stackTraceElement.getClassName().equals(DBUsersInterface.class.getName())
            &&
            // And the method is one of the methods of DBUsersInterface.
            Arrays.asList(DBUsersInterface.class.getMethods()).stream().map(m -> m.getName()).toList().contains(stackTraceElement.getMethodName())) {
                return;
            }
        }

        // Write user on file.
        try {
            // TODO: Write the remove or update user on file in DBUsersInterface.
            DBUsersInterface.writeUserOnFile(user);
        } catch (RuntimeException ex) {
            users.remove(user);
            throw new InvalidUser("Error writing user on file.");
        }

    }

    // GETTERS
    /**
     * Get a user from the database.
     * @param user The user to get.
     * @return The user if it exists, null otherwise.
     * @throws NullPointerException If the user is null.
     */
    public static User getUser(User user) throws NullPointerException {

        if (user == null) {
            throw new NullPointerException("User is null.");
        }
        
        User result = null;
        result = users.ceiling(user);
        if (result != null && result.equals(user)) {
            return result;
        }
        return null;
        
    }
    /**
     * Get the size of the database.
     * @return The size of the database (number of users) as an Integer.
     */
    public static Integer getUsersSize() {
        return users.size();
    }

    @Override
    public String toString() {
        String result = "";
        for (User user : users) {
            result += user.toString() + "\n";
        }
        return result;
    }

}
