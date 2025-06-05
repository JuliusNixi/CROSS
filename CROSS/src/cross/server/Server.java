package cross.server;

import cross.exceptions.InvalidConfig;
import cross.exceptions.InvalidOrder;
import cross.exceptions.InvalidUser;
import cross.orderbook.OrderBook;
import cross.orders.db.DBOrdersInterface;
import cross.orders.db.Orders;
import cross.types.price.GenericPrice;
import cross.users.db.DBUsersInterface;
import cross.users.db.Users;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.TreeMap;
import com.google.gson.JsonSyntaxException;
import java.net.InetSocketAddress;

/**
 *
 * The Server class.
 *
 * When initialized and then started, the server will accept clients after the call to startAccept() by using a dedicated thread and TCP sockets.
 * The use of a dedicated thread allows the caller (of the server) thread to do other things while the server is accepting clients on its own.
 *
 * For each accepted client (by the dedicated thread), a new thread will be created to handle it.
 * Then, this latter thread will be submitted to a CachedThreadPool.
 *
 * The server uses a configuration file to set the server's IP and port to listen on.
 * The extension of the file must be .properties and its path passed as argument to the constructor.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see AcceptThread
 * 
 * @see InvalidConfig
 *
 */
public class Server {

    // Path to the server's configuration file and the parameters read from it.
    private final String pathToConfigPropertiesFile;
    private final Integer serverPort;
    private final Integer serverNotificationsPort;
    private final InetAddress serverAddress;

    private Boolean isInitialized = false;

    // TCP server socket.
    private ServerSocket serverSocket = null;

    // UDP server socket.
    private DatagramSocket datagramSocket = null;

    // Thread that accepts clients.
    private AcceptThread acceptThread = null;

    /**
     *
     * Constructor of the Server class.
     *
     * @param pathToConfigPropertiesFile Path to the server's configuration file as String.
     *
     * @throws NullPointerException If the path to the server's configuration file is null.
     * @throws InvalidConfig If the server's IP or port are invalid, or if the server's configuration file extension is not .properties.
     * @throws FileNotFoundException If the server's configuration file is not found.
     * @throws IOException If there is an I/O error reading the server's configuration file.
     * @throws IllegalArgumentException If Malformed Unicode escape appears in the server's configuration file path.
     *
     */
    public Server(String pathToConfigPropertiesFile) throws NullPointerException, InvalidConfig, FileNotFoundException, IOException, IllegalArgumentException {

        // Null check.
        if (pathToConfigPropertiesFile == null) {
            throw new NullPointerException("Path to server's configuration file cannot be null.");
        }

        // .properties file check.
        if (!pathToConfigPropertiesFile.endsWith(".properties")) {
            throw new InvalidConfig("Invalid server's configuration file extension. Must be .properties.");
        }
        
        // Cannot be null, checked manually before, no need to check this exception.
        File configFile = new File(pathToConfigPropertiesFile);
        Properties props = new Properties();

        // Try with resources.
        // All the resources will be closed automatically.
        try (FileReader reader = new FileReader(configFile)) {

            // Read the properties file.
            props.load(reader);
            String server = props.getProperty("server_ip");
            String port = props.getProperty("server_port");
            String notificationsServerPortString = props.getProperty("server_notifications_port");
            if (server == null || port == null || notificationsServerPortString == null) {
                throw new InvalidConfig("Invalid (maybe null, not present) server IP or ports in the server's configuration file.");
            }

            // Parsing server port.
            this.serverPort = Integer.valueOf(port);
            if (serverPort < 0 || serverPort > 65535) {
                throw new InvalidConfig("Invalid server's port number in the server's configuration file.");
            }

            // Parsing server notifications port.
            this.serverNotificationsPort = Integer.valueOf(notificationsServerPortString);
            if (serverNotificationsPort < 0 || serverNotificationsPort > 65535) {
                throw new InvalidConfig("Invalid server's notifications port number in the server's configuration file.");
            }

            // Parsing IP.
            this.serverAddress = InetAddress.getByName(server);

            // Saving the path to the configuration file.
            this.pathToConfigPropertiesFile = pathToConfigPropertiesFile;


        // Throwed by FileReader.
        }catch (FileNotFoundException ex) {
            throw new FileNotFoundException("Server's configuration file not found.");
        }

        // Throwed by getByName.
        catch (UnknownHostException ex) {
            throw new InvalidConfig("Invalid server IP in the server's configuration file.");
        }

        // Throwed by Properties.load().
        catch (IOException ex) {
            throw new IOException("Error reading server's configuration file.");
        }

        // parseInt exception.
        catch (NumberFormatException ex) {
            throw new InvalidConfig("Invalid server's port number in the server's configuration file.");
        }

        // Throwed by Properties.load().
        // Malformed Unicode escape.
        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Illegal argument in server creation. Malformed Unicode escape appears in the server's configuration file path.");
        }

        // InvalidConfig exception.
        catch (InvalidConfig ex) {
            // Forwarding the exception.
            throw new InvalidConfig(ex.getMessage());
        }


    }

    /**
     *
     * Initialize the server.
     * 
     * After the call to this method, to start the server, call startServer().
     *
     * Synchronized method to avoid multiple inizializations from different threads.
     *
     * @throws IllegalStateException If the server is already initialized.
     * @throws IOException If there is an I/O error.
     *
     */
    public synchronized void initializeServer(String dbUsersFilePath, String dbOrdersFilePath) throws IllegalStateException, IOException {

        if (isInitialized) {
            throw new IllegalStateException("Server already initialized.");
        }

        OrderBook mainOrderBook = new OrderBook(new GenericPrice(1));
        OrderBook.setMainOrderBook(mainOrderBook);
        mainOrderBook.startStopOrdersExecutorThread();

        DBUsersInterface.setFile(dbUsersFilePath);
        DBUsersInterface.readFile();
        try {
            Users.loadUsers(this);
        } catch (JsonSyntaxException | IllegalStateException | NoSuchMethodException | IOException | InvalidUser ex) {
            throw new IOException("Error loading users from file.");
        }
        System.out.printf("DEBUG: USERS DB: \n%s\n", Users.toStringUsers());

        DBOrdersInterface.setFile(dbOrdersFilePath);
        DBOrdersInterface.readFile();
        try {
            Orders.loadOrders(true, true);
        } catch (JsonSyntaxException | IllegalStateException | InvalidOrder | IOException | NoSuchMethodException ex) {
            throw new IOException("Error loading orders from file.");
        }
        System.out.printf("DEBUG: ORDERS DB: \n%s\n", Orders.toStringOrders());

        System.out.println("Server successfully initialized.");

        this.isInitialized = true;

    }

    /**
     *
     * Start the server.
     * 
     * After the call to this method, to accept clients, call startAccept().
     * 
     * The server should be initialized, first, with initializeServer().
     *
     * Synchronized method to avoid multiple starts from different threads.
     *
     * @throws IllegalStateException If the server is not initialized.
     * @throws RuntimeException If the server is already started.
     * @throws IllegalArgumentException If the socket arguments are invalid.
     * @throws IOException If there is an I/O error.
     *
     */
    public synchronized void startServer() throws IllegalStateException, RuntimeException, IllegalArgumentException, IOException {

        // Not initialized check.
        if (!isInitialized) {
            throw new IllegalStateException("Server not initialized. Initialize it, first, with initializeServer().");
        }

        // Already started check.
        if (this.serverSocket != null) {
            throw new RuntimeException("Server already started.");
        }

        try {

            // Start the server.
            this.serverSocket = new ServerSocket(serverPort, 0, serverAddress);

            // Start the UDP server socket.
            this.datagramSocket = new DatagramSocket(serverNotificationsPort);

            System.out.printf("Started succesfully the server with these following args...\n%s\n", this.toString());

        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid socket arguments in server start.");
        } catch (IOException ex) {
            throw new IOException("I/O error starting the server.");
        }

    }

    /**
     *
     * Start accepting clients.
     * 
     * Call this method after initializeServer() and startServer().
     *
     * Synchronized method to avoid multiple starts from different threads.
     *
     * @throws IllegalStateException If the server is not initialized or not started.
     * @throws RuntimeException If the server is already accepting clients.
     * 
     */
    public synchronized void startAccept() throws IllegalStateException, RuntimeException {

        // Server not initialized check.
        if (!isInitialized) {
            throw new IllegalStateException("Server not initialized. Initialize it, first, with initializeServer().");
        }

        // Server not started check.
        if (this.serverSocket == null) {
            throw new IllegalStateException("Server not started. Start it, first, with startServer().");
        }

        // Server already accepting check.
        if (this.acceptThread != null) {
            throw new RuntimeException("Server already accepting clients.");
        }

        AcceptThread acceptThreadF = new AcceptThread(this);
        acceptThreadF.start();
        System.out.println("Accepting clients thread started.");
        this.acceptThread = acceptThreadF;

    }


    private TreeMap<String, InetSocketAddress> tcpToUdpBindings = new TreeMap<>();
    private NotificationRegisterThread notificationRegisterThread = null;
    public void startNotificationRegisterThread() {
        // Not initialized check.
        if (!isInitialized) {
            throw new IllegalStateException("Server not initialized. Initialize it, first, with initializeServer().");
        }
        // Server not started check.
        if (this.serverSocket == null) {
            throw new IllegalStateException("Server not started. Start it, first, with startServer().");
        }
        // Server already accepting check.
        if (this.notificationRegisterThread != null) {
            throw new RuntimeException("Server already registering clients for notifications.");
        }
        NotificationRegisterThread notificationRegisterThread = new NotificationRegisterThread(this);
        notificationRegisterThread.start();
        this.notificationRegisterThread = notificationRegisterThread;
    }
    public void registerClientForNotifications(String tcpIpAndPort, InetSocketAddress udpSocketAddress) {
        this.tcpToUdpBindings.put(tcpIpAndPort, udpSocketAddress);
    }
    public InetSocketAddress getUdpSocketAddressForTcpSocketAddress(String tcpIpAndPort) {
        return this.tcpToUdpBindings.get(tcpIpAndPort);
    }
    public void unregisterClientForNotifications(String tcpIpAndPort) {
        this.tcpToUdpBindings.remove(tcpIpAndPort);
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

        return this.serverPort;

    }
    /**
     *
     * Get the server notifications port.
     *
     * @return Integer rapresenting the server's notifications port.
     *
     */
    public Integer getServerNotificationsPort() {

        return this.serverNotificationsPort;

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
     * Get the server's initialized status.
     *
     * @return Boolean rapresenting the server's initialized status.
     *
     */
    public Boolean isInitialized() {

        return this.isInitialized;

    }
    /**
     *
     * Get the server's started status.
     *
     * @return Boolean rapresenting the server's started status.
     *
     */
    public Boolean isStarted() {

        return this.serverSocket != null;

    }
    /**
     *
     * Get the server's accepting status.
     *
     * @return Boolean rapresenting the server's accepting status.
     *
     */
    public Boolean isAccepting() {

        return this.acceptThread != null;

    }
    /**
     *
     * Get the server's notification registering status.
     *
     * @return Boolean rapresenting the server's notification registering status.
     *
     */
    public Boolean isNotificationRegistering() {

        return this.notificationRegisterThread != null;

    }
    /**
     *
     * Get the server's socket.
     * 
     * Protected method to be used only by the AcceptThread class.
     *
     * @return ServerSocket rapresenting the server's socket.
     *
     */
    protected ServerSocket getServerSocket() {

        return this.serverSocket;

    }
    /**
     *
     * Get the server's datagram socket.
     * 
     * Protected method to be used only by the ClientThread class.
     *
     * @return DatagramSocket rapresenting the server's datagram socket.
     *
     */
    public DatagramSocket getDatagramSocket() {

        return this.datagramSocket;

    }

    @Override
    public String toString() {

        return String.format("Server Info's [Server IP [%s] - Server port [%s] - Server notifications port [%s] - Configuration file path [%s] - Initialized [%s] - Started [%s] - Accepting [%s] - Notification registering [%s]]", this.getServerAddress(), this.getServerPort(), this.getServerNotificationsPort(), this.getPathToConfigPropertiesFile(), this.isInitialized() == true ? "Yes" : "No", this.isStarted() == true ? "Yes" : "No", this.isAccepting() == true ? "Yes" : "No", this.isNotificationRegistering() == true ? "Yes" : "No");

    }

}
