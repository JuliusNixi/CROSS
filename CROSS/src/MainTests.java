import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import cross.exceptions.InvalidOrder;
import cross.exceptions.InvalidUser;
import cross.orderbook.OrderBook;
import cross.orderbook.OrderBookLine;
import cross.orders.LimitOrder;
import cross.orders.MarketOrder;
import cross.orders.Order;
import cross.orders.StopOrder;
import cross.orders.db.DBOrdersInterface;
import cross.orders.db.Orders;
import cross.types.Currency;
import cross.types.Quantity;
import cross.types.price.GenericPrice;
import cross.types.price.PriceType;
import cross.types.price.SpecificPrice;
import cross.users.User;
import cross.users.db.DBUsersInterface;
import cross.users.db.Users;
import cross.utils.Separator;
import cross.utils.UniqueNumber;
import java.util.concurrent.RejectedExecutionException;
import com.google.gson.JsonSyntaxException;

/**
 *
 * This class is used to execute a few basics tests to check the functionality of some features of the CROSS project.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 */
public class MainTests {

    // Utility function, it's not directly part of the tests.
    public static void copyFile(String src, String dst) throws IOException {
        try {
            Path sourcePath = Paths.get(src);
            Path destinationPath = Paths.get(dst);

            // Copy the file. 
            // If the destination file exists, replace it.
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.printf("File %s copied successfully in %s.\n", src, dst);
        } catch (InvalidPathException | UnsupportedOperationException | IOException ex) {
            String err = String.format("I/O error occurred while copying the file %s in %s.", src, dst);
            throw new IOException(err);
        }
    }

    // UTILS TESTS
    public static void TestUtils() throws NullPointerException, IllegalArgumentException, RejectedExecutionException, IllegalStateException, InterruptedException {
        
        // Test utils.
        System.out.println("Testing utils...");

        // Separator.
        // Invalid length.
        Separator separator;
        try {
            separator = new Separator("-", 0);
            separator.getSeparator();
        } catch (IllegalArgumentException ex) {
            System.out.println("Test passed, blocked invalid separator length.");
        }
        // Valid separator.
        separator = new Separator("-", 8);
        System.out.printf("Here's the separator: %s.\n", separator);

        // Unique number, multiple threads.
        ExecutorService executor = Executors.newFixedThreadPool(8);
        LinkedList<UniqueNumber> uniqueNumbers = new LinkedList<>();
        for (int i = 0; i < 8 * 8 * 8; i++) {
            executor.execute(() -> {
         
                UniqueNumber uniqueNumber = new UniqueNumber();
                synchronized (uniqueNumbers) {
                    uniqueNumbers.add(uniqueNumber);
                }

            });
        }
        executor.shutdown();
        while (executor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS) == false){}
        while (true) {
            UniqueNumber uniqueNumber;
            if (uniqueNumbers.isEmpty()) {
                break;
            }
            uniqueNumber = uniqueNumbers.pop();
            if (uniqueNumbers.contains(uniqueNumber)) {
                throw new IllegalStateException("Unique number is not unique.");
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
            "thisusernameiswaytoolonggggggggggggggggggggggggggg", // Too long.
            "THISISUPPERCASE", // Contains NOT ALLOWED uppercase letters.
        };
        String[] badPasswords = {
            null,
            "",
            "s", // Too short.
            "thispasswordiswaytoolonggggggggggggggggggggggggggg", // Too long.
        };
        String validUser = "testuser";
        String validPassword = "testpassword";

        // User creation.
        User user;
        for (String badUser : badUsernames) {
            try {
                user = new User(badUser, validPassword);
                user.getUsername();
            }catch (IllegalArgumentException | NullPointerException ex) {
                System.out.printf("Test passed, blocked invalid username user creation: %s.\n", badUser);
            }
        }
        for (String badPassword : badPasswords) {
            try {
                user = new User(validUser, badPassword);
                user.getPassword();
            }catch (IllegalArgumentException | NullPointerException ex) {
                System.out.printf("Test passed, blocked invalid password user creation: %s.\n", badPassword);
            }
        }

        // Valid user creation.
        user = new User(validUser, validPassword);
        System.out.printf("Here's the user: %s.\n", user.toString());

        // File line id.
        user.setFileLineId(42L);
        System.out.printf("Here's the user with the file line id: %s.\n", user.toString());

        // Comparing users.
        User user2 = new User("a" + validUser, validPassword);
        LinkedList<User> users = new LinkedList<>();
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
            users = new LinkedList<>();
            users.add(user);
            users.add(user2);
            users.sort(null);
        }catch (Exception ex) {
            System.out.println("Test passed, blocked sorting multiple users with the same file line id.");
        }

    }

    // USERS TESTS
    public static void TestUsers(String dbUsersFilePath) throws IOException, JsonSyntaxException, NoSuchMethodException, NullPointerException, IllegalArgumentException, RuntimeException, InvalidUser, IllegalStateException, IllegalAccessException {

        // Test users.
        System.out.println("Testing users...");




        // DBUsersInterface tests.




        // Read file content before setting the file.
        try {
            DBUsersInterface.readFile();
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked reading users FROM INTERFACE without setting the file.");
        }

        // Loading users before setting the file.
        try {
            DBUsersInterface.loadUsers();
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked loading users FROM INTERFACE without setting the file.");
        }

        // Writing user before setting the file.
        try {
            DBUsersInterface.writeUserOnFile(new User("testuser42", "testpassword"));
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked writing user FROM INTERFACE without setting the file.");
        }

        // Updating user before setting the file.
        try {
            DBUsersInterface.updateUserOnFile(new User("testuser42", "testpassword"), new User("testuser", "testpassword"));
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked updating user FROM INTERFACE without setting the file.");
        }
        

        // Need to do these tests of the Users class here, before proceeding with the DBUsersInterface tests.

        // Add user before setting the file.
        UniqueNumber uniqueNumber = new UniqueNumber();
        User userToAdd = new User("test" + uniqueNumber.toString(), "testpassword");
        try {
            Users.addUser(userToAdd);
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked adding user FROM USERS before setting the file.");
        }

        // Update user before setting the file.
        User newUser = new User(userToAdd.getUsername(), "updatedpassword");
        try {
            Users.updateCredentials(userToAdd, newUser);
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked updating user FROM USERS before setting the file.");
        }

        // Load users before setting the file.
        try {
            Users.loadUsers(null);
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked loading users FROM USERS before setting the file.");
        }

        // Non existent file (to create it).
        // String filePathNonExistent = "./DB/Users/users2.json";
        // String filePath = filePathNonExistent;
        // DBUsersInterface.setFile(filePath);

        // Existing file.
        String filePath = dbUsersFilePath;
        DBUsersInterface.setFile(filePath);

        // Re-setting the file.
        try {
            DBUsersInterface.setFile(filePath);
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked re-setting FROM INTERFACE the file.");
        }

        // Writing user before reading the file.
        try {
            DBUsersInterface.writeUserOnFile(new User("testuser42", "testpassword"));
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked writing user FROM INTERFACE before reading the file.");
        }

        // Updating user before reading the file.
        try {
            DBUsersInterface.updateUserOnFile(new User("testuser42", "testpassword"), new User("testuser", "testpassword"));
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked updating user FROM INTERFACE before reading the file.");
        }

        // Reading the file.
        DBUsersInterface.readFile();

        // Re-reading the file.
        try {
            DBUsersInterface.readFile();
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked re-reading FROM INTERFACE the file.");
        }




        // Now all the others methods to test are ALSO USED in the Users class, so testing them through the Users class.
        // Users tests.




        // Loading users.
        Users.loadUsers(null);
        System.out.printf("Here are the users loaded from the file: \n%s", Users.toStringUsers());

        // Re-loading users.
        try {
            Users.loadUsers(null);
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked re-loading users FROM USERS.");
        }

        // Adding user.
        Users.addUser(userToAdd);

        // Re-adding same user.
        try {
            Users.addUser(userToAdd);
        }catch (InvalidUser ex) {
            System.out.println("Test passed, blocked re-adding user FROM USERS.");
        }

        // Updating non existent user.
        try {
            Users.updateCredentials(new User("testusernonexist", "testpassword"), newUser);
        }catch (InvalidUser ex) {
            System.out.println("Test passed, blocked updating non existent user FROM USERS.");
        }

        // Updating LAST user.
        // Also testing toStringUsers method.
        uniqueNumber = new UniqueNumber();
        userToAdd = new User("t" + uniqueNumber.toString(), "testpassword");
        Users.addUser(userToAdd);
        newUser = new User(userToAdd.getUsername(), "updatedpassword".toUpperCase());
        System.out.printf("Here are the users before updating the last user: \n%s", Users.toStringUsers());
        Users.updateCredentials(userToAdd, newUser);
        System.out.printf("Here are the users after updating the last user: \n%s", Users.toStringUsers());

        // Updating FIRST user AND user getting.
        String fixedFirstUserUsername = "exampleuser";
        newUser = new User(fixedFirstUserUsername, "testupdatedpassword".toUpperCase());
        // Fixed because must be the first user in the file template.
        User fixedOldUser = Users.getUserByUsername(fixedFirstUserUsername);
        if (fixedOldUser == null) {
            System.out.println("Skipping test on update first user, cannot find the user, this is normal only if testing the creation of an empty file.");
        }else {
            Users.updateCredentials(fixedOldUser, newUser);
            System.out.printf("Here are the users after updating the first user: \n%s", Users.toStringUsers());
        }

    }

    // ORDERBOOK LINE TESTS
    public static void TestOrderBookLine() throws NullPointerException, IllegalArgumentException {

        // Test order book line.
        System.out.println("Testing order book line...");

        // Line price.
        SpecificPrice linePrice = new SpecificPrice(100, PriceType.ASK, Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());

        // Initial order.
        Quantity quantity = new Quantity(1);
        LimitOrder initiaLimitOrder = new LimitOrder(linePrice, quantity, true);

        // Valid order book line.
        OrderBookLine<LimitOrder> limitOrderBookLine = new OrderBookLine<>(initiaLimitOrder);

        System.out.printf("Here's the line order book line with only the initial order: \n%s.\n", limitOrderBookLine.toStringWithOrders());

        // Invalid line price / initial order.
        // Coherence checks.
        // Using a market order.
        try {
            MarketOrder initialMarketOrder = new MarketOrder(PriceType.ASK, Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency(), quantity);
            new OrderBookLine<>(initialMarketOrder);
        } catch (IllegalArgumentException ex) {
            System.out.println("Test passed, blocked invalid line price / initial order, using a market order.");
        }
        // Add order.
        // Adding an order with a different price value.
        try {
            SpecificPrice differentLinePrice = new SpecificPrice(limitOrderBookLine.getLinePrice().getValue() + 1, PriceType.ASK, Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());
            LimitOrder newLimitOrder = new LimitOrder(differentLinePrice, quantity, true);
            limitOrderBookLine.addOrder(newLimitOrder);
        } catch (IllegalArgumentException ex) {
            System.out.println("Test passed, blocked adding an order with a different price.");
        }
        // Adding an order with a different price type.
        try {
            SpecificPrice differentLinePrice = new SpecificPrice(limitOrderBookLine.getLinePrice().getValue(), PriceType.BID, Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());
            LimitOrder newLimitOrder = new LimitOrder(differentLinePrice, quantity, true);
            limitOrderBookLine.addOrder(newLimitOrder);
        } catch (IllegalArgumentException ex) {
            System.out.println("Test passed, blocked adding an order with a different price type.");
        }
        // Adding an order with different currencies.
        try {
            SpecificPrice differentLinePrice = new SpecificPrice(limitOrderBookLine.getLinePrice().getValue(), PriceType.ASK, Currency.ETH, Currency.EUR);
            // True here because otherwise the order book with these different currencies is not created and an error is thrown.
            LimitOrder newLimitOrder = new LimitOrder(differentLinePrice, quantity, true);
            limitOrderBookLine.addOrder(newLimitOrder);
        } catch (IllegalArgumentException ex) {
            System.out.println("Test passed, blocked adding an order with different currencies.");
        }

        // Adding multiple legit orders.
        LimitOrder newLimitOrder = null;
        for (int i = 0; i < 9; i++) {
            Quantity newQuantity = new Quantity(1);
            newLimitOrder = new LimitOrder(linePrice, newQuantity, true);
            User user = new User("testuser" + i, "testpassword");
            newLimitOrder.setUser(user);
            limitOrderBookLine.addOrder(newLimitOrder);
        }

        System.out.printf("Here's the line AFTER-ADDING ordes order book line: \n%s.\n", limitOrderBookLine.toStringWithOrders());

        // Cancel invalid (not present) order.
        try {
            limitOrderBookLine.cancelOrder(new LimitOrder(linePrice, new Quantity(1), true));
        } catch (IllegalArgumentException ex) {
            System.out.println("Test passed, blocked cancelling invalid (not present) order.");
        }

        // Cancel valid order.
        System.out.printf("Here's the order book line with the orders list before cancelling the last order: \n%s\n", limitOrderBookLine.toStringWithOrders());
        limitOrderBookLine.cancelOrder(newLimitOrder);
        System.out.printf("Here's the order book line with the orders list after cancelling the last order: \n%s\n", limitOrderBookLine.toStringWithOrders());

        // executeMarketOrderOnLimitLine and executeStopOrderFromStopLine TESTED IN THE ORDERBOOK TESTS.

    }

    // TEST ORDERS DEMO DATABASE FILE
    public static void TestOrdersDemoFile(String dbOrdersFilePath) throws IOException, InvalidOrder, NoSuchMethodException, IllegalStateException, JsonSyntaxException, IllegalArgumentException {

        // Test orders demo database file.
        System.out.println("Testing orders demo database file...");

        // DBOrdersInterface tests.

        // Read file content before setting the file.
        try {
            DBOrdersInterface.readFile();
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked reading orders FROM INTERFACE without setting the file.");
        }

        // Loading orders before setting the file.
        try {
            DBOrdersInterface.loadOrders(false, false);
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked loading orders FROM INTERFACE without setting the file.");
        }

        // Writing order before setting the file.
        SpecificPrice specificPrice = new SpecificPrice(100, PriceType.ASK, Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());
        LimitOrder limitOrder = new LimitOrder(specificPrice, new Quantity(1), true);
        try {
            DBOrdersInterface.writeOrderOnFile((Order) limitOrder);
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked writing order FROM INTERFACE without setting the file.");
        }

        // Need to do these tests of the Orders class here, before proceeding with the DBOrdersInterface tests.

        // Add order before setting the file.
        try {
            Orders.addOrder((Order) limitOrder, false, true);
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked adding order FROM ORDERS before setting the file.");
        }

        // Load orders before setting the file.
        try {
            Orders.loadOrders(true, false);
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked loading orders FROM ORDERS before setting the file.");
        }


        // Non existent file (to create it).
        // String filePathNonExistent = "./DB/Orders/orders2.json";
        // String filePath = filePathNonExistent;
        // DBOrdersInterface.setFile(filePathNonExistent);


        // Existing file.
        String filePath = dbOrdersFilePath;
        DBOrdersInterface.setFile(filePath);

        // Re-setting the file.
        try {
            DBUsersInterface.setFile(filePath);
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked re-setting FROM INTERFACE the file.");
        }

        // Writing order before reading the file.
        try {
            DBOrdersInterface.writeOrderOnFile((Order) limitOrder);
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked writing order FROM INTERFACE before reading the file.");
        }

        // Reading the file.
        DBOrdersInterface.readFile();

        // Re-reading the file.
        try {
            DBOrdersInterface.readFile();
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked re-reading FROM INTERFACE the file.");
        }




        // Now all the others methods to test are ALSO USED in the Users class, so testing them through the Users class.
        // Users tests.




        // Loading orders.
        Orders.loadOrders(true, true);
        System.out.printf("Here are the orders loaded from the file: \n%s", Orders.toStringOrders());

        // Re-loading orders.
        try {
            Orders.loadOrders(true, true);
        }catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked re-loading orders FROM ORDERS.");
        }

        System.out.printf("The number of orders in the file is: %d.\n", Orders.getOrdersSize());

    }
    
    // TEST ORDERS DATABASE FILE
    public static void TestOrdersFile(String dbOrdersFilePath) throws IOException, InvalidOrder, NoSuchMethodException {

        // Test orders database file.
        System.out.println("Testing orders database file...");

        

        // Non existent file (to create it).
        // String filePathNonExistent = "./DB/Orders/orders2.json";
        // String filePath = filePathNonExistent;
        // DBOrdersInterface.setFile(filePathNonExistent);



        // Existing file.
        String filePath = dbOrdersFilePath;
        DBOrdersInterface.setFile(filePath);


        // Reading the file.
        DBOrdersInterface.readFile();


        // Loading orders.
        Orders.loadOrders(true, true);
        System.out.printf("Here are the orders loaded from the file: \n%s", Orders.toStringOrders());

        // Writing order.
        SpecificPrice specificPrice = new SpecificPrice(100, PriceType.ASK, Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());
        LimitOrder limitOrder = new LimitOrder(specificPrice, new Quantity(1), true);
        Orders.addOrder((Order) limitOrder, false, true);
        System.out.printf("Here are the orders after adding the new order: \n%s", Orders.toStringOrders());

        // Re-adding same order.
        try {
            Orders.addOrder((Order) limitOrder, false, true);
        }catch (InvalidOrder ex) {
            System.out.println("Test passed, blocked re-adding order FROM ORDERS.");
        }

    }

    // ORDER BOOK TESTS
    public static void TestOrderBook() throws InvalidOrder {

        // Test order book.
        System.out.println("Testing order book...");

        // Order book (not fully initialized).
        GenericPrice increment = new GenericPrice(1);
        OrderBook orderBook = new OrderBook(increment);

        // Trying to create an order with a non fully initialized order book and price coherence check.
        LimitOrder limitOrder;
        try {
            // Since the order book is not fully initialized, the price coherence check will fail.
            SpecificPrice price = new SpecificPrice(42, PriceType.ASK, Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());
            limitOrder = new LimitOrder(price, new Quantity(1), false);
        } catch (IllegalStateException ex) {
            System.out.println("Test passed, blocked creating LIMIT ORDER with a non fully initialized order book and price coherence check.");
        }
        
        // Different order currencies.
        // Throw away order book.
        OrderBook throwAwayOrderBook = new OrderBook(increment);
        SpecificPrice throwAwayBestAsk = new SpecificPrice(100, PriceType.ASK, Currency.USD, Currency.BTC);
        // No coherence check here since the order book is not fully initialized, and this is the first order to initialize it.
        LimitOrder throwAwayLimitOrder = new LimitOrder(throwAwayBestAsk, new Quantity(1), true);
        throwAwayOrderBook.executeOrder(throwAwayLimitOrder);
        try {
            SpecificPrice differentPrice = new SpecificPrice(throwAwayOrderBook.getActualPriceAsk().getValue(), PriceType.ASK, Currency.BTC, Currency.USD);
            // No coherence check here since an order book with these currencies does not exist.
            limitOrder = new LimitOrder(differentPrice, new Quantity(1), true);
            throwAwayOrderBook.executeOrder(limitOrder);
        } catch (IllegalArgumentException ex) {
            System.out.println("Test passed, blocked executing LIMIT ORDER with a different market.");
        }

        // HERE THERE ARE A LOT OF TESTS, IMPORTANT PART!
        // FUZZING APPROACH.

        // OrderBook initial prices fixed structure:
        Integer startMiddlePrice = 150;
        Integer startBidAskSpread = 60;
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

        // Some support variables.
        // Using always the same seed 42 as start to setup the order book with initial prices.
        Integer startRandomSeed = 42;
        Random random = new Random(startRandomSeed);

        Integer minQuantity = 1;
        Integer maxQuantity = 10;
        Quantity quantity;

        Integer minUser = 1;
        Integer maxUser = 10;
        Integer userValue;
        User user;

        // Initial ask and bid orders, to setup best ask and best bid prices and setup the order book as explained above.
        SpecificPrice bestStartAskPrice = new SpecificPrice(startMiddlePrice + startBidAskSpread, PriceType.ASK, Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());
        // Huge spread to test well.
        SpecificPrice bestStartBidPrice = new SpecificPrice(startMiddlePrice - startBidAskSpread, PriceType.BID, Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());

        // Initial quantity and user are fixed with the initial seed.
        quantity = new Quantity((random.nextInt((maxQuantity - minQuantity) + 1) + minQuantity));

        userValue = random.nextInt((maxUser - minUser) + 1) + minUser;
        user = new User("testuser" + userValue, "testpassword" + userValue);

        System.out.printf("Here's the UN-ITIALIZED starter LIMIT order BOOK: %s\n", orderBook.toString());

        // Initial best ask and best bid orders.
        // No coherence check here since this is the first order to initialize the order book.
        LimitOrder bestStartAskOrder = new LimitOrder(bestStartAskPrice, quantity, true);
        bestStartAskOrder.setUser(user);
        orderBook.executeOrder(bestStartAskOrder);

        System.out.printf("Here's the IN-ITIALIZED starter LIMIT order BOOK after the first order: %s\n", orderBook.toString());

        LimitOrder bestStartBidOrder = new LimitOrder(bestStartBidPrice, quantity, false);
        bestStartBidOrder.setUser(user);
        orderBook.executeOrder(bestStartBidOrder);

        System.out.printf("Here's the starter LIMIT order BOOK: %s\n", orderBook.toStringWithLimitBook());

        System.out.printf("TESTING LIMIT ORDERS.\n");

        // TESTING LIMIT ORDERS.
        Integer testsOrders = 50;

        for (int i = 0; i < testsOrders; i++) {

            // Quantity.
            quantity = new Quantity(random.nextInt((maxQuantity - minQuantity) + 1) + minQuantity);

            // Ask or bid.
            PriceType priceType = random.nextBoolean() ? PriceType.ASK : PriceType.BID;

            // Price.
            Integer priceValue = random.nextInt((stopMaxPrice - startMinPrice) + 1) + startMinPrice;
            SpecificPrice price = new SpecificPrice(priceValue, priceType, orderBook.getPrimaryCurrency(), orderBook.getSecondaryCurrency());

            // Order creation.
            try {
                limitOrder = new LimitOrder(price, quantity, false);
            } catch (IllegalArgumentException ex) {
                System.out.printf("Test passed, blocked invalid LIMIT ORDER creation (price: %d, type: %s) with error: %s\n", price.getValue(), price.getType().name(), ex.getMessage());
                // Skipping execution below.
                System.out.printf("\n\n");
                continue;
            }

            // User.
            userValue = random.nextInt((maxUser - minUser) + 1) + minUser;
            user = new User("testuser" + userValue, "testpassword" + userValue);
            limitOrder.setUser(user);

            // Order execution.
            System.out.printf("Here's the LIMIT ORDER that will be executed: %s.\n", limitOrder.toString());
            orderBook.executeOrder(limitOrder);
            System.out.printf("Here's the LIMIT order BOOK AFTER the order: %s\n", orderBook.toStringWithLimitBook());

            System.out.printf("\n\n");
            
        }

        System.out.printf("TESTING STOP ORDERS.\n");

        // TESTING STOP ORDERS.
        for (int i = 0; i < testsOrders; i++) {

            // Quantity.
            quantity = new Quantity(random.nextInt((maxQuantity - minQuantity) + 1) + minQuantity);

            // Ask or bid.
            PriceType priceType = random.nextBoolean() ? PriceType.ASK : PriceType.BID;

            // Price.
            Integer priceValue = random.nextInt((stopMaxPrice - startMinPrice) + 1) + startMinPrice;
            SpecificPrice price = new SpecificPrice(priceValue, priceType, orderBook.getPrimaryCurrency(), orderBook.getSecondaryCurrency());

            // Order creation.
            StopOrder stopOrder;
            try {
                stopOrder = new StopOrder(price, quantity, false);
            } catch (IllegalArgumentException ex) {
                System.out.printf("Test passed, blocked invalid STOP ORDER creation (price: %d, type: %s, best ask: %d, best bid: %d) with error: %s\n", price.getValue(), price.getType().name(), orderBook.getActualPriceAsk().getValue(), orderBook.getActualPriceBid().getValue(), ex.getMessage());
                // Skipping execution below.
                System.out.printf("\n\n");
                continue;
            }

            // User.
            userValue = random.nextInt((maxUser - minUser) + 1) + minUser;
            user = new User("testuser" + userValue, "testpassword" + userValue);
            stopOrder.setUser(user);

            // Order execution.
            System.out.printf("Here's the STOP ORDER that will be executed: %s.\n", stopOrder.toString());
            orderBook.executeOrder(stopOrder);
            System.out.printf("Here's the STOP order BOOK AFTER the order: %s\n", orderBook.toStringWithStopBook());

            System.out.printf("\n\n");
            
        }

        System.out.printf("TESTING AUTOMATIC STOP ORDERS EXECUTION.\n");
        System.out.printf("ENABLING VERBOSE LOGGING....\n");
        orderBook.setVerboseLogging(true);
        orderBook.startStopOrdersExecutorThread();

        System.out.printf("TESTING MARKET ORDERS & STOP ORDERS - BUY STOP.\n");
        MarketOrder marketOrder = new MarketOrder(PriceType.BID, orderBook.getPrimaryCurrency(), orderBook.getSecondaryCurrency(), new Quantity(1));
        orderBook.executeOrder(marketOrder);

        System.out.printf("TESTING MARKET ORDERS & STOP ORDERS - SELL STOP.\n");
        limitOrder = new LimitOrder(new SpecificPrice(173, PriceType.BID, orderBook.getPrimaryCurrency(), orderBook.getSecondaryCurrency()), new Quantity(5), false);
        orderBook.executeOrder(limitOrder);
        StopOrder stopOrder = new StopOrder(new SpecificPrice(174, PriceType.ASK, orderBook.getPrimaryCurrency(), orderBook.getSecondaryCurrency()), new Quantity(10), false);
        orderBook.executeOrder(stopOrder);
        marketOrder = new MarketOrder(PriceType.ASK, orderBook.getPrimaryCurrency(), orderBook.getSecondaryCurrency(), new Quantity(19));
        orderBook.executeOrder(marketOrder);

        System.out.printf("TESTING COMPLETELY UNSATISFIABLE MARKET ORDER.\n");
        marketOrder = new MarketOrder(PriceType.BID, orderBook.getPrimaryCurrency(), orderBook.getSecondaryCurrency(), new Quantity(1));
        orderBook.executeOrder(marketOrder);

        System.out.printf("TESTING PARTIALLY UNSATISFIABLE MARKET ORDER.\n");
        limitOrder = new LimitOrder(new SpecificPrice(174, PriceType.ASK, orderBook.getPrimaryCurrency(), orderBook.getSecondaryCurrency()), new Quantity(1), false);
        orderBook.executeOrder(limitOrder);
        marketOrder = new MarketOrder(PriceType.BID, orderBook.getPrimaryCurrency(), orderBook.getSecondaryCurrency(), new Quantity(2));
        orderBook.executeOrder(marketOrder);
        
        orderBook.stopStopOrdersExecutorThread();

    }



    /**
     *
     * Main method to execute some tests.
     *
     */
    public static void main(String[] args) throws NullPointerException, IllegalArgumentException, RejectedExecutionException, IllegalStateException, InterruptedException, IOException, RuntimeException, InvalidUser, JsonSyntaxException, NoSuchMethodException, InvalidOrder, IllegalAccessException {

        // Execute all the tests.
        System.out.println("Executing all tests...");

        Thread.currentThread().setName("MainTests");

        Separator separator = new Separator("-");
        System.out.println(separator);

        TestUtils();
        System.out.println(separator);

        TestUser();
        System.out.println(separator);

        // THIS MODIFIES (OVERWRITES) THE USERS.JSON FILE, SO PAY ATTENTION!
        copyFile("./DB/Users/defaultUsers.json", "./DB/Users/users.json");
        TestUsers("./DB/Users/users.json");
        System.out.println(separator);

        // Use the above order book to get the best prices.
        TestOrderBookLine();
        System.out.println(separator);

        // THIS MODIFIES (OVERWRITES) THE ORDERS.JSON FILE, SO PAY ATTENTION!
        // DO NOT USE THIS TEST WITH THE BELOW ONE TOGETHERS.
        // copyFile("./DB/Orders/storicoOrdini.json", "./DB/Orders/orders.json");
        // TestOrdersDemoFile("./DB/Orders/orders.json");
        // System.out.println(separator);

        // THIS MODIFIES (OVERWRITES) THE ORDERS.JSON FILE, SO PAY ATTENTION!
        copyFile("./DB/Orders/defaultOrders.json", "./DB/Orders/orders.json");
        TestOrdersFile("./DB/Orders/orders.json");
        System.out.println(separator);

        // TestOrderBook();
        // System.out.println(separator);

        System.out.println("All tests passed.");

        System.exit(0);

    }

}
