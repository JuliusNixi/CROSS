import com.google.gson.*;

import CROSS.Server.Server;
public class Main {
    public static void main(String[] args) throws Exception {

        System.out.println("Main ok!");

        final String pathToConfigPropertiesFile = "./CROSS/config.properties";
        Server server = new Server(pathToConfigPropertiesFile);

    }

}
