package CROSS.Users;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import CROSS.Exceptions.InvalidUser;

/**
 * This class is an interface to handle users database file.
 * It's used by the Users class to load and save users from and to a JSON file.
 * Abstract class because i assume that i don't want to handle different users databases at the same time.
 * @version 1.0
 * @see User
 * @see Users
 */
public abstract class DBUsersInterface {
    
    private static String filePath = null;

    private static File file = null;
    private static FileInputStream fileIn = null;
    private static FileOutputStream fileOut = null;

    private static String fileContent = null;

    private static Long emptyFileLines;

    // Costants for file handling.
    private static final String fileReaded = "loaded";
    private static final String fileInit = "[\n]\n";

    /**
     * Set the file to handle.
     * @param filePath The path to the file.
     * @throws IllegalArgumentException If the file is not a JSON file.
     * @throws NullPointerException If the file path is null.
     * @throws RuntimeException If the file is already attached.
     */
    public static void setFile(String filePath) throws IllegalArgumentException, NullPointerException, RuntimeException {

        if (filePath == null) {
            throw new NullPointerException("File path cannot be null.");
        }

        if (!filePath.endsWith(".json")) {
            throw new IllegalArgumentException("File must be a JSON file.");
        }

        if (DBUsersInterface.filePath != null) {
            throw new RuntimeException("File already attached.");
        }

        try {
            file = new File(filePath);
            fileIn = new FileInputStream(file);
            fileOut = new FileOutputStream(file, true);

            DBUsersInterface.filePath = filePath;

            System.out.printf("DB Users file %s attached.\n", filePath);
        } catch (FileNotFoundException ex) {
            System.out.printf("DB Users file %s not found.\n", filePath);
            // Create empty file.
            try {
                file.createNewFile();
                fileIn = new FileInputStream(file);
                fileOut = new FileOutputStream(file, true);

                DBUsersInterface.filePath = filePath;

                System.out.printf("DB Users file %s created and attached.\n", filePath);
            } catch (IOException ex2) {
                // TODO: Error handling.
            }
        } catch (Exception ex) {
            // TODO: Error handling.
        }

    }   

    /**
     * Get the file path.
     * @return The file path.
     */
    public static String getFilePath() {
        return String.format("%s", filePath);
    }

    /**
     * Read the file attached.
     * This fills the fileContent variable.
     * Before using this method, the file must be attached with setFile().
     * @throws RuntimeException If the file is not attached.
     */
    public static void readFile() throws RuntimeException {

        if (fileIn == null || DBUsersInterface.filePath == null) {
            throw new RuntimeException("File not attached. Set file before with setFile().");
        }

        if (DBUsersInterface.fileContent != null) {
            throw new RuntimeException("File already readed.");
        }

        // Initialize emptyFileLines.
        DBUsersInterface.emptyFileLines = Long.valueOf(0);

        // Read file.
        // Buffered to improve performance.
        BufferedInputStream fileBuffered = new BufferedInputStream(fileIn);

        StringBuilder fileContentBuilder = new StringBuilder();
        try {

            int data = fileBuffered.read();
            while (data != -1) {
                fileContentBuilder.append((char) data);
                data = fileBuffered.read();
            }

            DBUsersInterface.fileContent = fileContentBuilder.toString();

            System.out.printf("File %s readed.\n", filePath);
        } catch (IOException ex) {
            // TODO: Error handling.
        } catch (Exception ex) {
            // TODO: Error handling.
        }
    }

    /**
     * Load users from file (previously stored in the fileContent var) to Users (in RAM).
     * Before using this method, the file must be readed with readFile().
     * At all the users loaded is added its file line id. Used to update the file on user update without rewriting all the file.
     * @throws RuntimeException If the file is not readed.
     */
    public static void loadUsers() throws RuntimeException {

        if (DBUsersInterface.fileContent == null) {
            throw new RuntimeException("File not read. Read it before with readFile().");
        }

        if (Users.getUsersSize() > 0) {
            throw new RuntimeException("Users already loaded.");
        }

        // Empty file, initialize it.
        if (DBUsersInterface.fileContent.isEmpty()) {
            try {
                BufferedOutputStream fileOutBuffered = new BufferedOutputStream(fileOut);
                fileOutBuffered.write(fileInit.getBytes());
                fileOutBuffered.close();

                fileOut = new FileOutputStream(file, true);

                fileContent = fileInit;

                System.out.printf("Empty file %s. Initailized it.\n", filePath);

                return;
            } catch (Exception ex) {
                // TODO: Error handling.
            }
        }

        // IMPORTANT: The file could be already initialized before from the program, but no users has been added.
        if (DBUsersInterface.fileContent.equals(fileInit)) {
            fileContent = fileInit;

            System.out.printf("Users loaded from file %s.\n", filePath);

            return;
        }

        // Not empty file.
        try {
            User[] users = new Gson().fromJson(DBUsersInterface.fileContent, User[].class);
        
            // Add users to Users (RAM).
            for (User user : users) {
                Users.addUser(user);
            }

            // File content is no longer needed.
            // To save memory.
            fileContent = fileReaded;

            System.out.printf("Users loaded from file %s.\n", filePath);
        } catch (JsonSyntaxException ex) {
            // TODO: Error handling.
        } catch (Exception ex) {
            // TODO: Error handling.
        }

    }

    // ALL THE FOLLOWING METHODS ARE DONE TO AVOID REWRITING ALL THE FILE EVERY TIME A USER IS ADDED OR UPDATED.
    // IT COMPLICATES THE CODE BUT IT IMPROVES PERFORMANCE IN CASE OF LARGE FILES PREVENTING INTENSIVE I/O OPERATIONS.
    /**
     * Remove the last line from the file attached.
     * This is an utility function to append a new user to the file without rewriting all the file.
     * Private because it's not intended to be used outside this class.
     * @throws RuntimeException If the file is not attached or the file content is not loaded.
     */
    private static void removeLastLine() throws RuntimeException {
        
        if (file == null) {
            throw new RuntimeException("File not attached. Set file before with setFile().");
        }

        if (DBUsersInterface.fileContent == null || DBUsersInterface.fileContent.isEmpty()) {
            throw new RuntimeException("File content is needed to remove last line.");
        }

        // To open the file and truncate it.
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"))  {

            long length = raf.length();

            long pointer = length - 1;
            while (pointer >= 0) {
                raf.seek(pointer);
                int readByte = raf.readByte();
                if (readByte == '\n' && pointer != length - 1) {
                    // Found the end of the last line.
                    break;
                }
                pointer--;
            }

            // Truncate the file at this point.
            raf.setLength(pointer + 1);

        } catch (IOException ex) {
            // TODO: Error handling.
        } catch (Exception ex) {
            // TODO: Error handling.
        }

    }    
    /**
     * Edits a specific line in a file, overwriting it with spaces.
     * The spaces must be the same length of the existing line.
     * This method will be used to replace the line (containing an user to updated) with spaces and the updated user will be written at the end of the file.
     * Private because it's not intended to be used outside this class.
     * @param lineNumber The line number to edit (1-based index).
     * @throws RuntimeException If the file is not attached.
     * @throws IllegalArgumentException If the line number is less than 1 or null or if the line number is out of bounds.
     */
    private static void editLine(Long lineNumber) throws RuntimeException, IllegalArgumentException {

        if (file == null) {
            throw new RuntimeException("File not attached. Set file before with setFile().");
        }

        if (lineNumber == null || lineNumber < 1) {
            throw new IllegalArgumentException("Line number must be greater than or equal to 1.");
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {

            Long currentLine = 1L;
            String line;

            // Move to the start of the file.
            raf.seek(0);

            // Find the position of the line to edit.
            Long position = 0L;
            while ((line = raf.readLine()) != null) {
                if (currentLine == lineNumber) {
                    break;
                }
                position = raf.getFilePointer();
                currentLine++;
            }
            line.endsWith("");

            // Check if the line to edit was found.
            if (currentLine == lineNumber) {
                // Move the pointer to the start of the line.
                raf.seek(position);

                // Write spaces to overwrite the line.
                while (true) {
                    // Check EOF.
                    if (raf.getFilePointer() >= raf.length()) {
                        break;
                    }
                    // Check EOL.
                    char c = (char) raf.readByte();
                    if (c == '\n') {
                        break;
                    }else {
                        raf.seek(raf.getFilePointer() - 1);
                    }
                    raf.writeByte(' ');                    
                }

                DBUsersInterface.emptyFileLines++;

            } else {
                throw new IllegalArgumentException("Line number is out of bounds.");
            }
        }catch (IOException ex) {
            // TODO: Error handling.
        } catch (Exception ex) {
            // TODO: Error handling.
        }
    }
    /**
     * Remove the last char from the file attached.
     * This is an utility function to append a new user to the file without rewriting all the file.
     * Private because it's not intended to be used outside this class.
     * @return The previous char before the removed one. Used to check if the updated user is the last user in the file, in this case the ',' is not needed.
     * @throws RuntimeException If the file is not attached or the file content is not loaded.
     */
    private static char removeLastChar() throws RuntimeException {
        
        if (file == null) {
            throw new RuntimeException("File not attached. Set file before with setFile().");
        }

        if (DBUsersInterface.fileContent == null || DBUsersInterface.fileContent.isEmpty()) {
            throw new RuntimeException("File content is needed to remove last file char.");
        }
        
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {

            // Remove last char.
            long length = raf.length();
            if (length > 0) {
                raf.setLength(length - 1);
            }

            // Returning the previous char before the removed one.
            length = raf.length();
            raf.seek(length - 1);
            char c = (char) raf.readByte();
            return c;
        } catch (IOException ex) {
            // TODO: Error handling.
            return ' ';
        } catch (Exception ex) {
            // TODO: Error handling, remove the return.
            return ' ';
        }
    }
    /**
     * Calculate the number of lines in the file attached.
     * This is used to calculate the new file line id to assign to a new user before adding it.
     * @return The number of lines in the file attached as Long, without reading the file again every time.
     */
    public static Long calculateFileLines() {
        // +1 for the first line with "[".
        return 1 + Users.getUsersSize() + emptyFileLines;
    }
    
    /**
     * Write a user on the file attached.
     * This appends the user to the file without rewriting all the file.
     * @param user The user to write.
     * @throws RuntimeException If the file is not attached or the file content is not loaded or the update user method is not found.
     * @throws NullPointerException If the user is null.
     */
    public static void writeUserOnFile(User user) throws RuntimeException, NullPointerException {

        if (user == null) {
            throw new NullPointerException("User cannot be null.");
        }

        if (fileContent == null) {
            throw new RuntimeException("File content is needed to write on file. Load users before with loadUsers().");
        }

        // Remove last line.
        DBUsersInterface.removeLastLine();

        // Write user on file.
        // Buffered to improve performance.
        try {
            BufferedOutputStream fileOutBuffered = new BufferedOutputStream(fileOut);

            String jsonUser = new Gson().toJson(user);

            // Remove fileLineId from JSON.
            JsonObject jsonObject = JsonParser.parseString(jsonUser).getAsJsonObject();
            jsonObject.remove("fileLineId");
            jsonUser = new Gson().toJson(jsonObject);

            jsonUser = String.join("", jsonUser.trim().split("\n")).replaceAll(" ", "");
            if (fileContent.equals(fileInit)) {
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
                char c = DBUsersInterface.removeLastChar();
                jsonUser = "," + "\n" + jsonUser + "\n]";

                // Check if this operation is part of an update user and if the user is the last in the file.
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                String method = null;
                try {
                    method = DBUsersInterface.class.getMethod("updateUserOnFile", User.class, User.class).getName();
                }catch (NoSuchMethodException ex) {
                    throw new RuntimeException("Method updateUserOnFile not found.");
                }
                for (StackTraceElement stackTraceElement : stackTrace) {
                    if (stackTraceElement.getMethodName().equals(method)) {
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
        } catch (IOException ex) {
            // TODO: Error handling.
        } catch (Exception ex) {
            // TODO: Error handling.
        }
    }
    /**
     * Update a user on the file attached.
     * This overwrites the old user line on the file with spaces and appends the new user to the end of file.
     * The old user position in the file is found by the file line id.
     * This is done to avoid rewriting all the file.
     * WARNING: It creates fragmentation in the file, but that's the best I can do since the files are not meant to be databases.
     * @param oldUser The old user to update.
     * @param newUser The new user to write (update).
     * @throws RuntimeException If the file is not attached or the file content is not loaded.
     * @throws NullPointerException If the old user or the new user are null.
     * @throws IllegalArgumentException If the old user file line id is null.
     */
    public static void updateUserOnFile(User oldUser, User newUser) throws RuntimeException, NullPointerException, IllegalArgumentException, InvalidUser {

        if (oldUser == null) {
            throw new NullPointerException("Old user cannot be null.");
        }
        if (newUser == null) {
            throw new NullPointerException("New user cannot be null.");
        }

        if (fileContent == null) {
            throw new RuntimeException("File content is needed to write on file. Load users before with loadUsers().");
        }

        if (oldUser.getFileLineId() == null) {
            throw new IllegalArgumentException("oldUser file line id cannot be null.");
        }
        if (newUser.getFileLineId() != null) {
            throw new IllegalArgumentException("New user file line id must be null.");
        }

        // Removing the old user line from the file overwriting it with spaces.
        DBUsersInterface.editLine(oldUser.getFileLineId());

        // Write the new user.
        Users.addUser(newUser);
        
    }
    
}
