import CROSS.Tests.Tests;

public class Main {

    /* 

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
    public static void TestUser() {
        
        // Test User.
        System.out.println("Testing User...");
        
        User u = new User("testuser", "testpassword");
        System.out.println(u);

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

    */

    /*throws InterruptedException, InvalidUser*/

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
