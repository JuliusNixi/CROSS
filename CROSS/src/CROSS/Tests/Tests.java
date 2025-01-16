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
 * @version 1.0
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
     * To test the server.
     */
    public static void TestServer() throws NullPointerException, InvalidConfig, FileNotFoundException, IOException, Exception{

        // Test server.
        System.out.println("Testing server...");

        String pathToConfigPropertiesFile = "./Configs/server-config.properties";
        Server server = new Server(pathToConfigPropertiesFile);

        try {
            server.startAccept();
        }catch (RuntimeException ex) {
            System.out.println("Test passed, start the server first.");
        }

        server.startServer();

        try {
            server.startServer();
        }catch (RuntimeException ex) {
            System.out.println("Test passed, starting server multiple times.");
        }

        @SuppressWarnings("unused")
        AcceptThread s = server.startAccept();

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

        @SuppressWarnings("unused")
        ClientCLIThread c = null;
        try {
            c = Client.CLI(client);
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked starting CLI before client connection.");
        }

        client.connectClient();

        try {
            client.connectClient();
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked multiple client connections.");
        }

        c = Client.CLI(client);

        System.out.printf("Here's the client: %s.\n", client.toString());
        
    }

    /**
     * To perform all the tests.
     */
    public static void AllTests() throws InterruptedException {

        Separator separator = new Separator("-");

        System.out.println(separator);

        try {

            TestServer();
            // USED ONLY TO SEE THE DEBUG PRINTS IN THE CORRECT ORDER!
            Thread.sleep(1000 * 1);
            System.out.println(separator);

            TestClient();
            // USED ONLY TO SEE THE DEBUG PRINTS IN THE CORRECT ORDER!
            Thread.sleep(1000 * 1);
            // To go on a new line after user input prompt.
            System.out.println(separator);

            // To test a little bit the server and the client.
            Thread.sleep(1000 * 120);

        }catch (Exception ex) {
            // This should never happen.
            System.err.println("Tests failed with exception: " + ex.getMessage());
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
