package CROSS.Users;

import CROSS.Exceptions.InvalidUser;
import java.util.TreeSet;

/**
 * 
 * Users class is an abstract one.
 * That's because I assume that I don't want to handle different users dabatases at the same time.
 * So I will use all static methods and variables.
 * 
 * This class will rapresent the users database file in RAM.
 * It will be synchronized (best effort will be made to do so) with the users database file on disk.
 * That's will be done with the support of the DBUsersInterface class.
 * 
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
     * Add an user to the database.
     * The user is added BOTH to the TreeSet in memory and to the users database file if not present.
     * 
     * If not present in the file, to add the user to it, the user's line id is set to the current size of the users database file, since it's appended at the end of the file.
     * 
     * Synchronized ON CLASS method to prevent multiple threads to add users at the same time.
     * Synchronized ON USER object to prevent multiple threads change user's properties during the add operation.
     * 
     * @param user The user to add.
     * 
     * @throws InvalidUser If the user already exists in the database.
     * @throws NullPointerException If the user is null.
     * @throws Exception If an error occurs while writing the user on the users database file.
     * 
     */
    public static void addUser(User user) throws InvalidUser, NullPointerException, Exception {

        synchronized (Users.class) {

            // Null checks.
            if (user == null) {
                throw new NullPointerException("User to add to the database cannot be null.");
            }

            synchronized (user) {

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
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                String className = DBUsersInterface.class.getName();
                for (StackTraceElement stackTraceElement : stackTrace) {
                    // The method's name is needed since addUser is called not only from loadUsers but also from the updateUserOnFile method.
                    // If the caller is loadUsers, then exit before writing on file, otherwise continue.
                    if (stackTraceElement.getClassName().equals(className) && stackTraceElement.getMethodName().equals("loadUsers")) {
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

                    // Remove file line id.
                    user.setFileLineId(null);

                    // Forwarding the exception's message.
                    throw new Exception(ex.getMessage());
                }

            }

        }

    }
    /**
     * 
     * Update an user in the database.
     * The user is updated BOTH in the TreeSet and in the users database file.
     * 
     * The method will update the password of the old user with the password of the new user.
     * 
     * Synchronized ON CLASS method to prevent multiple threads to update an user at the same time.
     * Synchronized ON USER objects to prevent multiple threads change users' properties during the update operation.
     * 
     * @param userOld The old user to update.
     * @param userNew The new user to replace the old one with (the password will be updated).
     * 
     * @throws InvalidUser If the old user does not exist in the database.
     * @throws NullPointerException If the old user or the new user are null.
     * @throws Exception If an error occurs while updating the user in the the users database file.
     * @throws IllegalArgumentException If the old user file line id is null or the new user file line id is not null.
     * 
     */
    public static void updateUser(User userOld, User userNew) throws InvalidUser, NullPointerException, Exception, IllegalArgumentException {

        synchronized (Users.class) {

            // Null checks.
            if (userOld == null) {
                throw new NullPointerException("Old user to update cannot be null.");
            }
            if (userNew == null) {
                throw new NullPointerException("New user to update with cannot be null.");
            }

            synchronized (userOld) {
                synchronized (userNew) {

                    // User does not exist.
                    if (!users.contains(userOld)) {
                        throw new InvalidUser("Old user to update does not exist in the database.");
                    }

                    // Null file line id checks.
                    if (userOld.getFileLineId() == null) {
                        throw new IllegalArgumentException("Old user file line id cannot be null.");
                    }
                    if (userNew.getFileLineId() != null) {
                        throw new IllegalArgumentException("New user file line id MUST be null.");
                    }

                    // Remove old user from TreeSet.
                    users.remove(userOld);

                    // Write the new user on file and remove old user from file.
                    // Also add the new user to the TreeSet.
                    try {
                        DBUsersInterface.updateUserOnFile(userOld, userNew);
                    } catch (Exception ex) {
                        // Add the old user back to the TreeSet.
                        if (!users.contains(userOld)) {
                            users.add(userOld);
                        }

                        // Forwarding the exception's message.
                        throw new Exception(ex.getMessage());
                    }

                }
            }

        }

    }
    /**
     * 
     * Load all the users from the JSON users database file.
     * 
     * It's a synchronized ON CLASS method to prevent multiple threads to load users from the file at the same time.
     * 
     * The users are loaded in the TreeSet in memory from the users database file.
     * 
     * It's a wrapper method for the DBUsersInterface.loadUsers() method.
     * 
     * @throws Exception If an error occurs while loading the users from the users database file.
     * 
     */
    public static void loadUsers() throws Exception {

        synchronized (Users.class) {
            try {
                DBUsersInterface.loadUsers();
            } catch (Exception ex) {
                // Forwarding the exception's message.
                throw new Exception(ex.getMessage());
            }
        }

    }

    // GETTERS
    /**
     * 
     * Find an user with its username as String in the users database.
     * 
     * Synchronized on user to avoid its properties to be changed during the execution of this method.
     * 
     * @param username The username of the user to find as String.
     * 
     * @return A User object found with the given username if the user is found, null otherwise.
     * 
     * @throws NullPointerException If the username is null.
     * @throws RuntimeException If the users are not loaded from the database users file yet.
     * 
     */
    public static User getUser(String username) throws NullPointerException, RuntimeException {

        // Null check.
        if (username == null) {
            throw new NullPointerException("Username to search in the users database cannot be null.");
        }

        synchronized (username) {

            // Users not loaded from the database users file yet.
            if (DBUsersInterface.usersLoaded() == false) {
                throw new RuntimeException("Users not loaded from the database users file yet. Call loadUsers() before.");
            }
            
            // Search by username, password is a placeholder, cannot be null due to check in User's constructor.
            User toSearch = new User(username, "placeholder");
            User result = users.ceiling(toSearch);
            if (result != null && result.getUsername().equalsIgnoreCase(username)) {
                return result;
            }
            return null;

        }
        
    }
    /**
     * 
     * Get the size of the database.
     * 
     * @return The size of the database (number of users) as an Integer.
     * 
     */
    public static Integer getUsersSize() {

        // If the database is not loaded yet, return 0, do not throw an exception, otherwise problems during the users loading.

        return users.size();

    }

    /**
     * 
     * Get a string with the whole users database. Each user is on a new line.
     * 
     * Synchronized ON CLASS method to prevent multiple threads to modify the users database during the execution of this method.
     * 
     * @return The users database as list of string lines, joined in an unique string by '\n'.
     * 
     * @throws RuntimeException If the users are not loaded from the database users file yet.
     * 
     */
    public static String toStringUsers() throws RuntimeException {

        synchronized (Users.class) {

            // Users not loaded from the database users file yet.
            if (DBUsersInterface.usersLoaded() == false) {
                throw new RuntimeException("Users not loaded from the database users file yet. Call loadUsers() before.");
            }

            String result = "";
            for (User user : Users.users) {
                // To string is itself synchronized on the user object.
                result += user.toString() + "\n";
            }
            
            return result;

        }

    }

}
