package cross.users.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import cross.exceptions.InvalidUser;
import cross.users.User;
import cross.utils.FileHandler;

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

    // Users database file to handle and its streams.
    private static File file = null;
    private static FileInputStream fileIn = null;
    private static FileOutputStream fileOut = null;

    // Buffered to improve performance.
    private static BufferedInputStream fileInBuffered = null;
    private static BufferedOutputStream fileOutBuffered = null;

    // Users database file content as String.
    private static String fileContent = null;

    // Number of empty lines in the file.
    // "empty" means that these lines have been overwritten with spaces after an user update (change of the user's credentials).
    
    public static Long emptyFileLines = 0L;

    // Costant for file handling.
    private static final String FILE_INIT = "[\n]\n";

    // Users loaded, true if the users have been loaded from the database users file, false otherwise.
    // Used in the Users class to check if the users have been already loaded before getting / searching an user.
    private static Boolean usersLoaded = false;

    // These methods names are used to check who calls some functions of this class, so to perform some different operations in case the call comes from one of them.
    private static final String UPDATED_METHOD_NAME = "updateUserOnFile";
    private static final String ADDUSER_METHOD_NAME = "addUser";

    // FILE HANDLING
    /**
     *
     * Sets the users database file to handle.
     *
     * Synchronized ON CLASS to avoid multiple threads to set the file at the same time.
     * 
     * If the file is not found, it will be created with the initial content.
     *
     * @param filePath The path to the users database file as String.
     *
     * @throws IllegalArgumentException If the file is not a JSON file.
     * @throws NullPointerException If the file path is null.
     * @throws IllegalStateException If the file is already attached.
     * @throws IOException If there's an I/O error creating the file when not found.
     *
     */
    public static void setFile(String filePath) throws IllegalArgumentException, NullPointerException, IllegalStateException, IOException {

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
                throw new IllegalStateException("Database users file already attached.");
            }

            try {
                file = new File(filePath);

                fileIn = new FileInputStream(file);
                fileInBuffered = new BufferedInputStream(fileIn);

                fileOut = new FileOutputStream(file, true);
                fileOutBuffered = new BufferedOutputStream(fileOut);

                DBUsersInterface.filePath = filePath;

                System.out.printf("DB Users file %s attached.\n", filePath);
            } catch (FileNotFoundException ex) {

                System.out.printf("DB Users file %s not found. Creating it.\n", filePath);

                // Create an empty file.
                try {
                    file.createNewFile();

                    fileIn = new FileInputStream(file);
                    fileInBuffered = new BufferedInputStream(fileIn);

                    fileOut = new FileOutputStream(file, true);
                    fileOutBuffered = new BufferedOutputStream(fileOut);

                    fileOutBuffered.write(FILE_INIT.getBytes());
                    fileOutBuffered.flush();

                    DBUsersInterface.filePath = filePath;

                    System.out.printf("DB Users file %s created, initialized and attached.\n", filePath);
                } catch (IOException ex2) {
                    throw new IOException("Error creating the users database file.");
                }

            }

        }

    }
    /**
     *
     * Reads the file attached, previously setted with setFile().
     *
     * This fills the file content variable with the file content as String.
     *
     * Synchronized ON CLASS to avoid multiple threads to read the file at the same time.
     *
     * @throws IOException If there's an I/O error reading the file.
     * @throws IllegalStateException If the file is not attached or the file content is already readed.
     *
     */
    public static void readFile() throws IOException, IllegalStateException {

        synchronized (DBUsersInterface.class) {

            // File not attached.
            if (file == null || fileIn == null || DBUsersInterface.filePath == null) {
                throw new IllegalStateException("Database users file not attached. Set file before with setFile().");
            }

            // File content already readed.
            if (DBUsersInterface.fileContent != null) {
                throw new IllegalStateException("Database users file already readed.");
            }

            // Initialize empty file lines.
            DBUsersInterface.emptyFileLines = Long.valueOf(0);

            // Read file.
            StringBuilder fileContentBuilder = new StringBuilder();
            Integer buffSize = 1024;
            byte[] buffer = new byte[buffSize];
            try {

                while (true) {
                    int bytesRead = fileInBuffered.read(buffer, 0, buffSize);
                    if (bytesRead == -1) {
                        // End of file.
                        break;
                    }
                    fileContentBuilder.append(new String(buffer, 0, bytesRead));
                }

                DBUsersInterface.fileContent = fileContentBuilder.toString();

                System.out.printf("DB Users file %s readed.\n", filePath);
            } catch (IOException | IndexOutOfBoundsException ex) {
                throw new IOException("Error reading the database users file.");
            }

        }

    }

    // ON FILE USERS OPERATIONS
    /**
     *
     * Appends an user to the users database file attached.
     *
     * This appends the user to the users database file, at the end, without rewriting all the file.
     *
     * Synchronized ON CLASS to avoid multiple threads to write on the file at the same time.
     * Synchronized ON USER to avoid multiple threads to change the user's properties during the execution of this method.
     *
     * @param user The User to write (append) to the users database file.
     *
     * @throws IllegalStateException If the file content is not loaded.
     * @throws NoSuchMethodException If the updateUserOnFile() method in this class is not found.
     * @throws NullPointerException If the user is null.
     * @throws IOException If there's an I/O error.
     *
     */
    public static void writeUserOnFile(User user) throws IllegalStateException, NoSuchMethodException, NullPointerException, IOException {

        synchronized (DBUsersInterface.class) {

            // Null check.
            if (user == null) {
                throw new NullPointerException("User to append to the users database file cannot be null.");
            }

            synchronized (user) {

                // File not attached.
                if (DBUsersInterface.fileContent == null) {
                    throw new IllegalStateException("Users database file content is needed to append an user to the users database file. Call readFile() before.");
                }

                String newFileContent = String.format("%s", DBUsersInterface.fileContent);

                // Remove last line.
                try {
                    FileHandler.removeLastLine(DBUsersInterface.file);

                    // Updating also the file content to keep it in sync with the file on disk without reading it again.
                    int lastNewLine = newFileContent.lastIndexOf('\n');
                    if (lastNewLine == -1) {
                        throw new IOException();
                    }
                    newFileContent = newFileContent.substring(0, lastNewLine);
                } catch (IOException | IndexOutOfBoundsException ex) {
                    throw new IOException("Error removing the last line from the users database file.");
                }

                // Append user on file.
                try {

                    String jsonUser = new Gson().toJson(user);

                    // Remove all the '\n' from the JSON user.
                    jsonUser = String.join("", jsonUser.trim().split("\n"));
                    if (fileContent.compareTo(FILE_INIT) == 0) {
                        // First user.
                        /*
                        * [\n
                        *
                        */
                        jsonUser = jsonUser + "\n" + "]";
                    } else {
                        // Other users.
                        /*
                        * [
                        *  {user1}\n
                        *
                        */

                        // Need to remove the last char "\n" before writing the ','.
                        char c = FileHandler.removeLastChar(DBUsersInterface.file);
                        try {
                            newFileContent = newFileContent.substring(0, newFileContent.length() - 1);
                        } catch (IndexOutOfBoundsException ex) {
                            throw new IOException();
                        }

                        jsonUser = """
                                   ,
                                   """ + jsonUser + "\n]";

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
                                if (c == ' ') {
                                    jsonUser = jsonUser.substring(1);
                                }
                                break;
                            }
                        }

                    }

                    // Append to the file.
                    fileOutBuffered.write(jsonUser.getBytes());
                    fileOutBuffered.flush();

                    // Update the file content by adding the new user.
                    newFileContent = newFileContent + jsonUser;

                    // Update the main file content variable.
                    DBUsersInterface.fileContent = newFileContent;

                } catch (IOException ex) {
                    throw new IOException("Error appending the new user to the users database file.");
                } catch (NoSuchMethodException ex) {
                    throw new NoSuchMethodException(String.format("Method %s in the DBUsersInterface not found.", UPDATED_METHOD_NAME));
                }

            }

        }

    }

    /**
     *
     * Updates an user in the users database file attached.
     * Updates means that the user's password is changed.
     *
     * This overwrites the old user (the user with the old password) line in the users database file with spaces and appends the new user (the user with the new password) to the end of file.
     * The old user position in the file is given by its file line id.
     *
     * This is done to avoid rewriting all the users database file.
     *
     * WARNING: It creates fragmentation in the users database file, but that's the best I can do since the files are not perfect to be used as databases.
     *
     * Synchronized ON CLASS to avoid multiple threads to update the users database file at the same time.
     * Synchronized ON OLD USER and NEW USER to avoid multiple threads to change the users' properties during the execution of this method.
     *
     * @param oldUser The old user to update.
     * @param newUser The new user to update with.
     *
     * @throws IllegalStateException If the users database file content is not loaded.
     * @throws NullPointerException If the old user or the new user are null.
     * @throws IllegalArgumentException If the old user file line id IS null or the new user file line id is NOT null or the new user username is different from the old user username.
     * @throws IOException If there's an I/O error editing the old user line from the users database file or if an error occurs while writing the user on the users database file. 
     * @throws RuntimeException If the old user file line id is wrong.
     * @throws InvalidUser If the NEW user already exists in the database.
     * @throws NoSuchMethodException If the loadUsers() method is not found or if the updateUserOnFile() method in this class is not found in the DBUsersInterface class.
     *
     */
    public static void updateUserOnFile(User oldUser, User newUser) throws IllegalStateException, NullPointerException, RuntimeException, IOException, InvalidUser, NoSuchMethodException, IllegalArgumentException {

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
                        throw new IllegalStateException("Users database file content is needed to update an user in the users database file. Call readFile() before.");
                    }

                    // Null file line id checks.
                    if (oldUser.getFileLineId() == null) {
                        throw new IllegalArgumentException("Old user file line id to use to update an user in the users database file cannot be null.");
                    }
                    if (newUser.getFileLineId() != null) {
                        throw new IllegalArgumentException("New user file line id to use to update an user in the users database file MUST be null.");
                    }

                    // Checking if the new user has the same username as the old user.
                    if (oldUser.getUsername().compareToIgnoreCase(newUser.getUsername()) != 0) {
                        throw new IllegalArgumentException("New user username to use to update an user in the users database file cannot be different from the old user username, only the password can be changed.");
                    }

                    // Removing the old user line from the users database file by overwriting it with spaces.
                    try {
                        FileHandler.editLine(DBUsersInterface.file, oldUser.getFileLineId());

                        // Updating also the file content to keep it in sync with the file on disk without reading it again.
                        String[] lines = DBUsersInterface.fileContent.split("\n");
                        Integer spaces = lines[oldUser.getFileLineId().intValue()].length();
                        String newLine = " ".repeat(spaces);
                        lines[oldUser.getFileLineId().intValue()] = newLine;
                        DBUsersInterface.fileContent = String.join("\n", lines);

                        DBUsersInterface.emptyFileLines++;
                    } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
                        ex.printStackTrace();
                        throw new RuntimeException("Error editing the old user line from the users database file, wrong line id.");
                    } catch (IOException ex) {
                        throw new IOException("I/O error editing the old user line from the users database file.");
                    }

                    // Write the new user.
                    // The exceptions throwed are backwarded to the caller.
                    Users.addUser(newUser);

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

        return String.format("%s", filePath);

    }

    /**
     *
     * Used to calculate the number of lines in the users database file attached without reading the file again every time.
     *
     * This is used to calculate the new file line id to assign it to a new user before adding it to the users database file.
     *
     * Synchronized ON CLASS to avoid multiple threads to calculate the file lines at the same time.
     *
     * @return The number of lines in the users database file attached as Long.
     *
     * @throws IllegalStateException If the file content is not readed or the users are not loaded and we are not in the addUser() method in the Users class used to load users.
     * @throws NoSuchMethodException If the addUser() method in the Users class is not found.
     *
     */
    public static Long calculateFileLines() throws IllegalStateException, NoSuchMethodException {

        synchronized (DBUsersInterface.class) {

            // Need to skip this check if this method is called by the addUser() method in the Users class.
            Boolean checkDatabaseLoaded = true;
            String method = null;
            try {
                method = Users.class.getMethod(ADDUSER_METHOD_NAME, User.class).getName();
            }catch (NoSuchMethodException ex) {
                throw new NoSuchMethodException(String.format("Method %s in the Users class not found.", ADDUSER_METHOD_NAME));
            }

            String className = Users.class.getName();
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if (stackTraceElement.getClassName().compareToIgnoreCase(className) == 0 && stackTraceElement.getMethodName().compareToIgnoreCase(method) == 0) {
                    checkDatabaseLoaded = false;
                    break;
                }
            }
            if (checkDatabaseLoaded && (DBUsersInterface.fileContent == null || DBUsersInterface.usersLoaded() == false)) {
                throw new IllegalStateException("Database users file content must be readed and the users database must be loaded to calculate the file lines. Call readFile() and loadUsers() before.");
            }

            // + 1 for the first line with "[".
            // + emptyFileLines for the empty lines in the users database file, so the users database file lines where the users that have been updated and so the corresponding lines are overwritten with spaces.
            // + Users.getUsersSize() for the users in the database, so the filled lines.
            return 1 + Users.getUsersSize() + DBUsersInterface.emptyFileLines;

        }


    }

    /**
     *
     * Check if the users have been already loaded from the users database file in the Users class.
     *
     * @return True if the users have been loaded from the users database file, false otherwise.
     *
     */
    public static Boolean usersLoaded() {

        return DBUsersInterface.usersLoaded;

    }

    // MAIN SUPPORT (CALLED FROM THE Users CLASS) METHOD
    /**
     *
     * Loads users from the users database file (previously readed and stored in the file content variable) to Users class (in RAM).
     *
     * Synchronized ON CLASS to avoid multiple threads to load users at the same time.
     *
     * Before using this method, the file must be readed with readFile().
     *
     * At all the users loaded is added an unique file line id. This is used to update the users database file on user update (password change) without rewriting all the file.
     *
     * THIS METHOD IS MEANT TO BE USED ONLY AS SUPPORT FROM THE Users CLASS.
     * CALL THIS METHOD FROM THE Users CLASS.
     *
     * @throws IllegalStateException If the file is not readed or the users are already loaded.
     * @throws JsonSyntaxException If there's an error parsing the JSON users database file content.
     * @throws InvalidUser If the user already exists in the database.
     * @throws NoSuchMethodException If the loadUsers() method is not found or if the updateUserOnFile() method in this class is not found in the DBUsersInterface class.
     * @throws IOException If an error occurs while writing the user on the users database file.
     * 
     */
    public static void loadUsers() throws IllegalStateException, JsonSyntaxException, InvalidUser, NoSuchMethodException, IOException {

        synchronized (DBUsersInterface.class) {

            // Users database file content not readed check.
            if (DBUsersInterface.fileContent == null) {
                throw new IllegalStateException("Database users file not read. Read it before with readFile().");
            }

            // Users already loaded.
            if (DBUsersInterface.usersLoaded() == true) {
                throw new IllegalStateException("Users database already loaded.");
            }


            // IMPORTANT: The file could be already initialized before from the program, but no users has been added yet.
            if (DBUsersInterface.fileContent.compareTo(FILE_INIT) == 0) {

                DBUsersInterface.usersLoaded = true;

                System.out.printf("Users loaded from DB Users file %s.\n", DBUsersInterface.filePath);

                return;
            }

            // Not empty file.
            try {

                String[] lines = DBUsersInterface.fileContent.split("\n");
                for (String line : lines) {
                    if (line.compareTo("[") == 0 || line.compareTo("]") == 0) continue;
                    line = line.replaceAll(" ", "").replaceAll("\\[", "").replaceAll("\\]", "");
                    if (line.compareTo("") == 0) {
                        DBUsersInterface.emptyFileLines++;
                    }else {
                        if (line.charAt(line.length() - 1) == ',') {
                            line = line.substring(0, line.length() - 1);
                        }
                        User user = new Gson().fromJson(line, User.class);
                        // Exceptions throwed by the addUser() method are backwarded to the caller.
                        Users.addUser(user);
                    }
                }

            } catch (JsonSyntaxException ex) {
                throw new JsonSyntaxException("Error parsing the JSON users database file content.");
            }

            DBUsersInterface.usersLoaded = true;

            System.out.printf("Users loaded from DB Users file %s.\n", DBUsersInterface.filePath);

        }

    }

}
