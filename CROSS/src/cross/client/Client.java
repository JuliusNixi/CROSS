package cross.client;

import cross.api.requests.Request;
import cross.exceptions.InvalidConfig;
import cross.utils.ClientActionsUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Properties;

/**
 *
 * The Client class.
 * 
 * The client is used to connect to the server and send requests.
 * The requests are JSON strings sent to the server through the socket.
 *
 * The idea is I could have multiple instances of the client class, but only one CLI per class.
 * This since the CLI uses the terminal I/O.
 * The CLI is handled by a dedicated thread.
 * The CLI thread gets the user's input from the terminal and send the requests to the server.
 *
 * The client uses a configuration file to set the server's IP and port to connect to.
 * The file must have .properties extension.
 *
 * There is also a dedicated thread to handle the responses / notifications from the server and print them.
 * This thread is unique per client instance.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see ClientCLIThread
 * @see ResponsesThread
 * 
 * @see ClientActionsUtils
 * @see Request
 * 
 * @see InvalidConfig
 * 
 */
public class Client {

    // Path to the client's configuration file and the parameters read from it.
    private final String pathToConfigPropertiesFile;
    private final Integer serverPort;
    private final Integer serverNotificationsPort;
    private final InetAddress serverAddress;

    // TCP server socket.
    private Socket socket = null;

    // UDP server socket.
    private DatagramSocket datagramSocket = null;

    // Difference with the server:
    // In the server, the streams are created in the ClientThread class, since are used only there.
    // In the client, the streams are created in this class, since are used both in the ClientCLIThread class and in the ResponsesThread class.

    // Used to send requests to the server.
    private OutputStream outputStream = null;
    // Used to receive responses from the server.
    private InputStream inputStream = null;

    // Buffered streams for the client's socket. To optimize performance.
    private BufferedInputStream bin = null;
    private BufferedOutputStream bout = null;

    // Thread for the CLI.
    // Static, I cannot have multiple instances of the CLI.
    protected static ClientCLIThread clientCLI = null;

    // Thread for responses from server.
    private ResponsesThread responsesThread = null;

    // Thread for notifications from server.
    private NotificationsThread notificationsThread = null;

    // only for pretty printing and distinguishing the your own orders from the matched ones in the notifications printing.
    private LinkedList<Long> executedOrders = new LinkedList<>();
    public void addExecutedOrder(Long orderId) {

        // Adding the order id to the executed orders list.
        this.executedOrders.add(orderId);
        
    }
    public Boolean containsExecutedOrder(Long orderId) {

        // Returning the executed orders list.
        return this.executedOrders.contains(orderId);

    }


    /**
     *
     * Constructor of the class.
     *
     * @param pathToConfigPropertiesFile Path to the client's configuration file as String.
     *
     * @throws NullPointerException If the path to the client's configuration file is null.
     * @throws InvalidConfig If the server's IP or port to connect to are invalid or the file has not a .properties extension.
     * @throws FileNotFoundException If the client's configuration file is not found.
     * @throws IOException If there is I/O an error reading the client's configuration file.
     * @throws IllegalArgumentException If Malformed Unicode escape appears in the client's configuration file path.
     *
     */
    public Client(String pathToConfigPropertiesFile) throws NullPointerException, InvalidConfig, FileNotFoundException, IOException, IllegalArgumentException {

        // Null check.
        if (pathToConfigPropertiesFile == null) {
            throw new NullPointerException("Path to client's configuration file cannot be null.");
        }

        // .properties file check.
        if (!pathToConfigPropertiesFile.endsWith(".properties")) {
            throw new InvalidConfig("Invalid client's configuration file extension. Must be .properties.");
        }

        // Cannot be null, checked manually before, no need to check this exception.
        File configFile = new File(pathToConfigPropertiesFile);
        Properties props = new Properties();

        // Try with resources.
        // All the resources will be closed.
        try (FileReader reader = new FileReader(configFile)) {

            // Read the properties file.
            props.load(reader);
            String server = props.getProperty("server_ip");
            String port = props.getProperty("server_port");
            String notificationsServerPortString = props.getProperty("server_notifications_port");
            if (server == null || port == null || notificationsServerPortString == null) {
                throw new InvalidConfig("Invalid (maybe null, not present) server IP or ports to connect to in the client's configuration file.");
            }

            // Parsing server port.
            this.serverPort = Integer.valueOf(port);
            if (serverPort < 0 || serverPort > 65535) {
                throw new InvalidConfig("Invalid server port number to connect to in the client's configuration file.");
            }

            // Parsing server notifications port.
            this.serverNotificationsPort = Integer.valueOf(notificationsServerPortString);
            if (serverNotificationsPort < 0 || serverNotificationsPort > 65535) {
                throw new InvalidConfig("Invalid server notifications port number to connect to in the client's configuration file.");
            }

            // Parsing IP.
            this.serverAddress = InetAddress.getByName(server);

            // Saving the path to the configuration file.
            this.pathToConfigPropertiesFile = pathToConfigPropertiesFile;

        // Throwed by FileReader.
        }catch (FileNotFoundException ex) {
            throw new FileNotFoundException("Client's configuration file not found.");

        // Throwed by getByName.
        }catch (UnknownHostException ex) {
            throw new InvalidConfig("Invalid server IP to connect to in the client's configuration file.");
        }

        // Throwed by Properties.load().
        catch (IOException ex) {
            throw new IOException("Error reading client's configuration file.");
        }

        // parseInt exception.
        catch (NumberFormatException ex) {
            throw new InvalidConfig("Invalid port number to connect to in the client's configuration file.");
        }

        // Throwed by Properties.load().
        // Malformed Unicode escape.
        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Illegal argument in client creation. Malformed Unicode escape appears in the client's configuration file path.");
        }

        // InvalidConfig exception.
        catch (InvalidConfig ex) {
            // Forward the exception.
            throw new InvalidConfig(ex.getMessage());
        }

    }

    // CONNECTION / DISCONNECTION
    /**
     *
     * Connects the client to the server.
     *
     * It's a synchronized method to avoid multiple connections at the same time from different threads.
     *
     * @throws IllegalStateException If the client is already connected.
     * @throws IOException If there is an I/O error in getting the socket or the input and output streams from the server.
     *
     */
    public synchronized void connectClient() throws IllegalStateException, IOException {

        // Already connected check.
        if (this.socket != null) {
            throw new IllegalStateException("Client already connected.");
        }

        try {
            // Getting the socket.
            this.socket = new Socket(serverAddress, serverPort);

            // Getting the input and output streams.
            this.outputStream = socket.getOutputStream();
            this.inputStream = socket.getInputStream();

            // Creating the buffered streams.
            this.bin = new BufferedInputStream(inputStream);
            this.bout = new BufferedOutputStream(outputStream);

            // Creating the UDP socket.
            // The port is not specified, so the OS will assign a random port, an ephemeral one.
            this.datagramSocket = new DatagramSocket(0);

            // Printing the connection success.
            System.out.println(String.format("Client connected succesfully to %s:%d!", this.serverAddress, this.serverPort));
        }catch (IOException ex) {

            // Difference with the server:
            // This is NOT a dediacted thread, so I BACKWARD an exception, AND I DO NOT print it and I DO NOT try to continue.

            String messageError = "Error while getting socket or input and output streams from the server in the client. Is the server running? Closing this connection...\n";

            // Always close the buffered input (the outer) stream first.
            if (this.bin != null) {
                try {
                    this.bin.close();
                }catch (IOException ex2) {
                    messageError += "Error while closing buffered input stream from the server in the client.";
                }
            }else{
                // Maybe an error occurred while creating the buffered input stream, but the input stream is still valid.
                if (this.inputStream != null) {
                    try {
                        this.inputStream.close();
                    }catch (IOException ex2) {
                        messageError += "Error while closing input stream from the server in the client.";
                    }
                }
            }

            if (this.bout != null) {
                try {
                    this.bout.close();
                }catch (IOException ex2) {
                    messageError += "Error while closing buffered output stream from the server.";
                }
            }else{
                // Maybe an error occurred while creating the buffered output stream, but the output stream is still valid.
                if (this.outputStream != null) {
                    try {
                        this.outputStream.close();
                    }catch (IOException ex2) {
                        messageError += "Error while closing output stream from the server in the client.";
                    }
                }
            }
            
            try {
                if (this.socket != null)
                    this.socket.close();
            }catch (IOException ex2) {
                messageError += "Error while closing socket with the server in the client.";
            }

            throw new IOException(messageError);
    
        }

    }
   
    /**
     *
     * Disconnects the client from the server.
     *
     * It's a synchronized method to avoid multiple disconnections at the same time from different threads.
     *
     * It sends an EXIT request to the server to notify the disconnection and to exit gracefully.
     * It closes the output, input and socket streams.
     * It stops the responses thread and the CLI thread.
     * 
     * This method throws exceptions and does not print errors since it could be called by other threads (e.g: the ClientCLIThread).
     * If it disconnects the client successfully, it does not return anything.
     *
     * @throws IllegalStateException If the client is already disconnected.
     * @throws IOException If there is an I/O error.
     *
     */
    public synchronized void disconnectClient() throws IllegalStateException, IOException {

        // Already not connected check.
        if (this.socket == null || this.outputStream == null) {
            throw new IllegalStateException("Client already not connected. Nothing to disconnect.");
        }

        String messageError = null;

        // Send an EXIT request to the server to exit gracefully.
        ClientActionsUtils.ClientActions action = ClientActionsUtils.ClientActions.EXIT;
        Request request = new Request(action, null);
        String json = request.toJSONString();

        try {
            this.sendJSONToServer(json);
            // IllegalStateException is not thrown here, since the output stream and the socket are not null, checked before.
        }catch (IOException ex) {
            // Trying to continue to close the output stream and the socket anyway.
            messageError = "Error sending EXIT request to the server while disconnecting. Closing anyway the connection.";
        }

        try {
            this.bout.close();
        } catch (IOException ex) {
            if (messageError != null) messageError += " ";
            messageError += "Error closing the buffered output stream while disconnecting the client from the server.";
            try {
                this.outputStream.close();
            } catch (IOException ex2) {
                if (messageError != null) messageError += " ";
                messageError = "Error closing the output stream while disconnecting the client from the server.";
            }
        }
        
        try {
            this.bin.close();
        } catch (IOException ex) {
            if (messageError != null) messageError += " ";
            messageError += "Error closing the buffered input stream while disconnecting the client from the server.";
            try {
                this.inputStream.close();
            } catch (IOException ex2) {
                if (messageError != null) messageError += " ";
                messageError = "Error closing the input stream while disconnecting the client from the server.";
            }
        }

        try {
            this.socket.close();
        }catch (IOException ex) {
            if (messageError != null) messageError += " ";
            messageError = "Error closing the socket while disconnecting the client from the server.";
        }

        this.datagramSocket.close();

        if (messageError != null){
            throw new IOException(messageError);
        }

    }
    
    // SEND JSON TO SERVER
    /**
     *
     * Send a JSON string to the server.
     * The JSON is sent through the TCP socket and rapresent a request to the server.
     *
     * Synchronized to avoid multiple requests at the same time on the same socket.
     * E.g: The main thread and the CLI thread could try to send a request at the same time.
     *
     * @param json The JSON to send to the server.
     *
     * @throws NullPointerException If the JSON is null.
     * @throws IllegalStateException If the client is not connected.
     * @throws IOException If there is an I/O error sending the JSON to the server.
     *
     */
    public synchronized void sendJSONToServer(String json) throws NullPointerException, IllegalStateException, IOException {

        // No output stream / socket check.
        if (this.outputStream == null || this.socket == null) {
            throw new IllegalStateException("Client cannot be disconnected to send JSON to the server. Call connectClient() before.");
        }

        // Null check.
        if (json == null)
            throw new NullPointerException("JSON to send to the server cannot be null.");

        try {
            this.bout.write(json.getBytes());
            this.bout.flush();
        } catch (IOException ex) {
            throw new IOException("Error sending your JSON request to the server. Ignoring it.");
            // I decided to not close the output stream and the socket here for only one bad request.
        }

    }

    // CLI
    /**
     *
     * Command Line Interface to interact with the server.
     * Static, I cannot have multiple instances of the CLI.
     * 
     * Start a dedicated thread to handle the user's input through the CLI.
     *
     * Synchronized ON THE CLASS to avoid multiple CLI starts from different threads using different clients.
     *
     * @param client The client to start the CLI for.
     *
     * @throws IllegalStateException If the client's socket is null (not connected to the server yet) or the CLI is already started.
     * @throws NullPointerException If the client is null.
     *
     */
    public static void CLIStart(Client client) throws IllegalStateException, NullPointerException {

        // Synchronized ON THE CLASS to avoid multiple CLI starts.
        synchronized (Client.class) {

            // Check for null client.
            if (client == null) {
                throw new NullPointerException("Client to be used in the CLI cannot be null.");
            }

            // Check for null socket.
            if (client.getSocket() == null) {
                throw new IllegalStateException("Client's socket cannot be null in CLI. Call connectClient() before.");
            }

            // Check already started CLI.
            if (Client.clientCLI != null) {
                throw new IllegalStateException("Client's CLI already started.");
            }

            // Get and start the CLI thread.
            ClientCLIThread clientCLI = new ClientCLIThread(client);
            clientCLI.start();

            Client.clientCLI = clientCLI;

        }

    }

    // RESPONSES
    /**
     *
     * Start a dedicated thread to handle the responses from the server.
     * Could be used indipendently from the CLI, so to print them, but not to interact with the server.
     *
     * Synchronized to avoid multiple starts from different threads.
     *
     * Cannot have more threads started at the same time for the same client object.
     *
     * @throws IllegalStateException If the client's socket is null (not connected to the server yet) or the responses thread is already started.
     *
     */
    public synchronized void responsesStart() throws IllegalStateException {

        // Check for null socket.
        if (this.getSocket() == null || this.outputStream == null) {
            throw new IllegalStateException("Client is not connected to the server. Call connectClient() before.");
        }

        // Check already started responses thread.
        if (this.responsesThread != null) {
            throw new IllegalStateException("Client's responses thread already started.");
        }

        // Get and start a thread.
        ResponsesThread responsesThread = new ResponsesThread(this);
        responsesThread.start();

        this.responsesThread = responsesThread;

    }

    // NOTIFICATIONS
    /**
     *
     * Start a dedicated thread to handle the notifications from the server.
     * Could be used indipendently from the CLI, so to print them, but not to interact with the server.
     *
     * Synchronized to avoid multiple starts from different threads.
     *
     * Cannot have more threads started at the same time for the same client object.
     *
     * @throws IllegalStateException If the datagram socket is null (not connected to the server yet) or the notifications thread is already started.
     *
     */
    public synchronized void notificationsStart() throws IllegalStateException {

        // Check for null datagram socket.
        if (this.getDatagramSocket() == null) {
            throw new IllegalStateException("Client is not connected to the server. Call connectClient() before.");
        }

        // Check already started notifications thread.
        if (this.notificationsThread != null) {
            throw new IllegalStateException("Client's notifications thread already started.");
        }

        if (this.isClientConnected() == false) {
            throw new IllegalStateException("Client is not connected to the server. Call connectClient() before.");
        }

        // Get and start a thread.
        NotificationsThread notificationsThreadL = new NotificationsThread(this);
        notificationsThreadL.start();

        this.notificationsThread = notificationsThreadL;

    }

    // GETTERS
    /**
     *
     * Get the path to the client's configuration file.
     *
     * @return A string rapresenting the path to the client's configuration file.
     *
     */
    public String getPathToConfigPropertiesFile() {

        return String.format("%s", this.pathToConfigPropertiesFile);

    }
    /**
     *
     * Get the server port.
     *
     * @return Integer rapresenting the server's port used by the client to connect to.
     *
     */
    public Integer getServerPort() {

        return this.serverPort;

    }
    /**
     *
     * Get the server notifications port.
     *
     * @return Integer rapresenting the server's notifications port used by the client to receive notifications from.
     *
     */
    public Integer getServerNotificationsPort() {

        return this.serverNotificationsPort;

    }
    /**
     *
     * Get the server address used by the client to connect.
     *
     * @return InetAddress rapresenting the server's address used by the client to connect to.
     *
     */
    public InetAddress getServerAddress() {

        return this.serverAddress;

    }
    /**
     *
     * Check if the client is connected to the server.
     *
     * @return Boolean, true if the client is connected, false otherwise.
     *
     */
    public Boolean isClientConnected() {

        return this.socket != null;

    }
    /**
     *
     * Check if the CLI thread has been started.
     *
     * @return Boolean, true if the CLI thread has been started, false otherwise.
     *
     */
    public static Boolean hasCLIStarted() {

        return Client.clientCLI != null;

    }
    /**
     *
     * Check if the responses thread has been started.
     *
     * @return Boolean, true if the responses thread has been started, false otherwise.
     *
     */
    public Boolean hasResponsesStarted() {

        return this.responsesThread != null;

    }
    /**
     *
     * Get the socket.
     * Private, used in the CLIStart() method.
     *
     * @return Socket rapresenting the client's socket.
     *
     */
    protected Socket getSocket() {

        return this.socket;

    }
    /**
     *
     * Get the datagram socket.
     * Protected, used in the NotificationsThread class.
     *
     * @return DatagramSocket rapresenting the client's datagram socket.
     *
     */
    protected DatagramSocket getDatagramSocket() {

        return this.datagramSocket;

    }
    /**
     *
     * Get the buffered input stream.
     * Visible only to the package (client package), used only in the ResponsesThread class.
     *
     * @return BufferedInputStream object rapresenting the client's buffered input stream.
     *
     * @throws IllegalStateException If the client is not connected.
     *
     */
    BufferedInputStream getBufferedInputStream() throws IllegalStateException {

        // Connection check.
        if (this.socket == null) {
            throw new IllegalStateException("Client must be connected to the server to get the buffered input stream.");
        }

        return this.bin;

    }

    @Override
    public String toString() {

        String connected = this.socket != null ? "connected" : "not connected";
        String cliStarted = Client.clientCLI != null ? "started" : "not started";
        String responsesStarted = this.responsesThread != null ? "started" : "not started";
        return String.format("Client Info's [Server IP [%s] - Server port [%s] - Server notifications port [%s] - Config file path [%s] - Status [%s] - CLI [%s] - Responses [%s]]", this.serverAddress, this.getServerPort(), this.getServerNotificationsPort(), this.getPathToConfigPropertiesFile(), connected, cliStarted, responsesStarted);

    }


    public synchronized void notificationsStop() throws IllegalStateException {

        // Check already NOT started notifications thread.
        if (this.notificationsThread == null) {
            throw new IllegalStateException("Client's notifications thread already NOT started.");
        }

        this.notificationsThread.interrupt();

    }

    public synchronized void responsesStop() throws IllegalStateException {

        // Check already NOT started responses thread.
        if (this.responsesThread == null) {
            throw new IllegalStateException("Client's responses thread already NOT started.");
        }

        this.responsesThread.interrupt();


    }

}
