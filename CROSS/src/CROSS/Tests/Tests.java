package CROSS.Tests;

import java.util.Arrays;
import CROSS.Client.Client;
import CROSS.Client.ClientCLIThread;
import CROSS.Exceptions.InvalidUser;
import CROSS.Orders.LimitOrder;
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

// TODO: Generate Javadoc.
/**
 * This class is abstract, because it only contains some static methods.
 * These methods are used to perform some simple tests on the code.
 * @version 1.0
 * @see Server
 * @see AcceptThread
 * @see Client
 * @see ClientCLIThread
 * @see GenericPrice
 * @see SpecificPrice
 * @see CROSS.OrderBook.Market
 * @see PriceType
 * @see Currency
 * @see Separator
 * @see Quantity
 * @see LimitOrder
 * @see User
 * @see DBUsersInterface
 * @see Users
 * @see UniqueNumber
 */
public abstract class Tests {
    
    // SERVER AND CLIENT TESTS
    /**
     * To test the server.
     */
    public static void TestServer() {

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
    public static void TestClient() {

        // Test client.
        System.out.println("Testing client...");

        String pathToConfigPropertiesFile = "./Configs/client-config.properties";

        Client client = new Client(pathToConfigPropertiesFile);

        try {
            client.sendJSONToServer("asd");
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked sending json without client connection.");
        }

        @SuppressWarnings("unused")
        ClientCLIThread c;
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

    // TYPES TESTS
    /**
     * To test the types.
     */
    public static void TestPrices() {

        // Test prices.
        System.out.println("Testing prices...");

        GenericPrice genericPrice;
        try {
            genericPrice = new GenericPrice(-1);
        }catch (IllegalArgumentException ex) {
            System.out.println("Test passed, invalid GenericPrice.");
        }
        genericPrice = new GenericPrice(100);
        System.out.printf("Here's a GenericPrice: %s.\n", genericPrice.toString());

        // Market not tested here, but in a different dedicated method.
        CROSS.OrderBook.Market m = new CROSS.OrderBook.Market(Currency.USD, Currency.BTC, null, null, new GenericPrice(1));

        // Test toString(), getType(), getMarket(), toStringShort().
        SpecificPrice specificPrice = new SpecificPrice(genericPrice.getValue(), PriceType.ASK, m);
        System.out.printf("Here's a SpecificPrice: %s.\n", specificPrice.toString());
        System.out.printf("Here's a SpecificPrice (short): %s.\n", specificPrice.toStringShort());
        
        // Test compareTo().
        SpecificPrice prices[] = new SpecificPrice[2];
        prices[0] = specificPrice;
        prices[1] = new SpecificPrice(prices[0].getValue() + 1, prices[0].getType(), m);
        System.out.printf("Unsorted prices: %s.\n", Arrays.toString(prices));
        Arrays.sort(prices);
        System.out.printf("SORTED (reversed/descending) prices: %s.\n", Arrays.toString(prices));

    }
    /**
     * To test the quantity.
     */
    public static void TestQuantity() {

        // Test quantity.
        System.out.println("Testing Quantity...");

        Quantity quantity;

        try {
            quantity = new Quantity(-1);
        }catch (IllegalArgumentException ex) {
            System.out.println("Test passed, invalid Quantity.");
        }
        quantity = new Quantity(10);

        System.out.printf("Here's a quantity: %s.\n", quantity.toString());

    }

    // ORDERS TESTS
    /**
     * To test the limit order.
     */
    public static void TestLimitOrder() {

        // Test Limit order.
        System.out.println("Testing limit order...");
    
        LimitOrder order;
    
        // Prepare some necessary objects.
        GenericPrice increment = new GenericPrice(1);
        CROSS.OrderBook.Market BTCUSD = new CROSS.OrderBook.Market(Currency.BTC, Currency.USD, null, null, increment);
        SpecificPrice askCurrent = new SpecificPrice(99, PriceType.ASK, BTCUSD);
        SpecificPrice bidCurrent = new SpecificPrice(101, PriceType.BID, BTCUSD);
        BTCUSD.setActualPriceAsk(askCurrent);
        BTCUSD.setActualPriceBid(bidCurrent);
        Quantity quantity = new Quantity(10);
        User user = new User("testuser", "testpassword");

        // Price ok.
        SpecificPrice aValidAsk = new SpecificPrice(askCurrent.getValue() - 1, PriceType.ASK, BTCUSD);

        // Wrong price.
        SpecificPrice wrongAsk = new SpecificPrice(askCurrent.getValue() + 1, PriceType.ASK, BTCUSD);

        // Another market.
        CROSS.OrderBook.Market anotherMarket = new CROSS.OrderBook.Market(Currency.EUR, Currency.ETH, null, null, increment);

        try {
            // Exception expected.
            // Wrong market for the price market.
            order = new LimitOrder(anotherMarket, aValidAsk, quantity, user);
        }catch (IllegalArgumentException ex) {
            System.out.println("Test passed, blocked wrong market for the price market.");
        }

        try {
            // Exception expected.
            // Buy Limit price higher than the market ask price.
            order = new LimitOrder(BTCUSD, wrongAsk, quantity, user);
        }catch (IllegalArgumentException ex) {
            System.out.println("Test passed, blocked buy limit at price higher than the market ask price.");
        }

        // Order ok.
        order = new LimitOrder(BTCUSD, aValidAsk, quantity, user);

        System.out.println("Here's a valid Buy Limit order: " + order.toString());
        System.out.println("Here's a valid Buy Limit order (short): " + order.toStringShort());

    }

    // ORDER BOOK TESTS
    /**
     * To test the market.
     */
    public static void TestMarket() {

        // Test market.
        System.out.println("Testing Market...");

        GenericPrice increment = new GenericPrice(1);
        CROSS.OrderBook.Market BTCUSD = new CROSS.OrderBook.Market(Currency.BTC, Currency.USD, null, null, increment);
        SpecificPrice askCurrent = new SpecificPrice(99, PriceType.ASK, BTCUSD);

        // Test setActualPriceAsk(), setActualPriceBid().
        try {
            // Exception expected.
            // Cannot set bid price as ask price.
            BTCUSD.setActualPriceBid(askCurrent);
        }catch (IllegalArgumentException ex) {
            System.out.println("Test passed, blocked setting bid price as ask price.");
        }

        BTCUSD.setActualPriceAsk(askCurrent);

        // Test toString() and various getters.
        System.out.println("Here's a market: " + BTCUSD.toString());

    }

    // USERS TESTS
    /**
     * To test the user.
     */
    public static void TestUser() {
        
        // Test user.
        System.out.println("Testing user...");

        try {
            new User("a", "password");
        }catch (IllegalArgumentException ex) {
            System.out.println("Test passed, blocked invalid user.");
        }

        User strangeUser = new User(" I  m\t\nUser ", "strangeUserPassword");
        System.out.println("Here's a strange user (sanitize check): " + strangeUser.toString());

    }
    /**
     * To test users class and the users' database interface.
     */
    public static void TestUsersAndDB() {

        // Test Users and users' DB.
        System.out.println("Testing Users and users' DB...");

        try {
            DBUsersInterface.readFile();
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked reading non-existing file.");
        }
        try {
            DBUsersInterface.loadUsers();
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked loading users from not-readed file.");
        }

        String pathToUsersFile = "./DB/users.json";

        try {
            DBUsersInterface.setFile(pathToUsersFile + ".txt");
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked setting non-json file.");
        }

        DBUsersInterface.setFile(pathToUsersFile);

        try {
            DBUsersInterface.setFile(pathToUsersFile);
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked setting file multiple times.");
        }

        try {
            DBUsersInterface.loadUsers();
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked loading users before reading file.");
        }

        DBUsersInterface.readFile();

        try {
            DBUsersInterface.readFile();
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked reading file multiple times.");
        }

        DBUsersInterface.loadUsers();

        try {
            DBUsersInterface.loadUsers();
        }catch (RuntimeException ex) {
            System.out.println("Test passed, blocked loading users multiple times.");
        }

        System.out.println("Users DB loaded. Showing users...");

        System.out.printf("Users count: %d.\nHere's the users:\n", Users.getUsersSize());
        System.out.printf("%s", Users.toStringUsers());

        System.out.println("Adding new user...");
        UniqueNumber uniqueNumber = new UniqueNumber();
        String randomUsername = String.format("testuser%d", uniqueNumber.getNumber());
        String randomPassword = String.format("testpassword%d", uniqueNumber.getNumber());
        User newUser = new User(randomUsername, randomPassword);
        try {
            Users.addUser(newUser);
        }catch (InvalidUser ex) {
        }
        System.out.println("New user added.");
        try {
            Users.addUser(newUser);
        }catch (InvalidUser ex) {
            System.out.println("Test passed, blocked adding user with existing username.");
        }

        System.out.println("Showing new users DB...");
        System.out.printf("Users count: %d.\nHere's the users:\n", Users.getUsersSize());
        System.out.printf("%s", Users.toStringUsers());

        System.out.println("Searching the previous added user...");
        String password = Users.getUserPassword(randomUsername);
        System.out.printf("Here's the user found (without line id): %s.\n", new User(randomUsername, password).toString());

        System.out.println("Searching a non existent user...");
        password = Users.getUserPassword("nonexistentuser");
        password = password == null ? "null" : password;
        System.out.printf("Here's the non existent user found: %s.\n", password);

        System.out.println("Updating the previous added user...");
        User newUserUpdated = new User("newusernameupdated", "newpasswordupdated");
        User invalidUser = new User("nonexistentuser", "nonexistentpassword");
        try {
            Users.updateUser(invalidUser, newUserUpdated);
        }catch (InvalidUser ex) {
            System.out.println("Test passed, blocked updating non-existent user.");
        }
        try {
            Users.updateUser(newUser, newUserUpdated);
        }catch (InvalidUser ex) {
        }

        System.out.println("Showing new users DB...");
        System.out.printf("Users count: %d.\nHere's the users:\n", Users.getUsersSize());
        System.out.printf("%s", Users.toStringUsers());

    }

    public static void AllTests() throws InterruptedException {

        Separator separator = new Separator("-");

        System.out.println(separator);

        TestServer();
        // USED ONLY TO SEE THE DEBUG PRINTS IN THE CORRECT ORDER!
        Thread.sleep(1000 * 1);
        System.out.println(separator);

        TestClient();
        // USED ONLY TO SEE THE DEBUG PRINTS IN THE CORRECT ORDER!
        Thread.sleep(1000 * 1);
        // To go on a new line after user input prompt.
        System.out.println("");
        System.out.println(separator);

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

        /* 

        TestOrderBook();
        System.out.println(separator);

        OrderBook ob = TestExecutingLimit();
        for (LimitOrder o : ob.getLimitOrders()) {
            System.out.println(o.toString() + "\n");
        }
        System.out.println(separator);

        // Test Order Book with Sell Limit Orders.
        quantity = new Quantity(10);
        order = new LimitOrder(BTCUSD, actualPriceBid, Direction.SELL, quantity, user.getUsername());
        for (int i = order.getPrice().getValue(); i <= 110; i++) {
            price = new SpecificPrice(i, PriceType.BID);
            order = new LimitOrder(BTCUSD, price, Direction.SELL, quantity, user.getUsername());
            quantity = new Quantity(quantity.getQuantity() + 1);
            BTCUSDorderBook.executeOrder(order);
        }
        System.out.println(BTCUSDorderBook);

        */

        Thread.sleep(1000 * 5);
        System.exit(0);

    }

}
