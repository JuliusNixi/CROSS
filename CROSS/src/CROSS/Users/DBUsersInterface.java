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
import com.google.gson.JsonSyntaxException;

/**
 * This class is an interface to handle users database file.
 * Abstract class because i assume that i don't want to handle different users databases at the same time.
 * @version 1.0
 * @see User
 */
public abstract class DBUsersInterface {
    
    private static String filePath = null;

    private static File file = null;
    private static FileInputStream fileIn = null;
    private static FileOutputStream fileOut = null;

    private static String fileContent = null;

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
        } catch (FileNotFoundException e) {
            System.err.printf("DB Users file %s not found.\n", filePath);
        } catch (Exception e) {
            // TODO: Error handling.
        }

    }   

    /**
     * Get the file path.
     * @return The file path.
     */
    public static String getFilePath() {
        return filePath;
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
        } catch (IOException e) {
            System.err.printf("Error reading file %s.\n", filePath);
        } catch (Exception e) {
            // TODO: Error handling.
        }
    }

    /**
     * Load users from file to Users (in RAM).
     * Before using this method, the file must be readed with readFile().
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
            } catch (Exception e) {
                System.err.printf("Error initializing file %s.\n", filePath);
            }
            System.out.printf("Empty file %s. Initailized it.\n", filePath);
            return;
        }

        // IMPORTANT: The file could be already initialized before from the program, but no users has been added.
        if (DBUsersInterface.fileContent.equals(fileInit)) {
            fileContent = fileInit;
            return;
        }

        // Not empty file.
        try {
            User[] users = new Gson().fromJson(DBUsersInterface.fileContent, User[].class);
            // TODO: Continua qui. Salvare numero utente per calcolarsi la riga sul file al quale corrisponde.
            
            // Add users to Users (RAM).
            for (User user : users) {
                Users.addUser(user);
            }

            // File content is no longer needed.
            // To save memory.
            fileContent = fileReaded;
        } catch (JsonSyntaxException e) {
            System.err.printf("Error parsing JSON from file %s.\n", filePath);
        } catch (Exception e) {
            // TODO: Error handling.
        }

    }

    /**
     * Remove the last line from the file attached.
     * This is an utility function to append a new user without rewriting all the file.
     * Private because it's not intended to be used outside this class.
     */
    private static void removeLastLine() {
        
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
        } catch (IOException e) {
            System.err.printf("Error removing last line from file %s.\n", filePath);
        } catch (Exception e) {
            // TODO: Error handling.
        }

    }
    /**
     * Write a user on the file attached.
     * This appends the user to the file without rewriting all the file.
     * @param user The user to write.
     * @throws RuntimeException If the file content is null.
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
        removeLastLine();

        // Write user on file.
        // Buffered to improve performance.
        try {
            BufferedOutputStream fileOutBuffered = new BufferedOutputStream(fileOut);

            String jsonUser = new Gson().toJson(user);
            jsonUser = String.join("", jsonUser.trim().split("\n")).replaceAll(" ", "");
            if (fileContent.equals(fileInit)) {
                /*
                 * [
                 * 
                 */
                jsonUser = jsonUser + "\n";
                jsonUser = jsonUser + "]";
            } else {
                /* 
                 * [
                 *  {user1}
                 * 
                 */
                jsonUser = "," + "\n" + jsonUser + "\n]";
            }

            // Append to the file.
            fileOutBuffered.write(jsonUser.getBytes());
            fileOutBuffered.close();

            fileOut = new FileOutputStream(file, true);
        } catch (IOException e) {
            System.err.printf("Error writing user on file %s.\n", filePath);
        } catch (Exception e) {
            // TODO: Error handling.
        }
    }

    // TODO: Da rivedere.
    public static void removeUserOnFile(User user) throws IOException {

        if (fileContent == null) {
            throw new IOException("File content is needed to write on file. Load users before.");
        }

        // TODO: Continua qui.
        // Remove last line.
        removeLastLine();

        // Write user on file.
        // Buffered to improve performance.
        try {
            BufferedOutputStream fileOutBuffered = new BufferedOutputStream(fileOut);

            String jsonUser = new Gson().toJson(user);
            jsonUser = String.join("", jsonUser.trim().split("\n")).replaceAll(" ", "");
            if (fileContent.equals(fileInit)) {
                /*
                 * [
                 * 
                 */
                jsonUser = jsonUser + "\n";
                jsonUser = jsonUser + "]";
            } else {
                /* 
                 * [
                 *  {user1}
                 * 
                 */
                jsonUser = "," + "\n" + jsonUser + "\n]";
            }

            // Append to the file.
            fileOutBuffered.write(jsonUser.getBytes());
            fileOutBuffered.close();

            fileOut = new FileOutputStream(file, true);
        } catch (IOException e) {
            System.err.printf("Error writing user on file %s.\n", filePath);
        } catch (Exception e) {
            System.err.printf("Error writing user on file %s.\n", filePath);
        }

    }

}
