import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.TreeSet;

import CROSS.Client.Client;
import CROSS.Enums.Currency;
import CROSS.Enums.Direction;
import CROSS.Enums.PriceType;
import CROSS.Exceptions.InvalidUser;
import CROSS.OrderBook.Market;
import CROSS.OrderBook.OrderBook;
import CROSS.Orders.LimitOrder;
import CROSS.Server.Server;
import CROSS.Types.GenericPrice;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;
import CROSS.Users.DBUsersInterface;
import CROSS.Users.User;
import CROSS.Users.Users;
import CROSS.Utils.Separator;

public class Main {

    public static void TestUserDB() throws IOException, InvalidUser {
        // Test Users DB.

        String pathToUsersFile = "./DB/users.json";

        DBUsersInterface.setFile(pathToUsersFile);
        DBUsersInterface.readFile();
        DBUsersInterface.loadUsers();

        System.out.println("Users DB loaded. Showing users...");
        TreeSet<User> users = Users.getUsersCopy();
        for (User user : users) {
            System.out.println(user);
        }
        System.out.printf("Users count: %d.\n", users.size());

        System.out.println("Adding new user...");
        long now = Instant.now().getEpochSecond();
        String randomUsername = String.format("testuser%d", now);
        String randomPassword = String.format("testpassword%d", now);
        User newUser = new User(randomUsername, randomPassword);
        Users.addUser(newUser);
        System.out.println("New user added.");

        System.out.println("Showing new users DB...");
        users = Users.getUsersCopy();
        for (User user : users) {
            System.out.println(user);
        }
        System.out.printf("Users count: %d.\n", users.size());


    }

    public static void TestServer() throws IOException {

        // Test server.
        System.out.println("Testing server...");

        String pathToConfigPropertiesFile = "./Configs/server-config.properties";

        Server server = new Server(pathToConfigPropertiesFile);
        server.startServer();

        @SuppressWarnings("unused")
        Thread s = server.startAccept();
        
    }

    public static void TestClient() throws IOException {

        // Test client.
        System.out.println("Testing client...");

        String pathToConfigPropertiesFile = "./Configs/client-config.properties";

        Client client = new Client(pathToConfigPropertiesFile);
        client.connectClient();

        @SuppressWarnings("unused")
        Thread c = Client.CLI(client);
        
    }
    
    public static void TestPrices() {

        // Test Prices.
        System.out.println("Testing Prices...");

        SpecificPrice actualPriceAsk = new SpecificPrice(99, PriceType.ASK);

        // Test toString(), getType().
        System.out.println(actualPriceAsk);
        
        // Test compareTo().
        SpecificPrice prices[] = new SpecificPrice[2];
        prices[0] = actualPriceAsk;
        prices[1] = new SpecificPrice(prices[0].getValue() + 1, PriceType.ASK);
        System.out.printf("Unsorted prices: %s\n", Arrays.toString(prices));
        Arrays.sort(prices);
        System.out.printf("SORTED (reversed/descending) prices: %s\n", Arrays.toString(prices));

    }

    public static void TestMarket() {

            // Test Market.
            System.out.println("Testing Market...");

            SpecificPrice actualPriceAsk = new SpecificPrice(99, PriceType.ASK);
            SpecificPrice actualPriceBid = new SpecificPrice(101, PriceType.BID);

            GenericPrice increment = new GenericPrice(1);
            Market BTCUSD = new Market(Currency.BTC, Currency.USD, actualPriceAsk, actualPriceBid, increment);
            // Test setActualPriceAsk(), setActualPriceBid().
            try {
                // Exception expected.
                // Cannot set bid price as ask price.
                BTCUSD.setActualPriceBid(actualPriceAsk);
            }catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }

            BTCUSD.setActualPriceAsk(actualPriceAsk);

            // Test toString() and various getters.
            System.out.println(BTCUSD);

    }

    public static void TestQuantity() {

        // Test Quantity.
        System.out.println("Testing Quantity...");

        Quantity quantity = new Quantity(10);
        System.out.printf("Quantity: %s.\n", quantity.toString());

    }

    public static void TestUser() {
        
        // Test User.
        System.out.println("Testing User...");
        
        User u = new User("testuser", "testpassword");
        System.out.println(u);

    }

    public static void TestGenericOrder() {

        // Nothing to test here.
        // Is an abstract class.

    }

    public static void TestLimit() {

        // Test LimitOrder.
        System.out.println("Testing LimitOrder...");

        LimitOrder order;

        SpecificPrice actualPriceAsk = new SpecificPrice(99, PriceType.ASK);
        SpecificPrice actualPriceBid = new SpecificPrice(101, PriceType.BID);
        GenericPrice increment = new GenericPrice(1);
        Market BTCUSD = new Market(Currency.BTC, Currency.USD, actualPriceAsk, actualPriceBid, increment);
        Quantity quantity = new Quantity(10);
        User user = new User("testuser", "testpassword");

        // Price ok.
        SpecificPrice aValidPrice = new SpecificPrice(actualPriceAsk.getValue() - 1, PriceType.ASK);

        // Wrong prices.
        SpecificPrice tmpPriceBid = new SpecificPrice(actualPriceBid.getValue() + 1, PriceType.BID);
        SpecificPrice tmpPriceAsk = new SpecificPrice(actualPriceAsk.getValue() + 1, PriceType.ASK);
        try {
            // Exception expected.
            // Order constructor check test. Bid price, ask direction missmatch.
            order = new LimitOrder(BTCUSD, tmpPriceBid, Direction.BUY, quantity, user.getUsername());
        }catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
        try {
            // Exception expected.
            // Buy Limit price higher than the market ask price.
            tmpPriceAsk = new SpecificPrice(actualPriceBid.getValue(), PriceType.ASK);
            order = new LimitOrder(BTCUSD, tmpPriceAsk, Direction.BUY, quantity, user.getUsername());
        }catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }

        order = new LimitOrder(BTCUSD, aValidPrice, Direction.BUY, quantity, user.getUsername());
        System.out.println(order);

    }

    public static void TestOrderBook() {

        // Test Order Book.
        System.out.println("Testing Order Book...");

        SpecificPrice actualPriceAsk = new SpecificPrice(99, PriceType.ASK);
        SpecificPrice actualPriceBid = new SpecificPrice(101, PriceType.BID);
        GenericPrice increment = new GenericPrice(1);

        OrderBook BTCUSDorderBook = new OrderBook(Currency.BTC, Currency.USD, actualPriceAsk, actualPriceBid, increment);
        System.out.println(BTCUSDorderBook);

    }



    // Returns an OrderBook with some orders executed.
    public static OrderBook TestExecutingLimit() {

        // Test Limit Order execution.
        System.out.println("Testing Limit Order execution...");

        LimitOrder order = null;

        SpecificPrice actualPriceAsk = new SpecificPrice(99, PriceType.ASK);
        SpecificPrice actualPriceBid = new SpecificPrice(101, PriceType.BID);
        GenericPrice increment = new GenericPrice(1);
        Market BTCUSD = new Market(Currency.BTC, Currency.USD, actualPriceAsk, actualPriceBid, increment);
        Quantity quantity = new Quantity(10);
        User user = new User("testuser", "testpassword");
        SpecificPrice validPrice = new SpecificPrice(actualPriceAsk.getValue(), PriceType.ASK);
        OrderBook BTCUSDorderBook = new OrderBook(Currency.BTC, Currency.USD, actualPriceAsk, actualPriceBid, increment);

        // Some buy orders.
        for (int i = validPrice.getValue(); i >= 90; i--) {
            validPrice = new SpecificPrice(i, PriceType.ASK);
            order = new LimitOrder(BTCUSD, validPrice, Direction.BUY, quantity, user.getUsername());
            quantity = new Quantity(quantity.getQuantity() + 1);
            BTCUSDorderBook.executeOrder(order);
        }

        // Some sell orders.
        validPrice = new SpecificPrice(actualPriceBid.getValue(), PriceType.BID);
        quantity = new Quantity(10);
        for (int i = validPrice.getValue(); i <= 110; i++) {
            validPrice = new SpecificPrice(i, PriceType.BID);
            order = new LimitOrder(BTCUSD, validPrice, Direction.SELL, quantity, user.getUsername());
            quantity = new Quantity(quantity.getQuantity() + 1);
            BTCUSDorderBook.executeOrder(order);
        }

        System.out.println(BTCUSDorderBook);

        return BTCUSDorderBook;

    }

    public static void main(String[] args) throws InterruptedException, InvalidUser {

        String separator = "\n" + Separator.getSeparator("-") + "\n";
        try {
            System.out.println(separator);
            TestServer();
            // USED ONLY TO SEE THE DEBUG PRINTS IN THE CORRECT ORDER!
            Thread.sleep(1000 * 1);
            System.out.println(separator);
            TestClient();
            // USED ONLY TO SEE THE DEBUG PRINTS IN THE CORRECT ORDER!
            Thread.sleep(1000 * 1);
            System.out.println(separator);
            TestUserDB();
            System.out.println(separator);
            TestPrices();
            System.out.println(separator);
            TestMarket();
            System.out.println(separator);
            TestQuantity();
            System.out.println(separator);
            TestUser();
            System.out.println(separator);
            TestGenericOrder();
            System.out.println(separator);
            /*TestLimit();
            System.out.println(separator);
            TestOrderBook();
            System.out.println(separator);
            OrderBook ob = TestExecutingLimit();
            for (LimitOrder o : ob.getLimitOrders()) {
                System.out.println(o.toString() + "\n");
            }
            System.out.println(separator);*/


            Thread.sleep(1000 * 5);
            //System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }


     /*    
        
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

    }

}
