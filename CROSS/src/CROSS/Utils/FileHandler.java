package CROSS.Utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import CROSS.Users.DBUsersInterface;
import java.io.File;

/**
 * 
 * This class provides some utility methods to handle files (the users database and the orders database).
 * 
 * It's basically an optimization to avoid rewriting all the files every time something needs to be appendend / updated.
 * 
 * Abstract because it's not intended to be instantiated, only for the static methods.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see DBUsersInterface
 * 
 * @see DBOrdersInterface
 * 
 */
public abstract class FileHandler {
    
    // ALL THE FOLLOWING METHODS ARE DONE TO AVOID REWRITING ALL THE FILES (USERS DATABASE AND ORDERS DATABASE) EVERY TIME SOMETHING NEEDS TO BE APPENDED / UPDATED.
    // IT COMPLICATES THE CODE BUT IT SHOULD IMPROVES PERFORMANCE IN CASE OF LARGE FILES PREVENTING INTENSIVE I/O OPERATIONS.
    // THE EASIER ALTERNATIVE WOULD BE TO REWRITE ALL THE UPDATED JSONS ON THE DISK EVERY TIME, BUT IT'S LESS PERFORMANT.
    // THE BEST SOLUTION WOULD BE TO USE A DATABASE, BUT IT'S OUT OF THE SCOPE OF THIS PROJECT.
    /**
     * 
     * Remove the last line from a file.
     * 
     * This is an utility function used to append new content to a file without rewriting all the file.
     * 
     * Synchronized because it's intended to be used in a multi-threaded environment.
     * 
     * @param file The file from which remove the last line.
     * 
     * @throws IOException If there's an I/O error.
     * @throws NullPointerException If the file is null.
     * 
     */
    public static synchronized void removeLastLine(File file) throws NullPointerException, IOException {
        
        // Null check.
        if (file == null) {
            throw new NullPointerException("File from which remove the last line cannot be null.");
        }

        // To open the file and truncate it.
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"))  {

            long length = raf.length();

            // Reversed read.
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

        } catch (IOException | IllegalArgumentException ex) {
            throw new IOException("Error removing the last line from the file.");
        }

    }    
    /**
     * 
     * Edits a specific line in a file, overwriting it with spaces.
     * 
     * The spaces must be the SAME LENGTH of the existing line content due to the file system limitations.
     * 
     * This method will be used to replace the line (containing an user to update) with spaces and then the updated user will be written at the end of the file.
     * 
     * Synchronized because it's intended to be used in a multi-threaded environment.
     * 
     * @param file The file to edit.
     * @param lineNumber The line number to edit (1-based index).
     * 
     * @throws NullPointerException If the file or the line number are null.
     * @throws IllegalArgumentException If the line number is less than 1 or null or if the line number is out of bounds of the file lines.
     * @throws IOException If there's an I/O error.
     * 
     */
    public static synchronized void editLine(File file, Long lineNumber) throws NullPointerException, IllegalArgumentException, IOException {

        // Null check.
        if (file == null) {
            throw new NullPointerException("File from which edit a line cannot be null.");
        }
        if (lineNumber == null) {
            throw new NullPointerException("Line number to edit in a file cannot be null.");
        }

        // Line number validation check.
        if (lineNumber < 1) {
            throw new IllegalArgumentException("Line number to edit must be greater than or equal to 1.");
        }

        Boolean found = true;
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {

            Long currentLine = 1L;

            // Move to the start of the file.
            raf.seek(0);

            // Find the position of the line to edit.
            Long position = 0L;
            while ((raf.readLine()) != null) {

                if (currentLine == lineNumber) {
                    break;
                }

                position = raf.getFilePointer();

                currentLine++;

            }

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

            } else {
                found = false;
            }

        }catch (IOException | IllegalArgumentException ex) {
            throw new IOException("Error editing the line in the file.");
        }

        if (!found) {
            throw new IllegalArgumentException("Line number is out of bounds of the file lines.");
        }

    }
    /**
     * 
     * Remove the last character from a file.
     * 
     * This is an utility function used to append new content to a file without rewriting all the file.
     * 
     * Synchronized because it's intended to be used in a multi-threaded environment.
     * 
     * @param file The file from which remove the last character.
     * 
     * @return The previous character before the removed one. Used to check if an updated user is the last user in the file, in this case the ',' is not needed.
     * 
     * @throws NullPointerException If the file is null.
     * @throws IOException If there's an I/O error removing the last character from the file.
     * 
     */
    public static synchronized char removeLastChar(File file) throws NullPointerException, IOException {
        
        // Null check.
        if (file == null) {
            throw new NullPointerException("File from which remove the last character cannot be null.");
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
            throw new IOException("Error removing the last character from the file.");
        }

    }
    
}
