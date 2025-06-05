package cross.users.db;

import cross.api.notifications.Notification;
import cross.api.notifications.Trade;
import cross.exceptions.InvalidUser;
import cross.server.Server;
import cross.users.User;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.TreeSet;
import com.google.gson.JsonSyntaxException;

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

    // Add and search users in complexity O(log n).
    private final static TreeSet<User> users = new TreeSet<>();

    // These methods names are used to check who calls some functions of this class, so to perform some different operations in case the call comes from one of them.
    private static final String LOADUSERS_METHOD_NAME = "loadUsers";

    private static Server server;

    // USERS HANDLING
    /**
     *
     * Adds an user to the users database.
     * The user is added BOTH to the TreeSet in memory and to the users database file if not present.
     *
     * If not present in the users database file, to add the user to it, the user's line id is set to the current size of the users database file, since it's appended at the end of the file.
     *
     * Synchronized ON CLASS method to prevent multiple threads to add users at the same time.
     * Synchronized ON USER object to prevent multiple threads change user's properties during the add operation.
     *
     * @param user The user to add to the users database.
     *
     * @throws InvalidUser If the user already exists in the database.
     * @throws NullPointerException If the user is null.
     * @throws NoSuchMethodException If the loadUsers() method is not found or if the updateUserOnFile() method in this class is not found in the DBUsersInterface class.
     * @throws IOException If an error occurs while writing the user on the users database file.
     * @throws IllegalStateException If the users database file content is not loaded.
     *
     */
    public static void addUser(User user) throws InvalidUser, NoSuchMethodException, NullPointerException, IOException, IllegalStateException {

        synchronized (Users.class) {

            // Null checks.
            if (user == null) {
                throw new NullPointerException("User to add to the users database cannot be null.");
            }

            synchronized (user) {

                // Already exists check.
                if (users.contains(user)) {
                    throw new InvalidUser("User to add to the users database already exists.");
                }

                // Adds user to the TreeSet.
                users.add(user);

                // Set the file line id if not present.
                if (user.getFileLineId() == null) {
                    user.setFileLineId(DBUsersInterface.calculateFileLines());
                }

                // Prevent double file writes when the method is called from DBUsersInterface.loadUsers().
                // That's because:
                // DBUsersInterface.loadUsers() read from file user X -> call addUser() to add it in RAM -> writeUserOnFile() write user X on file AGAIN.
                String method = null;
                try {
                    method = DBUsersInterface.class.getMethod(LOADUSERS_METHOD_NAME).getName();
                }catch (NoSuchMethodException ex) {
                    throw new NoSuchMethodException(String.format("Method %s in the Users class not found.", LOADUSERS_METHOD_NAME));
                }

                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                String className = DBUsersInterface.class.getName();
                for (StackTraceElement stackTraceElement : stackTrace) {
                    // The method's name is needed since addUser is called not only from loadUsers but also from the updateUserOnFile method.
                    // If the caller is loadUsers, then exit before writing on file, otherwise continue.
                    if (stackTraceElement.getClassName().compareTo(className) == 0 && stackTraceElement.getMethodName().compareTo(method) == 0) {
                        // Exit before writing on file.
                        return;
                    }
                }

                // Write user on file.
                try {
                    DBUsersInterface.writeUserOnFile(user);
                } catch (IOException ex) {

                    // Remove user from TreeSet.
                    users.remove(user);

                    // Remove file line id.
                    user.setFileLineId(null);

                    // Forwarding the exception's message.
                    throw new IOException(ex.getMessage());

                } catch (IllegalStateException ex) {

                    // Remove user from TreeSet.
                    users.remove(user);

                    // Remove file line id.
                    user.setFileLineId(null);

                    // Forwarding the exception's message.
                    throw new IllegalStateException(ex.getMessage());

                } catch (NoSuchMethodException ex) {

                    // Remove user from TreeSet.
                    users.remove(user);

                    // Remove file line id.
                    user.setFileLineId(null);

                    // Forwarding the exception's message.
                    throw new NoSuchMethodException(ex.getMessage());

                }

            }

        }

    }

    /**
     *
     * Updates an user in the users database.
     * The user is updated BOTH in the TreeSet in memory and in the users database file.
     *
     * The method will update the password of the old user with the password of the new user.
     *
     * Synchronized ON CLASS method to prevent multiple threads to update an user at the same time.
     * Synchronized ON USER objects to prevent multiple threads change users' properties during the update operation.
     *
     * @param userOld The old user to update.
     * @param userNew The new user to replace the old one with (the password will be updated).
     *
     * @throws InvalidUser If the old user does not exist in the users database.
     * @throws NullPointerException If the old user or the new user are null.
     * @throws IllegalArgumentException If the old user file line id IS null or the new user file line id is NOT null or the new user username is different from the old user username.
     * @throws IOException If an error occurs while writing the user on the users database file.
     * @throws IllegalStateException If the users database file content is not loaded.
     * @throws RuntimeException If an error occurs while updating the user in the users database.
     * @throws NoSuchMethodException If the loadUsers() method is not found or if the updateUserOnFile() method in this class is not found in the DBUsersInterface class.
     * @throws IllegalAccessException If the old user password does not match the one in the users database.
     * @throws SecurityException If the old user password is equal to the new user password.
     * 
     */
    public static void updateUser(User userOld, User userNew) throws InvalidUser, NullPointerException, IllegalArgumentException, IOException, IllegalStateException, RuntimeException, NoSuchMethodException, IllegalAccessException, SecurityException {

        synchronized (Users.class) {

            // Null checks.
            if (userOld == null) {
                throw new NullPointerException("Old user to update in the users database cannot be null.");
            }
            if (userNew == null) {
                throw new NullPointerException("New user to update with in the users database cannot be null.");
            }

            // Users not loaded from the database users file yet.
            if (DBUsersInterface.usersLoaded() == false) {
                throw new IllegalStateException("Users not loaded from the database users file yet. Call loadUsers() before.");
            }

            synchronized (userOld) {
                synchronized (userNew) {

                    // User does not exist.
                    if (!users.contains(userOld)) {
                        throw new InvalidUser("Old user to update in the users database does not exist.");
                    }   

                    // Old password match check.
                    User userOldDB = getUserByUsername(userOld.getUsername());
                    if (userOld.getPassword().compareTo(userOldDB.getPassword()) != 0) {
                        throw new IllegalAccessException("Old user password does not match the one in the users database.");
                    }
                    userOld = userOldDB;

                    // Old password equals new password check.
                    if (userOld.getPassword().compareTo(userNew.getPassword()) == 0) {
                        throw new SecurityException("Old user password cannot be equal to the new user password.");
                    }

                    // Null file line id checks.
                    if (userOld.getFileLineId() == null) {
                        throw new IllegalArgumentException("Old user file line id in the users database cannot be null.");
                    }
                    if (userNew.getFileLineId() != null) {
                        throw new IllegalArgumentException("New user file line id in the users database MUST be null.");
                    }

                    // Remove old user from TreeSet.
                    users.remove(userOld);

                    // Write the new user on file and remove old user from file.
                    // Also add the new user to the TreeSet.
                    try {
                        DBUsersInterface.updateUserOnFile(userOld, userNew);
                    } catch (IllegalStateException ex) {

                        // Add the old user back to the TreeSet.
                        if (!users.contains(userOld)) {
                            users.add(userOld);
                        }

                        // Forwarding the exception's message.
                        throw new IllegalStateException(ex.getMessage());

                    } catch (IOException ex) {

                        // Add the old user back to the TreeSet.
                        if (!users.contains(userOld)) {
                            users.add(userOld);
                        }

                        // Forwarding the exception's message.
                        throw new IOException(ex.getMessage());

                    } catch (IllegalArgumentException ex) {

                        // Add the old user back to the TreeSet.
                        if (!users.contains(userOld)) {
                            users.add(userOld);
                        }

                        // Forwarding the exception's message.
                        throw new IllegalArgumentException(ex.getMessage());

                    } catch (RuntimeException ex) {

                        // Add the old user back to the TreeSet.
                        if (!users.contains(userOld)) {
                            users.add(userOld);
                        }

                        // Forwarding the exception's message.
                        throw new RuntimeException(ex.getMessage());

                    } catch (NoSuchMethodException ex) {

                        // Add the old user back to the TreeSet.
                        if (!users.contains(userOld)) {
                            users.add(userOld);
                        }

                        // Forwarding the exception's message.
                        throw new NoSuchMethodException(ex.getMessage());
                    }
                }
            }

        }

    }
    
    /**
     *
     * Loads all the users from the JSON users database file in the TreeSet in memory.
     *
     * Synchronized ON CLASS method to prevent multiple threads to load users from the file at the same time.
     *
     * It's a wrapper method for the DBUsersInterface.loadUsers() method.
     *
     * @throws IllegalStateException If the file is not readed or the users are already loaded.
     * @throws JsonSyntaxException If there's an error parsing the JSON users database file content.
     * @throws InvalidUser If the user already exists in the database.
     * @throws NoSuchMethodException If the loadUsers() method is not found or if the updateUserOnFile() method in this class is not found in the DBUsersInterface class.
     * @throws IOException If an error occurs while writing the user on the users database file.
     * 
     */
    public static void loadUsers(Server server) throws IllegalStateException, JsonSyntaxException, IOException, NoSuchMethodException, InvalidUser {

        // server can be null to preform tests.
        Users.server = server;

        synchronized (Users.class) {

            // Simply backward the exceptions to the caller.
            DBUsersInterface.loadUsers();

        }

    }

    // GETTERS
    /**
     *
     * Find an user by its username as String in the users database.
     *
     * @param username The username of the user to find as String.
     *
     * @return A User object found with the given username and its corresponding password if the user is found, null otherwise.
     *
     * @throws NullPointerException If the username is null.
     * @throws IllegalStateException If the users are not loaded from the database users file yet.
     *
     */
    public static User getUserByUsername(String username) throws NullPointerException, IllegalStateException {

        // Null check.
        if (username == null) {
            throw new NullPointerException("Username to search in the users database cannot be null.");
        }

        // Users not loaded from the database users file yet.
        if (DBUsersInterface.usersLoaded() == false) {
            throw new IllegalStateException("Users not loaded from the database users file yet. Call loadUsers() before.");
        }

        // Search by username, password is a placeholder, cannot be null due to check in User's constructor.
        User toSearch = new User(username, "placeholder");
        User result = users.ceiling(toSearch);
        if (result != null && result.getUsername().equalsIgnoreCase(username)) {
            return result;
        }
        return null;

    }
    /**
     *
     * Get the size of the users database.
     *
     * @return The size of the users database (number of users) as an Integer.
     *
     */
    public static Integer getUsersSize() {

        // If the database is not loaded yet, return 0, do not throw an exception, otherwise problems during the users loading.

        return (Integer) users.size();

    }
    /**
     *
     * Used to get the user (from the users database) currently logged in with a specific client socket if present.
     *
     * @param clientSocket The client socket to be used to get the user currently logged in with it.
     *
     * @return The user currently logged in with the given client socket if present, null otherwise.
     *
     * @throws NullPointerException If the client socket is null.
     *
     */
    public static User getLoggedInUser(Socket clientSocket) throws NullPointerException {

        synchronized (Users.class) {

            // Null check.
            if (clientSocket == null) {
                throw new NullPointerException("Client socket to be used to get the user currently logged in cannot be null.");
            }

            for (User user : users) {
                if (user.containsSocket(clientSocket)) {
                    return user;
                }
            }
            return null;
        }

    }

    // TO STRING
    /**
     *
     * Get a string with the whole users database. Each user is on a new line.
     *
     * Synchronized ON CLASS method to prevent multiple threads to modify the users database during the execution of this method.
     *
     * @return The users database as list of string lines, joined in an unique string by '\n'.
     *
     * @throws IllegalStateException If the users are not loaded from the database users file yet.
     *
     */
    public static String toStringUsers() throws IllegalStateException {

        synchronized (Users.class) {

            // Users not loaded from the database users file yet.
            if (DBUsersInterface.usersLoaded() == false) {
                throw new IllegalStateException("Users not loaded from the database users file yet. Call loadUsers() before.");
            }

            String result = "";
            for (User user : Users.users) {
                // To string is itself synchronized on the user object.
                result += user.toString() + "\n";
            }

            return result;

        }

    }

    // LOGIN
    /**
     *
     * Used to login an user with its username and the client socket if its password matches the one in the users database.
     *
     * @param user The user to be used to login.
     * @param clientSocket The client socket to be used to login the user.
     *
     * @throws NullPointerException If the user or the client socket are null.
     * @throws IllegalStateException If the users database is not loaded.
     * @throws InvalidUser If the user does not exist in the users database or the password does not match.
     * @throws IllegalAccessException If the user is already logged in with the given client socket.
     * 
     */
    public static void login(User user, Socket clientSocket) throws NullPointerException, IllegalStateException, InvalidUser, IllegalAccessException {

        synchronized (Users.class) {

            // Null checks.
            if (user == null) {
                throw new NullPointerException("User to used to login cannot be null.");
            }
            if (clientSocket == null) {
                throw new NullPointerException("Socket to be used to login an user cannot be null.");
            }

            synchronized (user) {

                User userFound;
                try {
                    userFound = getUserByUsername(user.getUsername());
                } catch (IllegalStateException ex) {
                    // Users database error.
                    throw new IllegalStateException(ex.getMessage());
                }

                if (userFound == null) {
                    // User not found in the users database.
                    throw new InvalidUser("User to login does not exist in the users database.");
                }

                if (userFound.getPassword().compareTo(user.getPassword()) != 0) {
                    // Password does not match.
                    throw new InvalidUser("Password provided to login does not match the one in the users database.");
                }

                try {
                    userFound.addSocket(clientSocket);
                } catch (IllegalArgumentException ex) {
                    // Socket already in the list of the sockets logged with this user.
                    throw new IllegalAccessException("User already logged in.");
                }

                // Register the client for notifications.
                if (server == null) {
                    throw new IllegalStateException("Server not initialized. Cannot register the client for notifications.");
                }

                // '/' needed.
                InetSocketAddress datagramSocket = server.getUdpSocketAddressForTcpSocketAddress(String.format("/%s:%d", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));
                if (datagramSocket != null) {
                    userFound.addNotificationSocket(datagramSocket);
                }

            }

        }
    
    }
    
    // IS LOGGED IN
    /**
     *
     * Used to check if an user is logged in with a specific client socket and the user object provided.
     *
     * @param user The user to be used to check if is logged in.
     * @param clientSocket The client socket to be used to check if the user is logged in.
     *
     * @return True if the given user is logged in with the given client socket, false otherwise.
     *
     * @throws NullPointerException If the user or the client socket are null.
     *
     */
    public static Boolean isLoggedIn(User user, Socket clientSocket) throws NullPointerException {

        synchronized (Users.class) {

            // Null checks.
            if (user == null) {
                throw new NullPointerException("User to be used to check if is logged in cannot be null.");
            }
            if (clientSocket == null) {
                throw new NullPointerException("Socket to be used to check if the user is logged in cannot be null.");
            }

            synchronized (user) {

                return user.containsSocket(clientSocket);

            }

        }

    }
    /**
     *
     * Used to check if an user (from the users database) is currently logged in with a specific client socket.
     *
     * @param clientSocket The client socket to be used to check if a user is logged in with it.
     *
     * @return True if a user is logged in with the given client socket, false otherwise.
     *
     * @throws NullPointerException If the client socket is null.
     *
     */
    public static Boolean isLoggedIn(Socket clientSocket) throws NullPointerException {

        return getLoggedInUser(clientSocket) != null;

    }

    // LOGOUT
    /**
     *
     * Used to logout an user from a specific client socket.
     *
     * @param user The user to be used to logout.
     * @param clientSocket The socket to be used to logout the user.
     *
     * @throws NullPointerException If the user or the socket are null.
     * @throws IllegalArgumentException If the user is not logged in with the given socket.
     *
     */
    public static void logout(User user, Socket clientSocket) throws NullPointerException, IllegalArgumentException {

        synchronized (Users.class) {

            // Null checks.
            if (user == null) {
                throw new NullPointerException("User to be used to logout cannot be null.");
            }
            if (clientSocket == null) {
                throw new NullPointerException("Socket to be used to logout an user cannot be null.");
            }

            synchronized (user) {

                try {
                    user.removeSocket(clientSocket);
                } catch (IllegalArgumentException ex) {
                    // Socket not found in the list of the sockets logged with this user.
                    throw new IllegalArgumentException("User not logged in.");
                }

                // Remove the notification socket if present to not see notifications anymore after logout.
                InetSocketAddress userNotificationSocket = server.getUdpSocketAddressForTcpSocketAddress(String.format("/%s:%d", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));
                if (userNotificationSocket != null && user.containsNotificationSocket(userNotificationSocket)) {

                    user.removeNotificationSocket(userNotificationSocket);

                }

            }

        }
        
    }

    // UPDATE CREDENTIALS
    /**
     *
     * Updates an user in the users database.
     * The user is updated BOTH in the TreeSet in memory and in the users database file.
     *
     * The method will update the password of the old user with the password of the new user.
     *
     * Synchronized ON CLASS method to prevent multiple threads to update an user at the same time.
     * Synchronized ON USER objects to prevent multiple threads change users' properties during the update operation.
     * 
     * THIS METHOD IS A WRAPPER FOR THE updateUser() METHOD.
     *
     * @param userOld The old user to update.
     * @param userNew The new user to replace the old one with (the password will be updated).
     *
     * @throws InvalidUser If the old user does not exist in the users database.
     * @throws NullPointerException If the old user or the new user are null.
     * @throws IllegalArgumentException If the old user file line id IS null or the new user file line id is NOT null or the new user username is different from the old user username.
     * @throws IOException If an error occurs while writing the user on the users database file.
     * @throws IllegalStateException If the users database file content is not loaded.
     * @throws RuntimeException If an error occurs while updating the user in the users database.
     * @throws NoSuchMethodException If the loadUsers() method is not found or if the updateUserOnFile() method in this class is not found in the DBUsersInterface class.
     * @throws IllegalAccessException If the old user password does not match the one in the users database.
     * 
     */
    public static void updateCredentials(User userOld, User userNew) throws NullPointerException, InvalidUser, IllegalArgumentException, IOException, IllegalStateException, RuntimeException, NoSuchMethodException, IllegalAccessException {

        synchronized (Users.class) {

            // Null checks.
            if (userOld == null) {
                throw new NullPointerException("Old user to be used to update credentials cannot be null.");
            }
            if (userNew == null) {
                throw new NullPointerException("New user to be used to update credentials cannot be null.");
            }

            synchronized (userOld) {
                synchronized (userNew) {

                    // Update the user in the users database.
                    try {
                        updateUser(userOld, userNew);
                    } catch (InvalidUser ex) {
                        throw new InvalidUser(ex.getMessage());
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalArgumentException(ex.getMessage());
                    } catch (IOException ex) {
                        throw new IOException(ex.getMessage());
                    } catch (IllegalStateException ex) {
                        throw new IllegalStateException(ex.getMessage());
                    } catch (RuntimeException ex) {
                        throw new RuntimeException(ex.getMessage());
                    } catch (NoSuchMethodException ex) {
                        throw new NoSuchMethodException(ex.getMessage());
                    } catch (IllegalAccessException ex) {
                        throw new IllegalAccessException(ex.getMessage());
                    }

                }
            }

        }
    }
    

    public static void notifyUsers(Notification notification) {

        // Null checks.
        if (notification == null) {
            throw new NullPointerException("Notification to send cannot be null.");
        }

        // Server not initialized.
        if (server == null) {
            throw new IllegalStateException("Server not initialized. Cannot send the notification to the user.");
        }


        // set to prevent dispatching the same notification to the same user multiple times.
        // because if the user has 2 orders in the trade, the notification will be sent twice.
        TreeSet<User> usersSet = new TreeSet<>();
        for (Trade trade : notification.getTrades()) {
            
            for (User user : users) {

                if (trade.getUser().compareTo(user) == 0) {

                    usersSet.add(user);

                }

            }

        }

        for (User user : usersSet) {
            for (InetSocketAddress socket : user.getNotificationsSockets()) {

                String msg = notification.toJSONString();
                byte[] data = msg.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, socket);
                try {
                    server.getDatagramSocket().send(sendPacket);
                } catch (IOException ex) {
                    System.err.println("DEBUG: Error sending notification to " + socket.getAddress() + ":" + socket.getPort() + ".");
                }
                System.out.println("DEBUG: Sent to " + socket.getAddress() + ":" + socket.getPort() + " a notification: " + msg);

            }
        }

    }
 
    public static void closeNotificationSocket(Socket clientTcpSocket) {
        
        server.unregisterClientForNotifications(String.format("/%s:%d", clientTcpSocket.getInetAddress().getHostAddress(), clientTcpSocket.getPort()));

    }

}

