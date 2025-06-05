import cross.exceptions.InvalidConfig;
import cross.server.Server;
import java.io.IOException;

public class MainServer {

    public static void main(String[] args) {

        String workingDir = System.getProperty("user.dir");
        System.out.println("Current Working Directory: " + workingDir);

        System.out.println("Starting main server...");

        try {
            Server server = new Server("./Configs/server-config.properties");
            server.initializeServer("./DB/Users/users.json", "./DB/Orders/orders.json");
            server.startServer();
            server.startAccept();
            server.startNotificationRegisterThread();
        } catch (InvalidConfig | IOException | IllegalArgumentException | NullPointerException ex) {
            System.err.println("Error: " + ex.getMessage());
        }

        System.out.println("Main server ended.");

    }
    
}
