package CROSS.Users;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import CROSS.Utils.FileHandler;

/**
 * 
 * This class is an interface to handle the users database file.
 * It's used by the Users class as support to load and save users from and to a JSON users database file.
 * 
 * Abstract class because I assume that I don't want to handle different users databases at the same time.
 * So I will use only static methods and variables.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see User
 * 
 * @see Users
 * 
 * @see FileHandler
 * 
 */
public abstract class DBUsersInterface {

    // Users database file path.
    private static String filePath = null;

    // Users database file to handle with streams.
    private static File file = null;
    private static FileInputStream fileIn = null;
    private static FileOutputStream fileOut = null;

    // Users database file content as String.
    private static String fileContent = null;

    // Number of empty lines in the file.
    // empty means that the line has been overwritten with spaces after an user update.
    private static Long emptyFileLines = 0L;

    // Costants for file handling.
    private static final String FILE_READED = "loaded";
    private static final String FILE_INIT = "[\n]\n";

    // Users loaded, true if the users has been loaded from the database users file, false otherwise.
    // Used in the Users class to check if the users has been already loaded before getting / searching an user.
    private static Boolean usersLoaded = false;

    private static final String UPDATED_METHOD_NAME = "updateUserOnFile";

    // FILE HANDLING
    /**
     * 
     * Set the users database file to handle.
     * 
     * Synchronized ON CLASS to avoid multiple threads to set the file at the same time.
     * 
     * @param filePath The path to the users database file as String.
     * 
     * @throws IllegalArgumentException If the file is not a JSON file.
     * @throws NullPointerException If the file path is null.
     * @throws RuntimeException If the file is already attached.
     * @throws IOException If there's an I/O error creating the file when not found.
     * 
     */
    public static void setFile(String filePath) throws IllegalArgumentException, NullPointerException, RuntimeException, IOException {

        synchronized (DBUsersInterface.class) {

            // Null check.
            if (filePath == null) {
                throw new NullPointerException("Database users file path to set cannot be null.");
            }

            // Check if the file is a JSON file.
            if (!filePath.endsWith(".json")) {
                throw new IllegalArgumentException("Database users file to set must be a JSON file.");
            }

            // Database file already attached.
            if (DBUsersInterface.filePath != null) {
                throw new RuntimeException("Database users file already attached.");
            }

            try {
                file = new File(filePath);
                fileIn = new FileInputStream(file);
                fileOut = new FileOutputStream(file, true);

                DBUsersInterface.filePath = filePath;

                System.out.printf("DB Users file %s attached.\n", filePath);
            } catch (FileNotFoundException ex) {

                System.out.printf("DB Users file %s not found. Creating it.\n", filePath);

                // Create an empty file.
                try {
                    file.createNewFile();
                    fileIn = new FileInputStream(file);
                    fileOut = new FileOutputStream(file, true);

                    DBUsersInterface.filePath = filePath;

                    System.out.printf("DB Users file %s created and attached.\n", filePath);
                } catch (IOException ex2) {
                    throw new IOException("Error creating the database users file.");
                }

            }

        }

    }   
    /**
     * 
     * Read the file attached, previously setted with setFile().
     * 
     * This fills the file content variable with the file content as String.
     * 
     * Synchronized ON CLASS to avoid multiple threads to read the file at the same time.
     * 
     * @throws RuntimeException If the file is not attached or the file content is already readed.
     * @throws IOException If there's an I/O error reading the file.
     * 
     */
    public static void readFile() throws IOException, RuntimeException {

        synchronized (DBUsersInterface.class) {

            // File not attached.
            if (file == null || fileIn == null || DBUsersInterface.filePath == null) {
                throw new RuntimeException("Database users file not attached. Set file before with setFile().");
            }

            // File content already readed.
            if (DBUsersInterface.fileContent != null) {
                throw new RuntimeException("Database users file already readed.");
            }

            // Initialize empty file lines.
            DBUsersInterface.emptyFileLines = Long.valueOf(0);

            // Read file.
            // Buffered to improve performance.
            BufferedInputStream fileBuffered = new BufferedInputStream(fileIn);
            StringBuilder fileContentBuilder = new StringBuilder();
            Integer buffSize = 1024;
            byte[] buffer = new byte[buffSize];
            try {

                while (true) {
                    int bytesRead = fileBuffered.read(buffer, 0, buffSize);
                    if (bytesRead == -1) {
                        // End of file.
                        break;
                    }
                    fileContentBuilder.append(new String(buffer, 0, bytesRead));
                }

                DBUsersInterface.fileContent = fileContentBuilder.toString();

                System.out.printf("DB Users file %s readed.\n", filePath);
            } catch (IOException ex) {
                throw new IOException("Error reading the database users file.");
            } 

        }

    }

    // ON FILE USERS OPERATIONS
    /**
     * 
     * Write an user on the users database file attached.
     * 
     * This appends the user to the users database file, at the end, without rewriting all the file.
     * 
     * Synchronized ON CLASS to avoid multiple threads to write on the file at the same time.
     * Syncronized ON USER to avoid multiple threads to change the user's properties during the execution of this method.
     * 
     * @param user The User to write.
     * 
     * @throws RuntimeException If the file content is not loaded.
     * @throws NoSuchMethodException If the update user method is not found.
     * @throws NullPointerException If the user is null.
     * @throws IOException If there's an I/O error.
     * @throws JsonSyntaxException If there's an error parsing the JSON user to write in the database users file.
     * 
     */
    public static void writeUserOnFile(User user) throws RuntimeException, NoSuchMethodException, NullPointerException, IOException, JsonSyntaxException {

        synchronized (DBUsersInterface.class) {

            // Null check.
            if (user == null) {
                throw new NullPointerException("User to write on file cannot be null.");
            }

            synchronized (user) {

                // File not attached.
                if (DBUsersInterface.fileContent == null) {
                    throw new RuntimeException("Users database file content is needed to write an user on file. Call readFile() before.");
                }

                // Remove last line.
                try {
                    FileHandler.removeLastLine(DBUsersInterface.file);
                } catch (IOException ex) {
                    // Forwarding exception's message.
                    throw new IOException(ex.getMessage());
                }

                // Write user on file.
                // Buffered to improve performance.
                try {
                    BufferedOutputStream fileOutBuffered = new BufferedOutputStream(fileOut);

                    String jsonUser = new Gson().toJson(user);

                    // TODO: Remove if works without these, added transient.
                    // Remove fileLineId from JSON.
                    // JsonObject jsonObject = JsonParser.parseString(jsonUser).getAsJsonObject();
                    // jsonObject.remove("fileLineId");
                    // jsonUser = new Gson().toJson(jsonObject);

                    // // Remove connectedClients from JSON.
                    // jsonObject = JsonParser.parseString(jsonUser).getAsJsonObject();
                    // jsonObject.remove("connectedClients");
                    // jsonUser = new Gson().toJson(jsonObject);

                    // Remove all the '\n' from the JSON user.
                    jsonUser = String.join("", jsonUser.trim().split("\n"));
                    if (fileContent.equals(FILE_INIT)) {
                        /*
                        * [\n
                        * 
                        */
                        jsonUser = jsonUser + "\n" + "]";
                    } else {
                        /* 
                        * [
                        *  {user1}\n
                        * 
                        */

                        // Need to remove the last char "\n" before writing the ','.
                        char c = FileHandler.removeLastChar(DBUsersInterface.file);
                        jsonUser = "," + "\n" + jsonUser + "\n]";

                        // Check if this operation is part of an update user and if the user is the last in the file.
                        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                        String method = null;
                        try {
                            method = DBUsersInterface.class.getMethod(UPDATED_METHOD_NAME, User.class, User.class).getName();
                        }catch (NoSuchMethodException ex) {
                            throw new NoSuchMethodException(String.format("Method %s in the DBUsersInterface not found.", UPDATED_METHOD_NAME));
                        }
                        for (StackTraceElement stackTraceElement : stackTrace) {
                            if (method != null && stackTraceElement.getMethodName().compareToIgnoreCase(method) == 0) {
                                // Remove the ',' if the user updated is the last in the file.
                                if (c == ' ')
                                    jsonUser = jsonUser.substring(1);
                                break;
                            }
                        }

                    }

                    // Append to the file.
                    fileOutBuffered.write(jsonUser.getBytes());
                    fileOutBuffered.close();

                    fileOut = new FileOutputStream(file, true);

                } catch (JsonSyntaxException | IndexOutOfBoundsException ex) {
                    throw new JsonSyntaxException("Error parsing the JSON user to write in the users database file.");
                } catch (IOException ex) {
                    throw new IOException("Error writing the new user in the users database file.");
                }

            }

        }

    }
    /**
     * 
     * Update an user on the users database file attached.
     * 
     * This overwrites the old user line on the users database file with spaces and appends the new user to the end of file.
     * The old user position in the file is found by his file line id.
     * 
     * This is done to avoid rewriting all the file.
     * 
     * WARNING: It creates fragmentation in the file, but that's the best I can do since the files are not meant to be databases.
     * 
     * Synchronized ON CLASS to avoid multiple threads to update the file at the same time.
     * Syncronized ON OLD USER and NEW USER to avoid multiple threads to change the users' properties during the execution of this method.
     * 
     * @param oldUser The old user to update.
     * @param newUser The new user to update with.
     * 
     * @throws RuntimeException If the file content is not loaded.
     * @throws Exception If the editing of the users database file or adding a new user to it fails.
     * @throws NullPointerException If the old user or the new user are null.
     * @throws IllegalArgumentException If the old user file line id is null or the new user file line id is not null.
     * 
     */
    public static void updateUserOnFile(User oldUser, User newUser) throws RuntimeException, Exception, NullPointerException, IllegalArgumentException {

        synchronized (DBUsersInterface.class) {

            // Null check.
            if (oldUser == null) {
                throw new NullPointerException("The old user to update in the users database file cannot be null.");
            }
            if (newUser == null) {
                throw new NullPointerException("The new user to update with in the users database file cannot be null.");
            }

            synchronized (oldUser) {
                synchronized (newUser) {

                    // File not attached.
                    if (DBUsersInterface.fileContent == null) {
                        throw new RuntimeException("File content is needed to update an user in the users database file. Call readFile() before.");
                    }

                    // Null file line id checks.
                    if (oldUser.getFileLineId() == null) {
                        throw new IllegalArgumentException("Old user file line id cannot be null.");
                    }
                    if (newUser.getFileLineId() != null) {
                        throw new IllegalArgumentException("New user file line id MUST be null.");
                    }

                    // Removing the old user line from the file by overwriting it with spaces.
                    try {
                        FileHandler.editLine(DBUsersInterface.file, oldUser.getFileLineId());
                        DBUsersInterface.emptyFileLines++;
                    }catch (Exception ex) {
                        // Forwarding exception's message.
                        throw new Exception(ex.getMessage());
                    }

                    // Write the new user.
                    try {
                        Users.addUser(newUser);
                    }catch (Exception ex) {
                        // Forwarding exception's message.
                        throw new Exception(ex.getMessage());
                    }

                }
            }

        }

    }
    
    // GETTERS
    /**
     * 
     * Get the users database file path.
     * 
     * @return The users database file path as String.
     * 
     */
    public static String getFilePath() {

        return filePath;

    }
    /**
     * 
     * Used to calculate the number of lines in the users database file attached without reading the file again every time.
     * 
     * This is used to calculate the new file line id to assign it to a new user before adding it to the database.
     * 
     * Syncronized ON CLASS to avoid multiple threads to calculate the file lines at the same time.
     * 
     * @return The number of lines in the users database file attached as Long.
     * 
     * @throws RuntimeException If the file content is not loaded or the users are not loaded and we are not in the addUser() method in the Users class used to load users.
     * 
     */
    public static Long calculateFileLines() throws RuntimeException {

        synchronized (DBUsersInterface.class) {

            // Need to skip this check if this method is called by the addUser() method in the Users class.
            Boolean checkDatabaseLoaded = true;
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String className = Users.class.getName();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if (stackTraceElement.getClassName().equals(className) && stackTraceElement.getMethodName().equals("addUser")) {
                    checkDatabaseLoaded = false;
                    break;
                }
            }
            if (checkDatabaseLoaded && (DBUsersInterface.fileContent == null || DBUsersInterface.usersLoaded() == false)) {
                throw new RuntimeException("Database users file content is needed to calculate the file lines. Call readFile() before.");
            }

            // + 1 for the first line with "[".
            // + emptyFileLines for the empty lines on the users database file, so the file lines where the users that have been updated and the corresponding lines overwritten with spaces.
            // + Users.getUsersSize() for the users in the database, so the filled lines.
            return 1 + Users.getUsersSize() + DBUsersInterface.emptyFileLines;

        }


    }
    /**
     * 
     * Check if the users have been already loaded from the users database file.
     * 
     * @return True if the users have been loaded from the users database file, false otherwise.
     * 
     */
    public static Boolean usersLoaded() {

        return DBUsersInterface.usersLoaded;

    }

    // MAIN SUPPORT METHOD
    /**
     * 
     * Load users from the file (previously readed and stored in the file content variable) to Users class (in RAM).
     * 
     * Synchronized ON CLASS to avoid multiple threads to load users at the same time.
     * 
     * Before using this method, the file must be readed with readFile().
     * 
     * At all the users loaded is added an unique file line id. This is used to update the file on user update without rewriting all the file.
     * 
     * THIS METHOD IS MEANT TO BE USED ONLY AS SUPPORT FROM THE Users CLASS.
     * CALL THIS METHOD FROM THE Users CLASS.
     * 
     * @throws RuntimeException If the file is not readed or the users are already loaded.
     * @throws JsonSyntaxException If there's an error parsing the JSON users database file content.
     * @throws Exception If there's an error loading the users from the JSON users database file to the Users class.
     * @throws IOException If there's an I/O error initializing the empty users database file.
     * 
     */
    public static void loadUsers() throws RuntimeException, JsonSyntaxException, Exception, IOException {

        synchronized (DBUsersInterface.class) {

            // Users database file content not readed check.
            if (DBUsersInterface.fileContent == null) {
                throw new RuntimeException("Database users file not read. Read it before with readFile().");
            }

            // Users already loaded.
            if (DBUsersInterface.usersLoaded() == true) {
                throw new RuntimeException("Users database already loaded.");
            }

            // Empty file, initialize it.
            if (DBUsersInterface.fileContent.isEmpty()) {
                try {
                    BufferedOutputStream fileOutBuffered = new BufferedOutputStream(fileOut);
                    fileOutBuffered.write(FILE_INIT.getBytes());

                    fileOutBuffered.close();
                    fileOut = new FileOutputStream(file, true);

                    DBUsersInterface.fileContent = FILE_INIT;

                    DBUsersInterface.usersLoaded = true;

                    System.out.printf("Empty DB Users file %s. Initailized it.\n", DBUsersInterface.filePath);

                    return;
                } catch (IOException ex) {
                    throw new IOException("Error initializing empty users database file.");
                }
            }

            // IMPORTANT: The file could be already initialized before from the program, but no users has been added yet.
            if (DBUsersInterface.fileContent.equals(FILE_INIT)) {
                DBUsersInterface.fileContent = FILE_INIT;

                DBUsersInterface.usersLoaded = true;

                System.out.printf("Users loaded from DB Users file %s.\n", DBUsersInterface.filePath);

                return;
            }

            // Not empty file.
            try {

                User[] users = new Gson().fromJson(DBUsersInterface.fileContent, User[].class);

                // Add users to Users class (RAM).
                for (User user : users) {
                    Users.addUser(user);
                }

                // File content is no longer needed.
                // To save memory.
                DBUsersInterface.fileContent = FILE_READED;

                DBUsersInterface.usersLoaded = true;

                System.out.printf("Users loaded from DB Users file %s.\n", DBUsersInterface.filePath);
            } catch (JsonSyntaxException ex) {
                throw new JsonSyntaxException("Error parsing the JSON users database file.");
            } catch (Exception ex) {
                // addUser() exception.
                // Forwarding exception's message.
                throw new Exception(ex.getMessage());
            }

        }

    }

}
