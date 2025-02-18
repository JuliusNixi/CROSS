package CROSS.Tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import CROSS.Client.Client;
import CROSS.Client.ClientCLIThread;
import CROSS.Exceptions.InvalidConfig;
import CROSS.Exceptions.InvalidUser;
import CROSS.Orders.LimitOrder;
import CROSS.Orders.MarketOrder;
import CROSS.Server.AcceptThread;
import CROSS.Server.Server;
import CROSS.Types.Currency;
import CROSS.Types.Quantity;
import CROSS.Types.Price.GenericPrice;
import CROSS.Types.Price.PriceType;
import CROSS.Types.Price.SpecificPrice;
import CROSS.Users.DBUsersInterface;
import CROSS.Users.User;
import CROSS.Users.Users;
import CROSS.Utils.Separator;
import CROSS.Utils.UniqueNumber;

/**
 * 
 * This class is abstract, because it only contains some static methods.
 * These methods are used to perform some simple tests on the code hoping to avoid bugs.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Server
 * @see AcceptThread
 * 
 * @see Client
 * @see ClientCLIThread
 * 
 * @see GenericPrice
 * @see SpecificPrice
 * @see PriceType
 * @see Quantity
 * @see Currency
 * 
 * @see InvalidConfig
 * @see InvalidUser
 * 
 * @see CROSS.OrderBook.Market
 * @see CROSS.OrderBook.OrderBook
 * 
 * @see LimitOrder
 * @see MarketOrder
 * @see StopOrderOrder
 * 
 * @see User
 * @see DBUsersInterface
 * @see Users
 * 
 * @see Separator
 * @see UniqueNumber
 * 
 */
public abstract class Tests {
    
    // SERVER AND CLIENT TESTS
    /**
     * 
     * To test the server.
     * 
     * @throws NullPointerException If the path to the server's config file is null.
     * @throws InvalidConfig If the server's config file is invalid.
     * @throws FileNotFoundException If the server's config file is not found.
     * @throws IOException If there's an I/O error.
     * @throws IllegalArgumentException If there is an error reading the server's config file, a malformed Unicode escape appears in the input.
     * @throws Exception If there's an unknown exception.
     * 
     */
    public static void TestServer() throws NullPointerException, InvalidConfig, FileNotFoundException, IOException, IllegalArgumentException, Exception {

        // Test server.
        System.out.println("Testing server...");

        String pathToConfigPropertiesFile = "./Configs/server-config.properties";
        // The exceptions thrown by the constructor are backwarded to the caller.
        Server server = new Server(pathToConfigPropertiesFile);

        // TODO: Qui ero rimasto prima di passare al server....
        try {
            server.startAccept();
        }catch (RuntimeException ex) {
            System.out.println("Test passed, start the server first.");
        }

        server.startServer();

        try {
            server.startServer();
        }catch (RuntimeException ex) {

            // Capturing the specific exception that should be thrown (I expect this one).
            // If something else is thrown, it's a problem and it's backwarded to the caller.

            System.out.println("Test passed, starting server multiple times.");
            
        }

        server.startAccept();

        try {
            server.startAccept();
        }catch (RuntimeException ex) {
            System.out.println("Test passed, start accepting multiple times.");
        }

        System.out.printf("Here's the server: %s.\n", server.toString());
        
    }
    /**
     * To test the client.
    */
    public static void TestClient() throws NullPointerException, FileNotFoundException, IllegalArgumentException, InvalidConfig, IOException, Exception {

        // Test client.
        System.out.println("Testing client...");

        String pathToConfigPropertiesFile = "./Configs/client-config.properties";
        Client client = new Client(pathToConfigPropertiesFile);

        try {
            client.sendJSONToServer("asd");
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked sending json without client connection.");
        }

        try {
            client.disconnectClient();
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked disconnecting without client connection.");
        }

        try {
            Client.CLI(client);
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked starting CLI before client connection.");
        }

        client.connectClient();

        try {
            client.connectClient();
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked multiple client connections.");
        }

        Client.CLI(client);

        System.out.printf("Here's the client: %s.\n", client.toString());

        client.responsesStart();
        
        
    }

    /**
     * To perform all the tests.
     * Main method.
     * 
     * @throws InterruptedException If the thread is interrupted, needed for the Thread.sleep() in the tests. Should never happen.
     */
    public static void AllTests() throws InterruptedException {

        Separator separator = new Separator("-");

        System.out.println(separator);

        try {

            TestServer();
            // USED ONLY TO SEE THE DEBUG PRINTS IN THE CORRECT ORDER!
            Thread.sleep(1000 * 1);
            System.out.println(separator);

            // TODO: Qui ero rimasto prima di passare al server (sopra/tests)....

            TestClient();
            // USED ONLY TO SEE THE DEBUG PRINTS IN THE CORRECT ORDER!
            Thread.sleep(1000 * 1);
            // To go on a new line after user input prompt.
            System.out.println(separator);

            // To test a little bit the server and the client.
            Thread.sleep(1000 * 120);

        }catch (InterruptedException ex) {

            // This should never happens, needed for the Thread.sleep() in the tests. 

            // Only for the InterruptedException in the Thread.sleep() in the tests.

            // Backwarding the exception to the caller.
            throw new InterruptedException(ex.getMessage());

        }catch (Exception ex) {

            // Exceptions thrown by the tests (my methods, it's a problem).

            // This should never happen, otherwise the tests failed.
            System.err.println("Tests failed with exception: " + ex.getMessage());
            ex.printStackTrace();

            System.exit(-1);

        }


        /* 

        TestPrices();
        System.out.println(separator);

        TestQuantity();
        System.out.println(separator);

        TestLimitOrder();
        System.out.println(separator);

        TestMarket();
        System.out.println(separator);

        TestUser();
        System.out.println(separator);

        TestUsersAndDB();
        System.out.println(separator);


        Thread.sleep(1000 * 5);

        */
    
    
    }

}

