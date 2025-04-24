package CROSS.Tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.JsonSyntaxException;

import CROSS.Exceptions.InvalidOrder;
import CROSS.Exceptions.InvalidUser;
import CROSS.OrderBook.Market;
import CROSS.OrderBook.OrderBook;
import CROSS.OrderBook.OrderBookLine;
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
 * @see DBOrdersInterface
 * @see InvalidOrder
 * 
 * @see OrderBookLine
 * @see OrderBook
 * 
 * 
 */
public abstract class Tests {

    private static Integer testPassed = 0;
    private static Integer testToPass = 0;
    
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

    // UTILS TESTS
    public static void TestUtils() throws RuntimeException, InterruptedException {
        
        // Test utils.
        System.out.println("Testing utils...");

        // Separator.
        // Invalid length.
        Separator separator = null;
        try {
            testToPass++;
            separator = new Separator("-", 0);
        } catch (Exception ex) {
            System.out.println("Test passed, blocked invalid separator length.");
            testPassed++;
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

    // USER TESTS
    public static void TestUser() throws NullPointerException, IllegalArgumentException {
        
        // Test user.
        System.out.println("Testing user...");

        String[] badUsernames = {
            null,
            "",
            "s", // Too short.
            "thisusernameiswaytoolonggggggggggggggggggggggggggg",
            "THISISUPPERCASE",
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
                testToPass++;
                user = new User(badUser, validPassword);
            }catch (Exception ex) {
                System.out.printf("Test passed, blocked invalid username user creation: %s.\n", badUser);
                testPassed++;
            }
        }
        for (String badPassword : badPasswords) {
            try {
                testToPass++;
                user = new User(validUser, badPassword);
            }catch (Exception ex) {
                System.out.printf("Test passed, blocked invalid password user creation: %s.\n", badPassword);
                testPassed++;
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
            testToPass++;
            users = new LinkedList<User>();
            users.add(user);
            users.add(user2);
            users.sort(null);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked sorting multiple users with the same file line id.");
            testPassed++;
        }

    }

    // USERS TESTS
    public static void TestUsers() throws IOException, RuntimeException, NullPointerException, IllegalArgumentException, Exception, InvalidUser {

        // Test users.
        System.out.println("Testing users...");




        // DBUsersInterface tests.




        // Read file content before setting the file.
        try {
            testToPass++;
            DBUsersInterface.readFile();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked reading users without setting the file.");
            testPassed++;
        }

        // Loading users before setting the file.
        try {
            testToPass++;
            DBUsersInterface.loadUsers();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked loading users without setting the file.");
            testPassed++;
        }

        // Writing user before setting the file.
        try {
            testToPass++;
            DBUsersInterface.writeUserOnFile(new User("testuser42", "testpassword"));
        }catch (Exception ex) {
            System.out.println("Test passed, blocked writing user without setting the file.");
            testPassed++;
        }

        // Updating user before setting the file.
        try {
            testToPass++;
            DBUsersInterface.updateUserOnFile(new User("testuser42", "testpassword"), new User("testuser", "testpassword"));
        }catch (Exception ex) {
            System.out.println("Test passed, blocked updating user without setting the file.");
            testPassed++;
        }
        
        // Add user before setting the file.
        UniqueNumber uniqueNumber = new UniqueNumber();
        User userToAdd = new User("test" + uniqueNumber.toString(), "testpassword");
        try {
            testToPass++;
            Users.addUser(userToAdd);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked adding user before setting the file.");
            testPassed++;
        }

        // Update user before setting the file.
        User newUser = new User(userToAdd.getUsername(), "updatedpassword");
        try {
            testToPass++;
            Users.updateUser(userToAdd, newUser);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked updating user before setting the file.");
            testPassed++;
        }

        // Load users before setting the file.
        try {
            testToPass++;
            Users.loadUsers();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked loading users before setting the file.");
            testPassed++;
        }

        // Non existent file.
        // String filePathNonExistent = "./DB/users2.json";
        // DBUsersInterface.setFile(filePathNonExistent);

        // Existing file.
        String filePath = "./DB/users.json";
        DBUsersInterface.setFile(filePath);

        // Re-setting the file.
        try {
            testToPass++;
            DBUsersInterface.setFile(filePath);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-setting the file.");
            testPassed++;
        }

        // Reading the file.
        DBUsersInterface.readFile();

        // Re-reading the file.
        try {
            testToPass++;
            DBUsersInterface.readFile();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-reading the file.");
            testPassed++;
        }




        // Now all the others methods to test are ALSO USED in the Users class, so testing them through the Users class.




        // Loading users.
        Users.loadUsers();
        System.out.printf("Here are the users loaded from the file: \n%s", Users.toStringUsers());

        // Re-loading users.
        try {
            testToPass++;
            Users.loadUsers();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-loading users.");
            testPassed++;
        }

        // Adding user.
        Users.addUser(userToAdd);

        // Re-adding same user.
        try {
            testToPass++;
            Users.addUser(userToAdd);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-adding user.");
            testPassed++;
        }

        // Updating non existent user.
        try {
            testToPass++;
            Users.updateUser(new User("testusernonexist", "testpassword"), newUser);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked updating non existent user.");
            testPassed++;
        }

        // Updating LAST user.
        // Also testing toStringUsers method.
        uniqueNumber = new UniqueNumber();
        userToAdd = new User("t" + uniqueNumber.toString(), "testpassword");
        Users.addUser(userToAdd);
        newUser = new User(userToAdd.getUsername(), "updatedpassword");
        System.out.printf("Here are the users before updating the last user: \n%s", Users.toStringUsers());
        Users.updateUser(userToAdd, newUser);
        System.out.printf("Here are the users after updating the last user: \n%s", Users.toStringUsers());

        // Updating FIRST user AND user getting.
        newUser = new User("testusernewfirstexample", "testupdatedpassword");
        // Fixed because must be the first user in the file template.
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
            testToPass++;
            quantity = new Quantity(-42);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked invalid quantity.");
            testPassed++;
        }

        // Generic price.
        GenericPrice genericPrice = new GenericPrice(42);
        System.out.printf("Here's the generic price: %s.\n", genericPrice.toString());

        // Invalid generic price.
        try {
            testToPass++;
            genericPrice = new GenericPrice(0);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked invalid generic price.");
            testPassed++;
        }

        // Specific price.
        Currency primaryCurrency = Currency.BTC;
        Currency secondaryCurrency = Currency.USD;
        GenericPrice increment = new GenericPrice(1);
        Market market = new Market(primaryCurrency, secondaryCurrency, increment);
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
        GenericPrice genericAskPrice = new GenericPrice(43);
        GenericPrice genericBidPrice = new GenericPrice(42);
        Market market = new Market(primaryCurrency, secondaryCurrency, increment);
        // Price.
        SpecificPrice askPrice = new SpecificPrice(genericAskPrice.getValue(), PriceType.ASK, market);
        SpecificPrice bidPrice = new SpecificPrice(genericBidPrice.getValue(), PriceType.BID, market);
        market.setActualPrices(askPrice, bidPrice);
        // Quantity.
        Quantity quantity = new Quantity(42);
        // User.
        User user = new User("testuser", "testpassword");
        // Another currency.
        Currency anotherCurrency = Currency.EUR;
        // Another market.
        Market anotherMarket = new Market(primaryCurrency, anotherCurrency, increment);
        // Testing limit order AND order, since this last one is abstract.
        LimitOrder limitOrder = null;

        // Invalid limit order price.
        SpecificPrice badAskPrice = new SpecificPrice(bidPrice.getValue() - 1, PriceType.ASK, market);
        try {
            testToPass++;
            limitOrder = new LimitOrder(badAskPrice, quantity, user);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked invalid limit order price.");
            testPassed++;
        }

        // Valid limit order creation.
        limitOrder = new LimitOrder(askPrice, quantity, user);
        System.out.printf("Here's the limit order: %s.\n", limitOrder.toString());

        // Setters.
        limitOrder.setQuantity(new Quantity(24));
        limitOrder.setPrice(new SpecificPrice(askPrice.getValue() - 1, PriceType.ASK, market));
        System.out.printf("Here's the limit order updated: %s.\n", limitOrder.toString());

        System.out.printf("Here's the limit order in the SHORT form: %s.\n", limitOrder.toStringShort());

        // Comparing orders.
        LimitOrder limitOrder2 = new LimitOrder(askPrice, quantity, user);
        LinkedList<LimitOrder> orders = new LinkedList<LimitOrder>();
        // limitOrder2 will have a greater id since has been created after limitOrder.
        orders.add(limitOrder2);
        orders.add(limitOrder);
        System.out.printf("Here's the first order before sorting: %s.\n", orders.get(0).toString());
        System.out.printf("Here's the second order before sorting: %s.\n", orders.get(1).toString());
        orders.sort(null);
        System.out.printf("Here's the first order after sorting: %s.\n", orders.get(0).toString());
        System.out.printf("Here's the second order after sorting: %s.\n", orders.get(1).toString());
        // Invalid orders comparison, from different markets.
        try {
            testToPass++;
            limitOrder2 = new LimitOrder(new SpecificPrice(24, PriceType.ASK, anotherMarket), quantity, user);
            orders = new LinkedList<LimitOrder>();
            orders.add(limitOrder2);
            orders.add(limitOrder);
            orders.sort(null);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked comparison of orders from different markets.");
            testPassed++;
        }

        StopMarketOrder stopMarketOrder = null;
        // Invalid stop market order price.
        try {
            testToPass++;
            stopMarketOrder = new StopMarketOrder(new SpecificPrice(market.getActualPriceBid().getValue() + 1, PriceType.ASK, market), quantity, user);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked invalid stop market order price.");
            testPassed++;
        }

        // Valid stop market order creation.
        stopMarketOrder = new StopMarketOrder(new SpecificPrice(market.getActualPriceBid().getValue(), PriceType.ASK, market), quantity, user);
        System.out.printf("Here's the stop market order: %s.\n", stopMarketOrder.toString());

        // MarketOrder tests.
        MarketOrder marketOrder = new MarketOrder(market, PriceType.ASK, quantity, user);
        // Price features checked through the toString method.
        System.out.printf("Here's the market order: %s.\n", marketOrder.toString());




        // Testing the Orders and DBOrdersInterface classes.




        Market.setMainMarket(market);

        // Read file content before setting the file.
        try {
            testToPass++;
            DBOrdersInterface.readFile();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked reading orders without setting the file.");
            testPassed++;
        }

        // Loading orders before setting the file.
        try {
            testToPass++;
            DBOrdersInterface.loadOrders(false, false);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked loading orders without setting the file.");
            testPassed++;
        }

        // Writing order before setting the file.
        try {
            testToPass++;
            DBOrdersInterface.writeOrderOnFile(limitOrder);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked writing order without setting the file.");
            testPassed++;
        }

        // Add order before setting the file.
        try {
            testToPass++;
            Orders.addOrder(limitOrder, false);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked adding order before setting the file.");
            testPassed++;
        }

        // Load orders before setting the file.
        try {
            testToPass++;
            Orders.loadOrders(false, false);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked loading orders before setting the file.");
            testPassed++;
        }

        // Non existent file.
        // String filePathNonExistent = "./DB/users2.json";
        // DBOrdersInterface.setFile(filePathNonExistent);

        // Existing file.
        String filePath = "./DB/orders.json";
        DBOrdersInterface.setFile(filePath);

        // Re-setting the file.
        try {
            testToPass++;
            DBOrdersInterface.setFile(filePath);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-setting the file.");
            testPassed++;
        }

        // Reading the file.
        DBOrdersInterface.readFile();

        // Re-reading the file.
        try {
            testToPass++;
            DBOrdersInterface.readFile();
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-reading the file.");
            testPassed++;
        }




        // Now all the others methods to test are ALSO USED in the Orders class, so testing them through the Orders class.




        // Loading orders.
        Orders.loadOrders(false, false);
        System.out.printf("Here are the orders loaded from the file: \n%s", Orders.toStringOrders());

        // Re-loading orders.
        try {
            testToPass++;
            Orders.loadOrders(false, false);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-loading orders.");
            testPassed++;
        }

        // Adding order.
        limitOrder.setTimestamp(System.currentTimeMillis());
        Orders.addOrder(limitOrder, false);

        // Re-adding same order.
        try {
            testToPass++;
            Orders.addOrder(limitOrder, false);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked re-adding same order.");
            testPassed++;
        }

        // Getting order.
        // ID Fixed from template file.
        Order order = Orders.getOrder(82908);
        System.out.printf("Here's the order with the ID 82908: %s.\n", order.toString());

    }

    // TEST ORDERS DEMO FILE
    public static void TestOrdersDemoFile() throws IllegalArgumentException, NullPointerException, RuntimeException, IOException, JsonSyntaxException, Exception {

        // Test demo orders file.
        System.out.println("Testing demo orders file...");

        // Market.
        Currency primaryCurrency = Currency.BTC;
        Currency secondaryCurrency = Currency.USD;
        GenericPrice increment = new GenericPrice(1);
        GenericPrice genericAskPrice = new GenericPrice(43);
        GenericPrice genericBidPrice = new GenericPrice(42);
        Market market = new Market(primaryCurrency, secondaryCurrency, increment);
        // Price.
        SpecificPrice askPrice = new SpecificPrice(genericAskPrice.getValue(), PriceType.ASK, market);
        SpecificPrice bidPrice = new SpecificPrice(genericBidPrice.getValue(), PriceType.BID, market);
        market.setActualPrices(askPrice, bidPrice);

        Market.setMainMarket(market);

        DBOrdersInterface.setFile("./DB/storicoOrdini.json");
        DBOrdersInterface.readFile();
        Orders.loadOrders(true, true);

        System.out.printf("Here's the orders loaded from the DEMO file: \n%s", Orders.toStringOrders());

    }

    // MARKET TESTS
    public static void TestMarket() {
        
        // Test market.
        System.out.println("Testing market...");

        // Market.
        Currency primaryCurrency = Currency.BTC;
        Currency secondaryCurrency = Currency.USD;
        Currency anotherCurrency = Currency.EUR;
        GenericPrice increment = new GenericPrice(1);
        Market market = null;

        // Same currencies.
        try {
            testToPass++;
            market = new Market(primaryCurrency, primaryCurrency, increment);
        } catch (Exception ex) {
            System.out.println("Test passed, blocked invalid market with same primary and secondary currency.");
            testPassed++;
        }

        // Null main market.
        // Main market is not null because has been set in the TestOrders method.

        // Valid market.
        market = new Market(primaryCurrency, secondaryCurrency, increment);
        System.out.printf("Here's the market: %s.\n", market.toString());

        // Invalid actual prices.
        try {
            testToPass++;
            SpecificPrice askPrice = new SpecificPrice(43, PriceType.ASK, market);
            Market anotherMarket = new Market(primaryCurrency, anotherCurrency, increment);
            SpecificPrice invalidBidPrice = new SpecificPrice(42, PriceType.BID, anotherMarket);
            market.setActualPrices(askPrice, invalidBidPrice);
        } catch (Exception ex) {
            System.out.println("Test passed, blocked setting invalid actual prices.");
            testPassed++;
        }

        // Valid actual prices.
        SpecificPrice askPrice = new SpecificPrice(43, PriceType.ASK, market);
        SpecificPrice bidPrice = new SpecificPrice(42, PriceType.BID, market);
        market.setActualPrices(askPrice, bidPrice);
        System.out.printf("Here's the market with the actual prices: %s.\n", market.toString());

        // Invalid set main market
        try {
            testToPass++;
            Market.setMainMarket(null);
        } catch (Exception ex) {
            System.out.println("Test passed, blocked setting null main market.");
            testPassed++;
        }

        // Valid set and get main market.
        Market.setMainMarket(market);
        System.out.printf("Here's the main market: %s.\n", Market.getMainMarket().toString());

        // Comparing different markets.
        try {
            testToPass++;
            Market anotherMarket = new Market(primaryCurrency, anotherCurrency, increment);
            market.compareTo(anotherMarket);
        } catch (Exception ex) {
            System.out.println("Test passed, blocked comparing different markets.");
            testPassed++;
        }

    }

    // ORDERBOOK LINE TESTS
    public static void TestOrderBookLine() {

        // Test order book line.
        System.out.println("Testing order book line...");

        // Market.
        Currency primaryCurrency = Currency.BTC;
        Currency secondaryCurrency = Currency.USD;
        GenericPrice increment = new GenericPrice(1);
        Market market = new Market(primaryCurrency, secondaryCurrency, increment);
        Market.setMainMarket(market);

        // Line price.
        SpecificPrice linePrice = new SpecificPrice(2, PriceType.ASK, Market.getMainMarket());

        // Initial order.
        Quantity quantity = new Quantity(1);
        User user = new User("testuser", "testpassword");
        LimitOrder initiaLimitOrder = new LimitOrder(linePrice, quantity, user);

        // Valid order book line.
        OrderBookLine<LimitOrder> limitOrderBookLine = new OrderBookLine<LimitOrder>(linePrice, initiaLimitOrder);
        System.out.printf("Here's the line order book line with orders list: %s.\n", limitOrderBookLine.toStringWithOrders());

        // Invalid line price / initial order.
        // Coherence checks.
        // Using a market order.
        try {
            testToPass++;
            MarketOrder initialMarketOrder = new MarketOrder(Market.getMainMarket(), PriceType.ASK, quantity, user);
            new OrderBookLine<MarketOrder>(linePrice, initialMarketOrder);
        } catch (Exception ex) {
            System.out.println("Test passed, blocked invalid line price / initial order, using a market order.");
            testPassed++;
        }
        // Different price values.
        try {
            testToPass++;
            SpecificPrice differentLinePrice = new SpecificPrice(43, PriceType.ASK, Market.getMainMarket());
            new OrderBookLine<LimitOrder>(differentLinePrice, initiaLimitOrder);
        } catch (Exception ex) {
            System.out.println("Test passed, blocked invalid line price / initial order, different price values.");
            testPassed++;
        }
        // Different price types.
        try {
            testToPass++;
            SpecificPrice differentLinePrice = new SpecificPrice(42, PriceType.BID, Market.getMainMarket());
            new OrderBookLine<LimitOrder>(differentLinePrice, initiaLimitOrder);
        } catch (Exception ex) {
            System.out.println("Test passed, blocked invalid line price / initial order, different price types.");
            testPassed++;
        }
        // Different markets.
        try {
            testToPass++;
            Market anotherMarket = new Market(primaryCurrency, Currency.EUR, increment);
            SpecificPrice differentLinePrice = new SpecificPrice(42, PriceType.ASK, anotherMarket);
            new OrderBookLine<LimitOrder>(differentLinePrice, initiaLimitOrder);
        } catch (Exception ex) {
            System.out.println("Test passed, blocked invalid line price / initial order, different markets.");
            testPassed++;
        }

        System.out.printf("Here's the line PRE-ADDING ordes (ONLY WITH the initial one) order book line: %s.\n", limitOrderBookLine.toString());

        // Add order.
        LimitOrder newLimitOrder = null;
        for (int i = 0; i < 9; i++) {
            Quantity newQuantity = new Quantity(1);
            User newUser = new User("testuser" + i, "testpassword" + i);
            newLimitOrder = new LimitOrder(linePrice, newQuantity, newUser);
            limitOrderBookLine.addOrder(newLimitOrder);
        }

        System.out.printf("Here's the line AFTER-ADDING ordes (ONLY WITH the initial one) order book line: %s.\n", limitOrderBookLine.toString());
        
        // To string with orders.
        System.out.printf("Here's the order book line with the orders list: %s\n", limitOrderBookLine.toStringWithOrders());

        // Cancel invalid (not present) order.
        try {
            testToPass++;
            limitOrderBookLine.cancelOrder(new LimitOrder(linePrice, new Quantity(1), new User("testuser", "testpassword")));
        } catch (Exception ex) {
            System.out.println("Test passed, blocked cancelling invalid (not present) order.");
            testPassed++;
        }
        // Cancel order.
        System.out.printf("Here's the order book line with the orders list before cancelling the last order: %s\n", limitOrderBookLine.toStringWithOrders());
        limitOrderBookLine.cancelOrder(newLimitOrder);
        System.out.printf("Here's the order book line with the orders list after cancelling the last order: %s\n", limitOrderBookLine.toStringWithOrders());

        // executeMarketOrderOnLimitLine and executeStopOrderFromStopLine TESTED IN THE ORDERBOOK TESTS.

    }

    // ORDERBOOK TESTS
    public static void TestOrderBook() {

        // Test order book.
        System.out.println("Testing order book...");

        // Market.
        Currency primaryCurrency = Currency.BTC;
        Currency secondaryCurrency = Currency.USD;
        GenericPrice increment = new GenericPrice(1);
        Market market = new Market(primaryCurrency, secondaryCurrency, increment);

        // Order book.
        OrderBook orderBook = new OrderBook(market);
        Market.setMainMarket(orderBook.getMarket());

        // Otherwise I mess up the market with the order book in the code below, using the market instead of the order book! Got crazy to find this stupid bug.
        market = null;

        // Different order market.
        try {
            testToPass++;
            LimitOrder limitOrder = new LimitOrder(new SpecificPrice(42, PriceType.ASK, new Market(primaryCurrency, Currency.EUR, increment)), new Quantity(1), new User("testuser", "testpassword"));
            orderBook.executeOrder(limitOrder);
        } catch (Exception ex) {
            System.out.println("Test passed, blocked executing limit order with a different market.");
            testPassed++;
        }

        // HERE THERE ARE A LOT OF TESTS, IMPORTANT PART!
        // FUZZING APPROACH.

        // Valid limit orders both ASK and BID.
        Integer startRandomSeed = 42;
        Random random = new Random(startRandomSeed);

        Integer startMiddlePrice = 150;
        Integer startBidAskSpread = 60;

        Integer minQuantity = 1;
        Integer maxQuantity = 10;

        Integer minUser = 1;
        Integer maxUser = 10;

        Integer userValue = null;
        User user = null;

        Quantity quantity = null;

        Boolean isAskOrBid = null;
        PriceType priceType = null;

        Integer priceValue = null;
        SpecificPrice price = null;

        Integer startMinPrice = 1;
        Integer stopMaxPrice = (startMiddlePrice - startBidAskSpread) + (startMiddlePrice + startBidAskSpread);
        // OrderBook initial prices structure:
        // 300 -> comes from the above stopMaxPrice.
        // ...
        // 210 -> comes from 150 (startMiddlePrice) + 60 (startBidAskSpread) = BEST ASK PRICE
        // ...
        // 150 -> comes from the above startMiddlePrice.
        // ...
        // 90 -> comes from 150 (startMiddlePrice) - 60 (startBidAskSpread) = BEST BID PRICE
        // ...
        // 1 -> comes from the above startMinPrice.
        Integer testsOrders = 50;

        // Remember each test execute n testOrders.
        // If testsOrders is 50, and differentTests (below) is 10, then 50 * 10 = 500 orders.
        // But some orders may fail.
        Integer differentTests = 10;
        Random randomSeeds[] = new Random[differentTests];
        for (int i = 1; i <= differentTests; i++) {
            randomSeeds[i - 1] = new Random(startRandomSeed + i);
        }

        // Initial ask and bid orders, to setup best ask and best bid prices and setup the order book as explained above.
        // Using always the same seed 42.
        SpecificPrice bestStartAskPrice = new SpecificPrice(startMiddlePrice + startBidAskSpread, PriceType.ASK, Market.getMainMarket());
        // Huge spread to test well.
        SpecificPrice bestStartBidPrice = new SpecificPrice(startMiddlePrice - startBidAskSpread, PriceType.BID, Market.getMainMarket());

        quantity = new Quantity(random.nextInt((maxQuantity - minQuantity) + 1) + minQuantity);

        userValue = random.nextInt((maxUser - minUser) + 1) + minUser;
        user = new User("testuser" + userValue, "testpassword" + userValue);

        LimitOrder bestStartAskOrder = new LimitOrder(bestStartAskPrice, quantity, user);
        orderBook.executeOrder(bestStartAskOrder);

        LimitOrder bestStartBidOrder = new LimitOrder(bestStartBidPrice, quantity, user);
        orderBook.executeOrder(bestStartBidOrder);

        System.out.printf("Here's the starter order book: %s\n", orderBook.toString());

        // Other orders.
        for (int t = 0; t < differentTests; t++) {

            random = randomSeeds[t];
            System.out.printf("\n\n\n\n TEST: %d - SEED: %d\n\n\n\n\n", t, startRandomSeed + t + 1);

            for (int i = 0; i < testsOrders; i++) {

                // Quantity.
                quantity = new Quantity(random.nextInt((maxQuantity - minQuantity) + 1) + minQuantity);

                // User.
                userValue = random.nextInt((maxUser - minUser) + 1) + minUser;
                user = new User("testuser" + userValue, "testpassword" + userValue);

                // Ask or bid.
                isAskOrBid = random.nextBoolean();
                priceType = isAskOrBid ? PriceType.ASK : PriceType.BID;

                // Price.
                priceValue = random.nextInt((stopMaxPrice - startMinPrice) + 1) + startMinPrice;
                price = new SpecificPrice(priceValue, priceType, Market.getMainMarket());

                // Order creation.
                LimitOrder limitOrder = null;
                try {
                    limitOrder = new LimitOrder(price, quantity, user);
                } catch (IllegalArgumentException ex) {
                    System.out.printf("Test passed, blocked invalid limit order creation (price: %d, type: %s): %s\n", price.getValue(), price.getType().name(), ex.getMessage());
                    // Skipping execution below.
                    System.out.printf("\n\n");
                    continue;
                }

                // Order execution.
                System.out.printf("Here's the order that will be executed: %s.\n", limitOrder.toString());
                try {
                    orderBook.executeOrder(limitOrder);
                    System.out.printf("Here's the order book after the order: %s\n", orderBook.toString());
                }catch (IllegalArgumentException ex) {
                    System.out.printf("Test passed, blocked invalid limit order execution (price: %d, type: %s): %s\n", price.getValue(), price.getType().name(), ex.getMessage());
                }

                System.out.printf("\n\n");
                
            }

        }


    }

    // Utility function, it's not part of the tests.
    public static void copyFile(String src, String dst) throws IOException {
        try {
            Path sourcePath = Paths.get(src);
            Path destinationPath = Paths.get(dst);

            // Copy the file. 
            // If the destination file exists, replace it.
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.printf("File %s copied successfully in %s.\n", src, dst);
        } catch (IOException e) {
            String err = String.format("I/O error occurred while copying the file %s in %s.", src, dst);
            throw new IOException(err);
        }
    }

    /**
     * 
     * To perform all the tests.
     * 
     * If a test is failed the program exits with -1.
     * 
     * Main method.
     * 
     * @throws InterruptedException If the thread is interrupted, needed for the Thread.sleep() in the tests. Should never happen. Backwarded to the caller.
     * 
     */
    public static void AllTests() throws InterruptedException {

        System.out.println("DEBUG: MAKE BEFORE THE CLIENT TO USE THE ACTIONS UTILS.");

        Separator separator = new Separator("-");

        System.out.println(separator);

        try {

            TestUtils();
            System.out.println(separator);

            TestUser();
            System.out.println(separator);

            // THIS MODIFIES THE USERS.JSON FILE, SO PAY ATTENTION!
            copyFile("./DB/defaultUsers.json", "./DB/users.json");
            TestUsers();
            System.out.println(separator);

            TestTypes();
            System.out.println(separator);

            // THIS MODIFIES THE ORDERS.JSON FILE, SO PAY ATTENTION!
            // copyFile("./DB/defaultOrders.json", "./DB/orders.json");
            // TestOrders();
            // System.out.println(separator);

            // TO USE THIS TEST, COMMENT THE ABOVE ONE.
            TestOrdersDemoFile();
            System.out.println(separator);

            TestMarket();
            System.out.println(separator);

            TestOrderBookLine();
            System.out.println(separator);

            System.out.println("\n\n\n\n\n\n");

            TestOrderBook();
            System.out.println(separator);




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
            Thread.sleep(1000 * 2);

            System.out.printf("Tests passed: %d/%d.\n", testPassed, testToPass);
            if (testPassed == testToPass) {
                System.out.println("The tests counter is RIGHT.");
            }else {
                throw new Exception("The tests counter is wrong.");
            }

            // Completation of the tests given by the caller.

        }catch (InterruptedException ex) {

            // This should never happens, needed for the Thread.sleep() in the above tests. 

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

