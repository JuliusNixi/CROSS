package CROSS.Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Properties;
import CROSS.Exceptions.InvalidConfig;

/**
 * 
 * The Server class.
 * 
 * When started, the server will accept clients after the call to startAccept() using a dedicated thread and sockets.
 * 
 * For each accepted client, a new thread will be created to handle it.
 * Then, the thread will be submitted to a CachedThreadPool.
 * 
 * The server use a configuration file to set the server's IP and port.
 * The extension of the file must be .properties.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * @see AcceptThread
 * 
 */
public class Server {

    // Path to the server's configuration file and the parameters read from it.
    private String pathToConfigPropertiesFile = null;
    private Integer serverPort = null;
    private InetAddress serverAddress = null;

    // TCP server socket.
    private ServerSocket serverSocket = null;

    // Thread that accepts clients.
    private AcceptThread acceptThread = null;

    /**
     * 
     * Constructor of the Server class.
     * 
     * @param pathToConfigPropertiesFile Path to the server's config file.
     * 
     * @throws NullPointerException If the path to the server's config file is null.
     * @throws InvalidConfig If the server's IP or port are invalid, or if the file extension is not .properties.
     * @throws FileNotFoundException If the server's config file is not found.
     * @throws IOException If there is an I/O error reading the server's config file.
     * @throws IllegalArgumentException If there is an error reading the server's config file.
     * @throws Exception If there is an unknown error.
     * 
     */
    public Server(String pathToConfigPropertiesFile) throws NullPointerException, InvalidConfig, FileNotFoundException, IOException, IllegalArgumentException, Exception {

        // Null check.
        if (pathToConfigPropertiesFile == null) {
            throw new NullPointerException("Path to server's config file cannot be null.");
        }

        // .properties file.
        if (!pathToConfigPropertiesFile.endsWith(".properties")) {
            throw new InvalidConfig("Invalid file extension. Must be .properties.");
        }

        // Cannot be null, no need to check the exception.
        File configFile = new File(pathToConfigPropertiesFile);
        Properties props = new Properties();
        
        // Try with resources.
        try (FileReader reader = new FileReader(configFile)) {
            
            // Read the properties file.
            props.load(reader);
            String server = props.getProperty("server_ip");
            String port = props.getProperty("server_port");

            if (server == null || port == null) {
                throw new InvalidConfig("Invalid server IP or port.");
            }

            // Parsing port.
            this.serverPort = Integer.parseInt(port);
            if (serverPort < 0 || serverPort > 65535) {
                throw new InvalidConfig("Invalid port number.");
            }            

            // Parsing IP.
            this.serverAddress = InetAddress.getByName(server);
            
            this.pathToConfigPropertiesFile = pathToConfigPropertiesFile;

        // Throwed by getByName.
        }catch (UnknownHostException ex) {
            throw new InvalidConfig("Invalid server IP.");
        }

        // Throwed by FileReader.
        catch (FileNotFoundException ex) {
            throw new FileNotFoundException("Server's config file not found.");
        }

        // parseInt exception.
        catch (NumberFormatException ex) {
            throw new InvalidConfig("Invalid port number.");
        }
        
        // Throwed by Properties.load().
        catch (IOException ex) {
            throw new IOException("Error reading server config file.");
        }

        // InvalidConfig exception.
        catch (InvalidConfig ex) {
            // Forward the exception.
            throw new InvalidConfig(ex.getMessage());
        }

        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Illegal argument in server creation.");
        }

        // Generic exception.
        catch (Exception ex) {
            throw new Exception("Unknown error in server creation.");
        }
        
    } // End of constructor.

    // SERVER START
    /**
     * 
     * Start the server. After the call to this method, to accept clients, call startAccept().
     * 
     * @throws RuntimeException If the server is already started.
     * @throws IllegalArgumentException If the socket arguments are invalid.
     * @throws IOException If there is an I/O error.
     * @throws Exception If there is an unknown error.
     * 
     */
    public void startServer() throws RuntimeException, IllegalArgumentException, IOException, Exception {

        // Already started check.
        if (this.serverSocket != null) {
            throw new RuntimeException("Server already started.");
        }

        try {
            // Start the server.
            this.serverSocket = new ServerSocket(serverPort, 0, serverAddress);
            System.out.printf("Started succesfully the server with these following args...\n%s\n", this.toString());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid socket arguments in server start.");
        } catch (IOException ex) {
            throw new IOException("I/O error starting the server.");
        } catch (Exception ex) {
            throw new Exception("Unknown error in server start.");
        }

    }

    // CLIENTS ACCEPTANCE
    /**
     * 
     * Start accepting clients. Call this method after startServer().
     * 
     * @return The new thread that accepts clients.
     * 
     * @throws RuntimeException If the server is not started (socket null) or if the accept thread is already started.
     * 
     */
    public AcceptThread startAccept() throws RuntimeException {

        // Server not started check.
        if (this.serverSocket == null) {
            throw new RuntimeException("Server not started. Call before startServer().");
        }

        // Already started check.
        if (this.acceptThread != null) {
            throw new RuntimeException("Accept thread already started.");
        }

        AcceptThread acceptThread = new AcceptThread(this);
        acceptThread.start();

        this.acceptThread = acceptThread;

        return this.acceptThread;

    }

    // GETTERS
    /**
     * 
     * Get the path to the server's configuration file.
     * 
     * @return A string rapresenting the path to the server's configuration file.
     * 
     */
    public String getPathToConfigPropertiesFile() {
        return String.format("%s", this.pathToConfigPropertiesFile);
    }
    /**
     * 
     * Get the server's port.
     * 
     * @return Integer rapresenting the server's port.
     * 
     */
    public Integer getServerPort() {
        return Integer.valueOf(this.serverPort);
    }
    /**
     * 
     * Get the server address.
     * 
     * @return InetAddress rapresenting the server's address.
     * 
     */
    public InetAddress getServerAddress() {
        return this.serverAddress;
    }
    /**
     * 
     * Get the socket.
     * 
     * @return Socket rapresenting the server's socket.
     * 
     */
    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }
    /**
     * 
     * Get the server acceptance's thread.
     * 
     * @return The server acceptance's thread.
     * 
     */ 
    public AcceptThread getAcceptThread()  {
        return this.acceptThread;
    }
     
    @Override
    public String toString() {
        return String.format("Server Info's [Server IP [%s] - Server port [%s] - Config file path [%s]]", this.getServerAddress(), this.getServerPort(), this.getPathToConfigPropertiesFile());
    }
    
}
