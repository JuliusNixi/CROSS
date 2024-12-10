import java.util.TreeSet;

import CROSS.Users.DBUsersInterface;
import CROSS.Users.User;
import CROSS.Users.Users;

public class Main {
    public static void main(String[] args) throws Exception {

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
        System.out.printf("Users count: %d\n", users.size());
        
        User newUser = new User("testuser2", "testpassword");
        Users.addUser(newUser);


    }

}
