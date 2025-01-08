package CROSS.Client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.net.Socket;
import java.net.SocketException;
import CROSS.Exceptions.InvalidConfig;
import CROSS.Users.User;

/**
 * The client class.
 * The idea is I could have multiple instances of the client, but only one CLI.
 * 
 * @version 1.0
 * @see ClientCLIThread
 */
public class Client {

    // Thread for the CLI.
    private static ClientCLIThread clientCLI = null;

    private String pathToConfigPropertiesFile = null;
    private Integer serverPort = null;
    private InetAddress serverAddress = null;

    private Socket socket = null;

    private OutputStream outputStream = null;

    private User userLogged = null;

    /**
     * Constructor of the Client class.
     * 
     * @param pathToConfigPropertiesFile Path to the client config file.
     * @throws NullPointerException If the path to the client config file is null.
     */
    public Client(String pathToConfigPropertiesFile) throws NullPointerException {

        if (pathToConfigPropertiesFile == null) {
            throw new NullPointerException("Path to client config file cannot be null.");
        }

        File configFile = new File(pathToConfigPropertiesFile);
        Properties props = new Properties();
        
        // Try with resources.
        try (FileReader reader = new FileReader(configFile)) {
            
            // Read the properties file.
            props.load(reader);
            String server = props.getProperty("server_ip");
            String port = props.getProperty("server_port");

            if (server == null || port == null) {
                throw new InvalidConfig("Invalid server IP or server port.");
            }

            // Parsing IP and port.
            this.serverPort = Integer.parseInt(port);
            if (serverPort < 0 || serverPort > 65535) {
                throw new InvalidConfig("Invalid port number.");
            }
            this.serverAddress = InetAddress.getByName(server);

            this.pathToConfigPropertiesFile = pathToConfigPropertiesFile;

        // Throwed by getByName.
        }catch (UnknownHostException ex) {
            // TODO: Error handling.
        }

        // Throwed by FileReader.
        catch (FileNotFoundException ex) {
            // TODO: Error handling.
        }

        // parseInt exception.
        catch (NumberFormatException ex) {
            // TODO: Error handling.
        }
        
        // Throwed by Properties.load().
        catch (IllegalArgumentException ex) {
            // TODO: Error handling.
        } catch (IOException ex) {
            // TODO: Error handling.
        }
        catch (NullPointerException ex) {
            // TODO: Error handling.
        } 

        // InvalidConfig exception.
        catch (InvalidConfig ex) {
            // TODO: Error handling.
        }

        // Generic exception.
        catch (Exception ex) {
            // TODO: Error handling.
        }

    }
    
    /**
     * Connects the client to the server.
     * @throws RuntimeException If the client is already connected.
     */
    public void connectClient() throws RuntimeException {

        if (this.socket != null) {
            throw new RuntimeException("Client already connected.");
        }

        try {
            this.socket = new Socket(serverAddress, serverPort);
            // 10 seconds timeout.
            socket.setSoTimeout(10 * 1000);
            this.outputStream = socket.getOutputStream();
            System.out.println("Client connected succesfully!");
        } catch (NullPointerException ex) {
            // TODO: Error handling.
        } catch (IllegalArgumentException ex) {
            // TODO: Error handling.
        } catch (SocketException ex) {
            // TODO: Error handling.
        } catch (IOException ex) {
            // TODO: Error handling.
        } catch (Exception ex) {
            // TODO: Error handling.
        }

    }

    // CLI
    /**
     * Command Line Interface.
     * Static, I cannot have multiple instances of the CLI.
     * Returns the CLI thread.
     * The check for the not null client's socket is performed inside the ClientCLIThread constructor.
     * @param client The client to start the CLI for.
     * @return ClientCLIThread, the CLI thread.
     * @throws RuntimeException If the client's socket is null or the CLI is already started.
     * @throws NullPointerException If the client is null.
     */
    public static ClientCLIThread CLI(Client client) throws RuntimeException, NullPointerException {
        
        // Check for null client.
        if (client == null) {
            throw new NullPointerException("Client cannot be null.");
        }

        // Start the CLI.
        if (Client.clientCLI != null) {
            throw new RuntimeException("CLI already started.");
        }

        // Get and start a thread.
        ClientCLIThread clientCLI = new ClientCLIThread(client);
        clientCLI.start();
        Client.clientCLI = clientCLI;

        return Client.clientCLI;

    }
    /**
     * Get the client CLI's thread.
     * @return The client CLI's thread.
     */
    public static ClientCLIThread getClientCLI() {
        return Client.clientCLI;
    }    
  
    // GETTERS
    /**
     * Get the path to the client's configuration file.
     * @return A string rapresenting the path to the client's configuration file.
     */
    public String getPathToConfigPropertiesFile() {
        return String.format("%s", this.pathToConfigPropertiesFile);
    }
    /**
     * Get the server port.
     * @return Integer rapresenting the server's port used by the client to connect.
     */
    public Integer getServerPort() {
        return Integer.valueOf(this.serverPort);
    }
    /**
     * Get the server address.
     * @return InetAddress rapresenting the server's address.
     */
    public InetAddress getServerAddress() {
        return this.serverAddress;
    }
    /**
     * Get the socket.
     * @return Socket rapresenting the client's socket.
     */
    public Socket getSocket() {
        return this.socket;
    }
    /**
     * Get the output stream.
     * @return OutputStream of the client.
     */
    public OutputStream getOutputStream() {
        return this.outputStream;
    }
    /**
     * Get the logged user.
     * The logging status is also indipendently managed by the server to ensure more security.
     * @return User rapresenting the logged user or null if no user is logged.
     */
    public User getLoggedUser() {
        if (this.userLogged == null) {
            return null;
        }
        
        return new User(this.userLogged.getUsername(), this.userLogged.getPassword());
    }

    // SETTERS
    /**
     * Set the user logged.
     * @param userLogged The logged user.
     * @throws NullPointerException If the logged user is null.
     */
    public void setLoggedUser(User userLogged) throws NullPointerException {
        if (userLogged == null) {
            throw new NullPointerException("User logged cannot be null.");
        }

        this.userLogged = userLogged;
    }

    @Override
    public String toString() {
        return String.format("Client Info's [Server IP [%s] - Server port [%s] - Config file path [%s]]", this.getServerAddress(), this.getServerPort(), this.getPathToConfigPropertiesFile());
    }

    /**
     * 
     * Send a JSON to the server.
     * @param json The JSON to send to the server.
     * @throws NullPointerException If the json is null.
     * @throws RuntimeException If the outputStream is null.
     */
    public void sendJSONToServer(String json) throws NullPointerException, RuntimeException {

        if (this.outputStream == null) {
            throw new RuntimeException("Output stream cannot be null. Call connectClient() before.");
        }

        if (json == null) 
            throw new NullPointerException("json cannot be null.");

        try {
            // Buffered to optimize the performance.
            BufferedOutputStream outputStreamBuff = new BufferedOutputStream(this.outputStream);
            outputStreamBuff.write(json.getBytes());
            outputStreamBuff.flush();
        } catch (IOException ex) {
            // TODO: Error handling.
        } catch (Exception ex) {
            // TODO: Error handling.
        }

    }

}
