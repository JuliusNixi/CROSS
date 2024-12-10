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

// Abstract class. I assume that i don't want to handle different users dabatases.
public abstract class DBUsersInterface {
    
    private static String filePath = null;

    private static File file = null;
    private static FileInputStream fileIn = null;
    private static FileOutputStream fileOut = null;

    private static String fileContent = null;

    // Costants for file handling checks.
    private static final String fileReaded = "loaded";
    private static final String fileInit = "[\n]\n";

    public static void setFile(String filePath) throws IOException {
        if (!filePath.endsWith(".json")) {
            throw new IOException("File must be a JSON file.");
        }

        try {
            file = new File(filePath);

            fileIn = new FileInputStream(file);
            fileOut = new FileOutputStream(file, true);

            DBUsersInterface.filePath = filePath;

            System.out.printf("DB Users file %s attached.\n", filePath);
        } catch (FileNotFoundException e) {
            System.err.printf("DB Users file %s not found.\n", filePath);
        }catch (Exception e) {
            // TODO: Error
        }

    }   

    public static String getFilePath() {
        return filePath;
    }

    public static void readFile() throws IOException {
        if (fileIn == null) {
            throw new IOException("File not attached. Set file before.");
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
        } catch (Exception e) {
            System.err.printf("Error reading file %s.\n", filePath);
        }
    }

    public static void loadUsers() throws IOException {
        // Populate Users.


        if (DBUsersInterface.fileContent == null) {
            throw new IOException("File not read. Read it before.");
        }

        if (Users.getUsersSize() > 0) {
            throw new IOException("Users already loaded.");
        }

        // Empty file.
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
            for (User user : users) {
                Users.addUser(user);
            }
            // File content is no longer needed.
            // To save memory.
            fileContent = fileReaded;
        } catch (JsonSyntaxException e) {
            System.err.printf("Error parsing JSON from file %s.\n", filePath);
        } catch (Exception e) {
            System.err.printf("Error loading users from file %s.\n", filePath);
        }

    }

    // To remove last line from db users file.
    // This to append a new user without rewriting all the file.
    // Private because it's not intended to be used outside this class.
    private static void removeLastLine() throws IOException {
        
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
        } catch (Exception e) {
            System.err.printf("Error removing last line from file %s.\n", filePath);
        }

    }

    public static void writeUserOnFile(User user) throws RuntimeException {

        if (fileContent == null) {
            throw new RuntimeException("File content is needed to write on file. Load users before.");
        }

        // Remove last line.
        try {
            removeLastLine();
        } catch (IOException e) {
            System.err.printf("Error removing last line from file %s.\n", filePath);
        }

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
        } catch (Exception e) {
            System.err.printf("Error: %s\n", e.getMessage());
            System.err.printf("Error writing user on file %s.\n", filePath);
        }
    }

}

