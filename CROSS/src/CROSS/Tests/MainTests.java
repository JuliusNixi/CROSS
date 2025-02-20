package CROSS.Tests;

/**
 * 
 * This class is the main one to execute some tests.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Tests
 * 
 */
public class MainTests {

    /**
     * 
     * Main method to execute some tests.
     * 
     * @param args Unused array of strings representing the program's arguments.
     * 
     */
    public static void main(String[] args) {

        try {

            // Execute all the tests.
            System.out.println("Executing all tests...");

            Tests.AllTests();

            System.out.println("All tests passed.");

            System.exit(0);
            
        } catch (InterruptedException ex){
            
            // This should never happens, needed for the Thread.sleep() in the tests. 
            System.err.println("Interrupted exception in main executing tests.");
            ex.printStackTrace();

            System.exit(-1);

        }

    }

}
