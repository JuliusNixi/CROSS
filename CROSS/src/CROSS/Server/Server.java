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
 * The Server class.
 * 
 * @version 1.0
 * @see AcceptThread
 */
public class Server {

    private String pathToConfigPropertiesFile = null;
    private Integer serverPort = null;
    private Integer maxConnections = null;
    private InetAddress serverAddress = null;

    private ServerSocket serverSocket = null;

    private AcceptThread acceptThread = null;

    /**
     * Constructor of the Server class.
     * 
     * @param pathToConfigPropertiesFile Path to the server config file.
     * @throws NullPointerException If the path to the server config file is null.
     */
    public Server(String pathToConfigPropertiesFile) throws NullPointerException {

        if (pathToConfigPropertiesFile == null) {
            throw new NullPointerException("Path to server config file cannot be null.");
        }

        File configFile = new File(pathToConfigPropertiesFile);
        Properties props = new Properties();
        
        // Try with resources.
        try (FileReader reader = new FileReader(configFile)) {
            
            // Read the properties file.
            props.load(reader);
            String server = props.getProperty("server_ip");
            String port = props.getProperty("server_port");
            String maxConnections = props.getProperty("server_max_connections");

            if (server == null || port == null) {
                throw new InvalidConfig("Invalid server IP or port.");
            }

            // Parsing IP and port.
            this.serverPort = Integer.parseInt(port);
            if (serverPort < 0 || serverPort > 65535) {
                throw new InvalidConfig("Invalid port number.");
            }            

            this.serverAddress = InetAddress.getByName(server);
            
            this.pathToConfigPropertiesFile = pathToConfigPropertiesFile;

            // Max connections checks.
            if (maxConnections == null) {
                maxConnections = "42";
                System.out.printf("Max connections not set in %s file. Using the default one: %s.\n", pathToConfigPropertiesFile, maxConnections);
            }
            this.maxConnections = Integer.parseInt(maxConnections);
            if (this.maxConnections < 1) {
                throw new InvalidConfig("Invalid max connections number.");
            }

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

        // InvalidIPOrPort exception.
        catch (InvalidConfig ex) {
            // TODO: Error handling.
        }

        // Generic exception.
        catch (Exception ex) {
            // TODO: Error handling.
        }
        
    } // End of constructor.

    /**
     * Start the server, after the call to this method, to accept clients, call startAccept().
     * @throws RuntimeException If the server is already started.
     */
    public void startServer() throws RuntimeException {

        if (this.serverSocket != null) {
            throw new RuntimeException("Server already started.");
        }

        System.out.printf("Starting server with these following args...\n%s\n", this.toString());

        try {
            // Start the server.
            this.serverSocket = new ServerSocket(serverPort, maxConnections, serverAddress);
            System.out.printf("Server started succesfully.\n");
        } catch (IllegalArgumentException ex) {
            // TODO: Error handling.
        } catch (IOException ex) {
            // TODO: Error handling.
        } catch (Exception ex) {
            // TODO: Error handling.
        }

    }

    // CLIENTS ACCEPTANCE
    /**
     * Start accepting clients. Call this method after startServer().
     * 
     * @return The thread that accepts clients.
     * @throws RuntimeException If the server is not started (socket null) or if the accept thread is already started.
     */
    public AcceptThread startAccept() throws RuntimeException {

        if (this.serverSocket == null) {
            throw new RuntimeException("Server socket is null. Server not started. Call before startServer().");
        }

        if (this.acceptThread != null) {
            throw new RuntimeException("Accept thread already started.");
        }

        AcceptThread acceptThread = new AcceptThread(this);
        acceptThread.start();

        this.acceptThread = acceptThread;

        return acceptThread;
    }
    /**
     * Get the server acceptance's thread.
     * @return The server acceptance's thread.
     */ 
    public AcceptThread getAcceptThread()  {
        return this.acceptThread;
    }
     
    // GETTERS
    /**
     * Get the path to the server's configuration file.
     * @return A string rapresenting the path to the server's configuration file.
     */
    public String getPathToConfigPropertiesFile() {
        return String.format("%s", this.pathToConfigPropertiesFile);
    }
    /**
     * Get the server port.
     * @return Integer rapresenting the server's port.
     */
    public Integer getServerPort() {
        return Integer.valueOf(this.serverPort);
    }
    /**
     * Get the max connections number allowed from the server.
     * @return Integer rapresenting the max connections number allowed from the server.
     */
    public Integer getMaxConnections() {
        return Integer.valueOf(this.maxConnections);
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
     * @return Socket rapresenting the server's socket.
     */
    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    @Override
    public String toString() {
        return String.format("Server IP [%s] - Server port [%s] - Max connections [%s] - Config file path [%s]", this.getServerAddress(), this.getServerPort(), this.getMaxConnections(), this.getPathToConfigPropertiesFile());
    }
    
}
