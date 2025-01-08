import CROSS.Tests.Tests;

public class Main {

    public static void main(String[] args) {

        try {
            Tests.AllTests();
        }catch (InterruptedException ex){
            // This should never happens.
            System.err.println("Interrupted exception in main.");
            ex.printStackTrace();
            System.exit(-1);
        }

    }

}
