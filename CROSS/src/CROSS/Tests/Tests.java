package CROSS.Tests;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import CROSS.Exceptions.InvalidOrder;
import CROSS.Exceptions.InvalidUser;
import CROSS.OrderBook.Market;
import CROSS.Orders.DBOrdersInterface;
import CROSS.Orders.LimitOrder;
import CROSS.Orders.MarketOrder;
import CROSS.Orders.Order;
import CROSS.Orders.Orders;
import CROSS.Orders.StopMarketOrder;
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
 * These methods are used to perform some simple tests on the code hoping to discover bugs.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see Separator
 * 
 * @see User
 * 
 * @see DBUsersInterface
 * @see Users
 * @see UniqueNumber
 * @see InvalidUser
 * 
 * @see Quantity
 * @see GenericPrice
 * @see SpecificPrice
 * 
 * @see Currency
 * @see Market
 * @see Order
 * @see LimitOrder
 * @see PriceType
 * @see StopMarketOrder
 * @see MarketOrder
 * @see Orders
 * @see DBOOrdersInterface
 * @see InvalidOrder
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
    /*
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
        */
    /**
     * To test the client.
    */
    /*
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
        */

    // USER TESTS
    public static void TestUser() throws NullPointerException, IllegalArgumentException {
        
        // Test user.
        System.out.println("Testing user...");

        String[] badUsernames = {
            null,
            "",
            "s",
            "thisusernameiswaytoolonggggggggggggggggggggggggggg",
            "THISISUPPERCASE",
            "",
        };
        String[] badPasswords = {
            null,
            "",
            "s",
            "thispasswordiswaytoolonggggggggggggggggggggggggggg",
        };
        String validUser = "testuser";
        String validPassword = "testpassword";

        // User creation.
        User user = null;
        for (String badUser : badUsernames) {
            try {
                user = new User(badUser, validPassword);
            }catch (Exception ex) {
                System.out.printf("Test passed, blocked invalid username: %s.\n", badUser);
            }
        }
        for (String badPassword : badPasswords) {
            try {
                user = new User(validUser, badPassword);
            }catch (Exception ex) {
                System.out.printf("Test passed, blocked invalid password: %s.\n", badPassword);
            }
        }

        user = new User(validUser, validPassword);
        System.out.printf("Here's the user: %s.\n", user.toString());

        // File line id.
        user.setFileLineId(42L);
        System.out.printf("Here's the user with the file line id: %s.\n", user.toString());

        // Comparing users.
        User user2 = new User("a" + validUser, validPassword);
        LinkedList<User> users = new LinkedList<User>();
        users.add(user);
        users.add(user2);
        System.out.printf("Here's the first user before sorting: %s.\n", users.get(0).toString());
        System.out.printf("Here's the second user before sorting: %s.\n", users.get(1).toString());
        users.sort(null);
        System.out.printf("Here's the first user after sorting: %s.\n", users.get(0).toString());
        System.out.printf("Here's the second user after sorting: %s.\n", users.get(1).toString());

        // Comparing users with the same file line id.
        user2.setFileLineId(42L);
        try {
            users = new LinkedList<User>();
            users.add(user);
            users.add(user2);
            users.sort(null);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked multiple users with the same file line id.");
        }

    }

    // USERS TESTS
    public static void TestUsers() throws IOException, RuntimeException, NullPointerException, IllegalArgumentException, Exception, InvalidUser {

        // Test users.
        System.out.println("Testing users...");

        // Read file content before setting the file.
        try {
            DBUsersInterface.readFile();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked reading users without setting the file.");
        }

        // Loading users before setting the file.
        try {
            DBUsersInterface.loadUsers();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked loading users without setting the file.");
        }

        // Writing user before setting the file.
        try {
            DBUsersInterface.writeUserOnFile(new User("testuser42", "testpassword"));
        }catch (Exception ex) {
            System.out.println("Test passed, blocked writing user without setting the file.");
        }

        // Updating user before setting the file.
        try {
            DBUsersInterface.updateUserOnFile(new User("testuser42", "testpassword"), new User("testuser", "testpassword"));
        }catch (Exception ex) {
            System.out.println("Test passed, blocked updating user without setting the file.");
        }

        // Non existent file.
        // String filePathNonExistent = "./DB/users2.json";
        // DBUsersInterface.setFile(filePathNonExistent);

        // Existing file.
        String filePath = "./DB/users.json";
        DBUsersInterface.setFile(filePath);

        // Re-setting the file.
        try {
            // DBUsersInterface.setFile(filePathNonExistent);
            DBUsersInterface.setFile(filePath);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-setting the file.");
        }

        // Reading the file.
        DBUsersInterface.readFile();

        // Re-reading the file.
        try {
            DBUsersInterface.readFile();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-reading the file.");
        }




        // Now all the others methods to test are in the Users class.




        // Loading users.
        Users.loadUsers();
        System.out.printf("Here are the users loaded from the file: \n%s", Users.toStringUsers());

        // Re-loading users.
        try {
            Users.loadUsers();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-loading users.");
        }

        // Adding user.
        UniqueNumber uniqueNumber = new UniqueNumber();
        User userToAdd = new User("test" + uniqueNumber.toString(), "testpassword");
        Users.addUser(userToAdd);

        // Re-adding same user.
        try {
            Users.addUser(userToAdd);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-adding user.");
        }

        // Updating non existent user.
        User newUser = new User("testusernewlast", "testpassword");
        try {
            Users.updateUser(new User("testuser42", "testpassword"), newUser);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked updating non existent user.");
        }

        // Updating LAST user.
        uniqueNumber = new UniqueNumber();
        userToAdd = new User("test" + uniqueNumber.toString(), "testpassword");
        Users.addUser(userToAdd);
        System.out.printf("Here are the users before updating the last user: \n%s", Users.toStringUsers());
        Users.updateUser(userToAdd, newUser);
        System.out.printf("Here are the users after updating the last user: \n%s", Users.toStringUsers());

        // Updating FIRST user AND user getting.
        newUser = new User("testusernewfirst", "testpassword");
        String fixedFirstUserUsername = "exampleuser";
        User fixedOldUser = Users.getUser(fixedFirstUserUsername);
        Users.updateUser(fixedOldUser, newUser);

    }

    // TYPES TESTS
    public static void TestTypes() throws NullPointerException, IllegalArgumentException {

        // Test types.
        System.out.println("Testing types...");

        // Quantity.
        Quantity quantity = new Quantity(0);
        System.out.printf("Here's the quantity: %s.\n", quantity.toString());

        // Invalid quantity.
        try {
            quantity = new Quantity(-42);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked invalid quantity.");
        }

        // Generic price.
        GenericPrice genericPrice = new GenericPrice(42);
        System.out.printf("Here's the generic price: %s.\n", genericPrice.toString());

        // Invalid generic price.
        try {
            genericPrice = new GenericPrice(0);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked invalid generic price.");
        }

        // Specific price.
        Currency primaryCurrency = Currency.BTC;
        Currency secondaryCurrency = Currency.USD;
        GenericPrice increment = new GenericPrice(1);
        Market market = new Market(primaryCurrency, secondaryCurrency, null, null, increment);
        PriceType type = PriceType.ASK;
        SpecificPrice specificPrice = new SpecificPrice(42, type, market);
        System.out.printf("Here's the specific price: %s.\n", specificPrice.toString());
        System.out.printf("Here's the specific price SHORT format: %s.\n", specificPrice.toStringShort());
        System.out.printf("Here's the specific price WITHOUT MARKET format: %s.\n", specificPrice.toStringWithoutMarket());

    }

    // ORDERS TESTS
    public static void TestOrders() throws IOException, RuntimeException, NullPointerException, IllegalArgumentException, Exception, InvalidOrder {

        // Test orders.
        System.out.println("Testing orders...");

        // Some needed objects.
        // Market.
        Currency primaryCurrency = Currency.BTC;
        Currency secondaryCurrency = Currency.USD;
        GenericPrice increment = new GenericPrice(1);
        Market market = new Market(primaryCurrency, secondaryCurrency, null, null, increment);
        // Price.
        SpecificPrice askPrice = new SpecificPrice(42, PriceType.ASK, market);
        SpecificPrice bidPrice = new SpecificPrice(askPrice.getValue() + 1, PriceType.BID, market);
        // Quantity.
        Quantity quantity = new Quantity(42);
        // User.
        User user = new User("testuser", "testpassword");
        // Another currency.
        Currency anotherCurrency = Currency.EUR;
        // Another market.
        Market anotherMarket = new Market(primaryCurrency, anotherCurrency, null, null, increment);

        // Testing limit order AND order, since this last one is abstract.
        LimitOrder limitOrder = null;

        // Creating order without best market prices.
        try {
            limitOrder = new LimitOrder(askPrice, quantity, user);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked order creation without best market prices.");
        }

        market.setActualPrices(askPrice, bidPrice);

        // Invalid limit order price.
        SpecificPrice badAskPrice = new SpecificPrice(bidPrice.getValue() + 1, PriceType.ASK, market);
        try {
            limitOrder = new LimitOrder(badAskPrice, quantity, user);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked invalid limit order price.");
        }

        // Valid limit order creation.
        limitOrder = new LimitOrder(askPrice, quantity, user);
        System.out.printf("Here's the limit order: %s.\n", limitOrder.toString());

        // Setters.
        limitOrder.setQuantity(new Quantity(24));
        limitOrder.setPrice(new SpecificPrice(askPrice.getValue() - 1, PriceType.ASK, market));
        UniqueNumber uniqueNumber = new UniqueNumber();
        Integer u = uniqueNumber.getNumber().intValue();
        if (u < 0) {
            u = -u;
        }
        limitOrder.setId(u);
        System.out.printf("Here's the limit order updated: %s.\n", limitOrder.toString());

        System.out.printf("Here's the limit order in the SHORT form: %s.\n", limitOrder.toStringShort());

        // Comparing orders.
        LimitOrder limitOrder2 = new LimitOrder(askPrice, quantity, user);
        LinkedList<LimitOrder> orders = new LinkedList<LimitOrder>();
        // limitOrder2 will have a greater id.
        orders.add(limitOrder2);
        orders.add(limitOrder);
        System.out.printf("Here's the first order before sorting: %s.\n", orders.get(0).toString());
        System.out.printf("Here's the second order before sorting: %s.\n", orders.get(1).toString());
        orders.sort(null);
        System.out.printf("Here's the first order after sorting: %s.\n", orders.get(0).toString());
        System.out.printf("Here's the second order after sorting: %s.\n", orders.get(1).toString());
        // Invalid orders comparison.
        try {
            limitOrder2 = new LimitOrder(new SpecificPrice(24, PriceType.ASK, anotherMarket), quantity, user);
            orders = new LinkedList<LimitOrder>();
            orders.add(limitOrder2);
            orders.add(limitOrder);
            orders.sort(null);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked comparison of orders from different markets.");
        }

        // StopMarketOrder is tested in the same way as LimitOrder.

        // MarketOrder tests.
        MarketOrder marketOrder = new MarketOrder(market, PriceType.ASK, quantity, user);
        System.out.printf("Here's the market order: %s.\n", marketOrder.toString());




        // Testing the Orders and DBOrdersInterface classes.




        Market.setMainMarket(market);

        // Read file content before setting the file.
        try {
            DBOrdersInterface.readFile();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked reading orders without setting the file.");
        }

        // Loading orders before setting the file.
        try {
            DBOrdersInterface.loadOrders();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked loading orders without setting the file.");
        }

        // Writing order before setting the file.
        try {
            DBOrdersInterface.writeOrderOnFile(limitOrder);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked writing order without setting the file.");
        }

        // Non existent file.
        // String filePathNonExistent = "./DB/users2.json";
        // DBOrdersInterface.setFile(filePathNonExistent);

        // Existing file.
        String filePath = "./DB/testorders.json";
        DBOrdersInterface.setFile(filePath);

        // Re-setting the file.
        try {
            // DBOrdersInterface.setFile(filePathNonExistent);
            DBOrdersInterface.setFile(filePath);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-setting the file.");
        }

        // Reading the file.
        DBOrdersInterface.readFile();

        // Re-reading the file.
        try {
            DBOrdersInterface.readFile();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-reading the file.");
        }




        // Now all the others methods to test are in the Orders class.




        // Loading orders.
        Orders.loadOrders();
        System.out.printf("Here are the orders loaded from the file: \n%s", Orders.toStringOrders());

        // Re-loading orders.
        try {
            Orders.loadOrders();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-loading orders.");
        }

        // Adding order.
        limitOrder.setTimestamp(System.currentTimeMillis());
        Orders.addOrder(limitOrder);

        // Re-adding same order.
        try {
            Orders.addOrder(limitOrder);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-adding order.");
        }

    }

    // UTILS TESTS
    public static void TestUtils() throws RuntimeException, InterruptedException {
        
        // Test utils.
        System.out.println("Testing utils...");

        // Separator.
        // Invalid length.
        Separator separator = null;
        try {
            separator = new Separator("-", 0);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked invalid separator length.");
        }
        // Valid separator.
        separator = new Separator("-", 8);
        System.out.printf("Here's the separator: %s.\n", separator);

        // Unique number, multiple threads.
        ExecutorService executor = Executors.newFixedThreadPool(8);
        LinkedList<UniqueNumber> uniqueNumbers = new LinkedList<UniqueNumber>();
        for (int i = 0; i < 8 * 8 * 8; i++) {
            executor.execute(() -> {
         
                UniqueNumber uniqueNumber = new UniqueNumber();
                synchronized (uniqueNumbers) {
                    uniqueNumbers.add(uniqueNumber);
                }

            });
        }
        executor.shutdown();
        while (executor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS) == false);
        while (true) {
            UniqueNumber uniqueNumber = null;
            if (uniqueNumbers.size() == 0) {
                break;
            }
            uniqueNumber = uniqueNumbers.pop();
            if (uniqueNumbers.contains(uniqueNumber)) {
                throw new RuntimeException("Unique number is not unique.");
            }
        }
        System.out.println("Test passed, all unique numbers are unique.");

        // FileHandler Class tested in the users tests.
        
    }

    /**
     * 
     * To perform all the tests.
     * 
     * Main method.
     * 
     * @throws InterruptedException If the thread is interrupted, needed for the Thread.sleep() in the tests. Should never happen.
     * 
     */
    public static void AllTests() throws InterruptedException {

        Separator separator = new Separator("-");

        System.out.println(separator);

        try {

            // TestUser();
            // System.out.println(separator);

            // THIS MODIFIES THE USERS.JSON FILE, SO IT'S COMMENTED OUT.
            // TestUsers();
            // System.out.println(separator);

            // TestTypes();
            // System.out.println(separator);

            TestOrders();
            System.out.println(separator);

            // TestUtils();
            // System.out.println(separator);

            // TestServer();
            // USED ONLY TO SEE THE DEBUG PRINTS IN THE CORRECT ORDER!
            // Thread.sleep(1000 * 1);
            // System.out.println(separator);

            // TestClient();
            // USED ONLY TO SEE THE DEBUG PRINTS IN THE CORRECT ORDER!
            // Thread.sleep(1000 * 1);
            // To go on a new line after user input prompt.
            // System.out.println(separator);

            // To test a little bit the server and the client.
            Thread.sleep(1000 * 120 / 120);

        }catch (InterruptedException ex) {

            // This should never happens, needed for the Thread.sleep() in the above tests. 

            // Only for the InterruptedException in the Thread.sleep() in the above tests.

            // Backwarding the exception to the caller.
            throw new InterruptedException(ex.getMessage());

        }catch (Exception ex) {

            // Exceptions thrown by the tests (my methods, it's a problem, means some bug is present).

            // This should never happen, otherwise the tests are failed.
            System.err.println("Tests failed with exception: " + ex.getMessage());
            ex.printStackTrace();

            System.exit(-1);

        }
    
    
    }

}

