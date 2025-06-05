import cross.client.Client;
import cross.exceptions.InvalidConfig;
import java.io.IOException;

public class MainClient {

    public static void main(String[] args) {

        String workingDir = System.getProperty("user.dir");
        System.out.println("Current Working Directory: " + workingDir);

        System.out.println("Starting main client...");

        try {
            Client client = new Client("./Configs/client-config.properties");
            client.connectClient();
            client.responsesStart();
            client.notificationsStart();
            Client.CLIStart(client);
        } catch (InvalidConfig | IOException | IllegalArgumentException | NullPointerException | IllegalStateException ex) {
            System.err.println("Error: " + ex.getMessage());
        }

        System.out.println("Main client ended.");

    }
    
}
