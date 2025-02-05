import CROSS.Tests.Tests;

public class Main {

    public static void main(String[] args) {

        try {

            Tests.AllTests();

            System.exit(0);
            
        }catch (InterruptedException ex){
            
            // This should never happens, needed for the Thread.sleep() in the tests. 
            System.err.println("Interrupted exception in main executing tests.");
            ex.printStackTrace();

            System.exit(-1);

        }

    }

}
