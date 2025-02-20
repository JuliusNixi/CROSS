package CROSS.Users;

import CROSS.Exceptions.InvalidUser;
import java.io.IOException;
import java.util.TreeSet;

/**
 * 
 * Users class is an abstract one.
 * That's because I assume that I don't want to handle different users dabatases at the same time.
 * 
 * This class will rapresent the users database file in RAM.
 * It will be synchronized (best effort will be made to do so) with the database file on disk.
 * That's will be done with the support of the DBUsersInterface class.
 * ALL OPERATIONS MUST BE DONE THROUGH THIS CLASS, THE DBUsersInterface CLASS IS NOT TO BE USED DIRECTLY.
 * 
 * It uses a TreeSet to store the users in memory to add and search in complexity O(log n).
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see User
 * 
 * @see DBUsersInterface
 * 
 * @see InvalidUser
 * 
 */
public abstract class Users {
    
    // Add and search in complexity O(log n).
    private static TreeSet<User> users = new TreeSet<User>();

    // USERS HANDLING
    /**
     * 
     * Add a user to the database.
     * The user is added BOTH to the TreeSet in memory and to the database file if not present.
     * 
     * If not present in the file, the user line id is set to the current size of the database file, since it's appended at the end of the file.
     * 
     * Synchronized method to prevent multiple threads to write on the file at the same time.
     * 
     * @param user The user to add.
     * 
     * @throws InvalidUser If the user already exists.
     * @throws NullPointerException If the user is null.
     * @throws NoSuchMethodException If the method loadUsers() is not found.
     * @throws Exception If an error occurs while writing the user on file.
     * 
     */
    public static synchronized void addUser(User user) throws InvalidUser, NullPointerException, NoSuchMethodException, Exception {

        // Null checks.
        if (user == null) {
            throw new NullPointerException("User to add to the database cannot be null.");
        }

        // Already exists check.
        if (users.contains(user)) {
            throw new InvalidUser("User to add to the database already exists.");
        }

        // Set the file line id if not present.
        if (user.getFileLineId() == null) {
            user.setFileLineId(DBUsersInterface.calculateFileLines() + 1);
        }

        // Adds user to the TreeSet.
        users.add(user);

        // Prevent double file writes when the method is called from DBUsersInterface.loadUsers().
        // That's because:
        // DBUsersInterface.loadUsers() read from file user X -> call addUser() to add it in RAM -> writeUserOnFile() write user X on file AGAIN.
        String method = null;
        try {
            method = DBUsersInterface.class.getMethod("loadUsers").getName();
        } catch (NoSuchMethodException ex) {
            throw new NoSuchMethodException("Method loadUsers() not found in the DBUsersInterface class.");
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getMethodName().equals(method)) {
                // Exit before writing on file.
                return;
            }
        }

        // Write user on file.
        try {
            DBUsersInterface.writeUserOnFile(user);
        } catch (Exception ex) {
            // Remove user from TreeSet.
            users.remove(user);

            // Forwarding the exception's message.
            throw new IOException(ex.getMessage());
        }

    }
    /**
     * 
     * Update a user in the database.
     * 
     * The user is updated BOTH in the TreeSet and in the database file.
     * 
     * The method will update the password of the old user with the password of the new user.
     * 
     * Synchronized method to prevent multiple threads to write on the file at the same time.
     * 
     * @param userOld The old user to update.
     * @param userNew The new user to replace the old one.
     * 
     * @throws InvalidUser If the old user does not exist.
     * @throws NullPointerException If the old user or the new user are null.
     * @throws Exception If an error occurs while writing the new user on file.
     * 
     */
    public static synchronized void updateUser(User userOld, User userNew) throws InvalidUser, NullPointerException, Exception {

        // Null checks.
        if (userOld == null) {
            throw new NullPointerException("Old user to update cannot be null.");
        }
        if (userNew == null) {
            throw new NullPointerException("New user to update cannot be null.");
        }

        // User does not exist.
        if (!users.contains(userOld)) {
            throw new InvalidUser("Old user to update does not exist.");
        }

        // Remove old user from TreeSet.
        users.remove(userOld);

        // Write the new user on file and remove old user from file.
        // Also add the new user to the TreeSet.
        try {
            DBUsersInterface.updateUserOnFile(userOld, userNew);
        } catch (Exception ex) {
            // Add the old user back to the TreeSet.
            users.add(new User(userOld.getUsername(), userOld.getPassword()));

            // Forwarding the exception's message.
            throw new Exception(ex.getMessage());
        }

    }
    /**
     * 
     * Load all the users from the JSON database file.
     * 
     * It's a synchronized method to prevent multiple threads to read from the file at the same time.
     * 
     * The users are loaded in the TreeSet in memory from the database file.
     * 
     * It's a wrapper method for the DBUsersInterface.loadUsers() method.
     * 
     * @throws Exception If an error occurs while loading the users from the database file.
     * 
     */
    public static synchronized void loadUsers() throws Exception {

        try {
            DBUsersInterface.loadUsers();
        } catch (Exception ex) {
            // Forwarding the exception's message.
            throw new Exception(ex.getMessage());
        }

    }

    // GETTERS
    /**
     * 
     * Find a user with its username as String in the database.
     * 
     * @param username The username of the user to find as String.
     * 
     * @return A User object found with the given username if the user is found, null otherwise.
     * 
     * @throws NullPointerException If the username is null.
     * 
     */
    public static User getUser(String username) throws NullPointerException {

        // Null check.
        if (username == null) {
            throw new NullPointerException("Username to search in the database cannot be null.");
        }
        
        // Search by username, password is a placeholder, cannot be null due to check in User constructor.
        User toSearch = new User(username, "placeholder");
        User result = users.ceiling(toSearch);
        if (result != null && result.getUsername().equals(username)) {
            return new User(result.getUsername(), result.getPassword());
        }
        return null;
        
    }
    /**
     * 
     * Get the size of the database.
     * 
     * @return The size of the database (number of users) as an Integer.
     * 
     */
    public static Integer getUsersSize() {
        return users.size();
    }

    /**
     * 
     * Get a string with the whole users database as list of string lines, one for each user.
     * 
     * @return The users database as list of string lines, joined in an unique string by '\n'.
     * 
     */
    public static String toStringUsers() {

        String result = "";
        for (User user : users) {
            result += user.toString() + "\n";
        }
        
        return result;

    }

}
