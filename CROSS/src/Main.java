import CROSS.Client.Client;
import CROSS.Server.Server;

public class Main {
    public static void main(String[] args) throws Exception {

        String pathToConfigPropertiesFile = "./Configs/server-config.properties";
        Server server = new Server(pathToConfigPropertiesFile);
        server.startServer();
        @SuppressWarnings("unused")
        Thread s = server.startAccept();

        pathToConfigPropertiesFile = "./Configs/client-config.properties";
        @SuppressWarnings("unused")
        Thread c = Client.CLI();
        Client client = new Client(pathToConfigPropertiesFile);
        client.connectClient();
        
    }

}
