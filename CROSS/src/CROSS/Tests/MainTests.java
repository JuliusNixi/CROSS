package CROSS.Tests;

/**
 * 
 * This class is the main one used to execute a few basic tests.
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
     */
    public static void main() {

        try {

            // Execute all the tests.
            System.out.println("Executing all tests...");

            Tests.AllTests();

            System.out.println("All tests passed.");

            System.exit(0);
            
        } catch (InterruptedException ex){
            
            // This should never happens, needed for the Thread.sleep() in the tests. 
            System.err.println("Interrupted exception in the main method, while executing tests.");
            ex.printStackTrace();

            System.exit(-1);

        }

    }

}
