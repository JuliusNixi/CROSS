package CROSS.Users;

import java.util.TreeSet;
import CROSS.Exceptions.InvalidUser;

/**
 * Users class.
 * Abstract class. I assume that i don't want to handle different users dabatases at the same time.
 * This class will rapresent the users database in RAM.
 * @version 1.0
 * @see User
 * @see DBUsersInterface
 * @see InvalidUser
 */
public abstract class Users {
    
    // Add and search in complexity O(log n).
    private static TreeSet<User> users = new TreeSet<User>();

    /**
     * Add a user to the database.
     * The user is added to the TreeSet and to the file.
     * If not present, the user line id is set to the current size of the database, since it's appended at the end of the file.
     * @param user The user to add.
     * @throws InvalidUser If the user already exists.
     * @throws NullPointerException If the user is null.
     * @throws RuntimeException If an error occurs while writing the user on file or if the method loadUsers() is not found.
     */
    public static void addUser(User user) throws InvalidUser, NullPointerException, RuntimeException {

        if (user == null) {
            throw new NullPointerException("User is null.");
        }

        if (users.contains(user)) {
            throw new InvalidUser("User already exists.");
        }

        if (user.getFileLineId() == null) {
            user.setFileLineId(DBUsersInterface.calculateFileLines() + 1);
        }

        // Added user to the TreeSet.
        users.add(user);

        // Prevent double file writes when the method is called from DBUsersInterface.loadUsers().
        // That because:
        // loadUsers() read from file user X -> call addUser() to add it in RAM -> writeUserOnFile() write user X on file AGAIN.
        String method = null;
        try {
            method = DBUsersInterface.class.getMethod("loadUsers").getName();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method loadUsers() not found.");
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getMethodName().equals(method)) {
                return;
            }
        }

        // Write user on file.
        try {
            DBUsersInterface.writeUserOnFile(user);
        } catch (RuntimeException ex) {
            users.remove(user);
            throw new RuntimeException("Error writing user on file.");
        }

    }

    /**
     * Update a user in the database.
     * The user is updated in the TreeSet and in the file.
     * The method will updated the password of the userOld with the password of the userNew.
     * @param userOld The user to update.
     * @param userNew The new user to replace the old one.
     * @throws InvalidUser If the userOld does not exist.
     * @throws NullPointerException If the userOld or userNew are null.
     * @throws RuntimeException If an error occurs while writing the newUser on file.
     */
    public static void updateUser(User userOld, User userNew) throws InvalidUser, NullPointerException, RuntimeException {

        if (userOld == null) {
            throw new NullPointerException("Old user is null.");
        }
        if (userNew == null) {
            throw new NullPointerException("New user is null.");
        }

        if (!users.contains(userOld)) {
            throw new InvalidUser("User does not exist.");
        }

        users.remove(userOld);

        // Write newUser on file and remove old user from file.
        try {
            DBUsersInterface.updateUserOnFile(userOld, userNew);
        } catch (RuntimeException ex) {
            users.add(new User(userOld.getUsername(), userOld.getPassword()));
            throw new RuntimeException("Error writing newUser on file.");
        }

    }

    // GETTERS
    /**
     * Find a user with its username in the database and get its password.
     * @param username The username of the user to find.
     * @return A string with the password of the user if the user is found, null otherwise.
     * @throws NullPointerException If the username is null.
     */
    public static String getUserPassword(String username) throws NullPointerException {

        if (username == null) {
            throw new NullPointerException("Username cannot be null.");
        }
        
        // Search by username, password is a placeholder, cannot be null due to check in User constructor.
        User toSearch = new User(username, "placeholder");
        User result = users.ceiling(toSearch);
        if (result != null && result.getUsername().equals(username)) {
            return String.format("%s", result.getPassword());
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

    /**
     * Get the users database as list of string lines, one for each user.
     * @return The users database as list of string lines.
     */
    public static String toStringUsers() {
        String result = "";
        for (User user : users) {
            result += user.toString() + "\n";
        }
        return result;
    }

}
