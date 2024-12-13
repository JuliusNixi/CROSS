import java.time.Instant;
import java.util.Arrays;
import java.util.TreeSet;

import CROSS.Enums.Currency;
import CROSS.Enums.Direction;
import CROSS.Enums.PriceType;
import CROSS.OrderBook.Market;
import CROSS.OrderBook.OrderBook;
import CROSS.Orders.LimitOrder;
import CROSS.Types.GenericPrice;
import CROSS.Types.Quantity;
import CROSS.Types.SpecificPrice;
import CROSS.Users.DBUsersInterface;
import CROSS.Users.User;
import CROSS.Users.Users;

public class Main {
    public static void main(String[] args) throws Exception {

        final String separator = "\n\n----------------------------------------\n\n";

        System.out.println(separator);
        
        /* 
        // Test server.
        String pathToConfigPropertiesFile = "./Configs/server-config.properties";
        Server server = new Server(pathToConfigPropertiesFile);
        server.startServer();
        @SuppressWarnings("unused")
        Thread s = server.startAccept();

        // Test client.
        pathToConfigPropertiesFile = "./Configs/client-config.properties";
        @SuppressWarnings("unused")
        Thread c = Client.CLI();
        Client client = new Client(pathToConfigPropertiesFile);
        client.connectClient();
        */

        // Test Users DB.

        String pathToUsersFile = "./DB/users.json";

        DBUsersInterface.setFile(pathToUsersFile);
        DBUsersInterface.readFile();
        DBUsersInterface.loadUsers();

        TreeSet<User> users = Users.getUsersCopy();
        for (User user : users) {
            System.out.println(user);
        }
        System.out.printf("Users count: %d.\n", users.size());

        System.out.println("Adding new user...");
        String randomUsername = String.format("testuser%d", Instant.now().getEpochSecond());
        User newUser = new User(randomUsername, "testpassword");
        Users.addUser(newUser);

        users = Users.getUsersCopy();
        for (User user : users) {
            System.out.println(user);
        }
        System.out.printf("Users count: %d.\n", users.size());

        System.out.println(separator);

        // Test Prices.
        SpecificPrice actualPriceAsk = new SpecificPrice(99, PriceType.ASK);
        SpecificPrice actualPriceBid = new SpecificPrice(101, PriceType.BID);
        // Test toString(), getType().
        System.out.println(actualPriceAsk);
        // Test compareTo().
        SpecificPrice prices[] = new SpecificPrice[2];
        try {
            // Exception expected.
            // Cannot sort prices of different types.
            prices[0] = actualPriceAsk;
            prices[1] = actualPriceBid;
            Arrays.sort(prices);
        }catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
        prices[1] = new SpecificPrice(prices[0].getValue() + 1, PriceType.ASK);
        System.out.printf("Unsorted prices: %s\n", Arrays.toString(prices));
        Arrays.sort(prices);
        System.out.printf("SORTED (reversed/descending) prices: %s\n", Arrays.toString(prices));

        System.out.println(separator);

        // Test Market.
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

        System.out.println(separator);

        // Test quantity, toString().
        Quantity quantity = new Quantity(10);
        System.out.printf("Quantity: %s.\n", quantity.toString());

        System.out.println(separator);

        // Test User (again).
        User user = newUser;
        System.out.println(user);

        System.out.println(separator);

        // Test Order and Buy LimitOrder.
        LimitOrder order;
        // Price ok.
        SpecificPrice price = new SpecificPrice(actualPriceAsk.getValue() - 1, PriceType.ASK);
        // Wrong prices.
        SpecificPrice tmpPriceBid = new SpecificPrice(actualPriceBid.getValue() + 1, PriceType.BID);
        SpecificPrice tmpPriceAsk = new SpecificPrice(actualPriceAsk.getValue() + 1, PriceType.ASK);
        try {
            // Exception expected.
            // Order constructor check test. Bid price, ask direction mismatch.
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
        order = new LimitOrder(BTCUSD, price, Direction.BUY, quantity, user.getUsername());
        System.out.println(order);

        System.out.println(separator);
        
        // Test Order Book with Buy Limit Orders.
        OrderBook BTCUSDorderBook = new OrderBook(Currency.BTC, Currency.USD, actualPriceAsk, actualPriceBid, increment);
        for (int i = order.getPrice().getValue(); i >= 90; i--) {
            price = new SpecificPrice(i, PriceType.ASK);
            order = new LimitOrder(BTCUSD, price, Direction.BUY, quantity, user.getUsername());
            quantity = new Quantity(quantity.getQuantity() + 1);
            BTCUSDorderBook.executeOrder(order);
        }
        System.out.println(BTCUSDorderBook);
        

    }

}
