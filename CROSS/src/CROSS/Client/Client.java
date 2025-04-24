package CROSS.Client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.net.Socket;
import CROSS.API.Requests.Request;
import CROSS.Exceptions.InvalidConfig;
import CROSS.Users.User;

/**
 * 
 * The Client class.
 * 
 * The idea is I could have multiple instances of the client class, but only one CLI per class.
 * This since the CLI use the terminal I/O.
 * The CLI is handled by a dedicated thread.
 * 
 * The client is used to connect to the server and send requests.
 * The requests are JSON strings sent to the server through the socket.
 * 
 * The client use a configuration file to set the server's IP and port to connect to.
 * The file must have .properties extension.
 * 
 * There is also a dedicated thread to handle the responses / notifications from the server.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * 
 * @see ClientCLIThread
 * @see ResponsesThread
 * 
 */
public class Client {

    // Thread for the CLI.
    // Static, I cannot have multiple instances of the CLI.
    private static ClientCLIThread clientCLI = null;

    // Thread for responses / notifications from server.
    private ResponsesThread responsesThread = null;

    // Path to the client's configuration file and the parameters read from it.
    private final String pathToConfigPropertiesFile;
    private final Integer serverPort;
    private final InetAddress serverAddress;

    // TCP server socket.
    private Socket socket = null;

    // Used to send requests to the server.
    private OutputStream outputStream = null;

    // Used to create orders client-side before converting in JSON.
    private User connectedUser = null;

    /**
     * 
     * Constructor of the class.
     * 
     * @param pathToConfigPropertiesFile Path to the client's config file as String.
     * 
     * @throws NullPointerException If the path to the client's config file is null.
     * @throws InvalidConfig If the server's IP or port are invalid or the file has not a .properties extension.
     * @throws FileNotFoundException If the client's config file is not found.
     * @throws IOException If there is I/O an error reading the client's config file.
     * @throws IllegalArgumentException If there is an error reading the client's config file, a malformed Unicode escape appears in the input.
     * @throws Exception If there is an unknown error.
     * 
     */
    public Client(String pathToConfigPropertiesFile) throws NullPointerException, InvalidConfig, FileNotFoundException, IOException, IllegalArgumentException, Exception {

        // Null check.
        if (pathToConfigPropertiesFile == null) {
            throw new NullPointerException("Path to client's config file cannot be null.");
        }

        // .properties file check.
        if (!pathToConfigPropertiesFile.endsWith(".properties")) {
            throw new InvalidConfig("Invalid client's config file extension. Must be .properties.");
        }

        // Cannot be null, no need to check the exception.
        File configFile = new File(pathToConfigPropertiesFile);
        Properties props = new Properties();
        
        // Try with resources.
        // All the resources will be closed.
        try (FileReader reader = new FileReader(configFile)) {
            
            // Read the properties file.
            props.load(reader);
            String server = props.getProperty("server_ip");
            String port = props.getProperty("server_port");

            if (server == null || port == null) {
                throw new InvalidConfig("Invalid (maybe null) server IP or port to connect to.");
            }

            // Parsing port.
            this.serverPort = Integer.parseInt(port);
            if (serverPort < 0 || serverPort > 65535) {
                throw new InvalidConfig("Invalid port number to connect to.");
            }

            // Parsing IP.
            this.serverAddress = InetAddress.getByName(server);

            this.pathToConfigPropertiesFile = pathToConfigPropertiesFile;

        // Throwed by getByName.
        }catch (UnknownHostException ex) {
            throw new InvalidConfig("Invalid server IP to connect to.");
        }

        // Throwed by FileReader.
        catch (FileNotFoundException ex) {
            throw new FileNotFoundException("Client's config file not found.");
        }

        // parseInt exception.
        catch (NumberFormatException ex) {
            throw new InvalidConfig("Invalid port number to connect to.");
        }
        
        // Throwed by Properties.load().
        catch (IOException ex) {
            throw new IOException("Error reading client's config file.");
        }

        // InvalidConfig exception.
        catch (InvalidConfig ex) {
            // Forward the exception.
            throw new InvalidConfig(ex.getMessage());
        }

        // Malformed Unicode escape.
        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Illegal argument in client creation. Malformed Unicode escape appears in the input.");
        }

        // Generic exception.
        catch (Exception ex) {
            throw new Exception("Unknown error in client creation.");
        }

    } // End of constructor.
  
    // CONNECTION / DISCONNECTION
    /**
     * 
     * Connects the client to the server.
     * 
     * It's a synchronized method to avoid multiple connections at the same time.
     * E.g: The main thread and the CLI thread could try to connect at the same time.
     * 
     * @throws RuntimeException If the client is already connected.
     * @throws IOException If there is an I/O socket error.
     * 
     */
    public synchronized void connectClient() throws RuntimeException, IOException {

        // Already connected check.
        if (this.socket != null) {
            throw new RuntimeException("Client already connected.");
        }

        try {
            this.socket = new Socket(serverAddress, serverPort);

            this.outputStream = socket.getOutputStream();

            System.out.println("Client connected succesfully!");
        }catch (IOException ex) {

            String messageError = "First I/O error in connecting to the server.";

            if (this.outputStream != null) {
                try {
                    this.outputStream.close();
                } catch (IOException ex2) {
                    messageError += " Another I/O error closing the output stream with the server.";
                }
            }

            if (this.socket != null) {
                try {
                    this.socket.close();
                } catch (IOException ex2) {
                    messageError += " Another I/O error closing the socket with the server.";
                }
            }

            throw new IOException(messageError);

        }

    }
    /**
     * 
     * Disconnects the client from the server.
     * 
     * It's a synchronized method to avoid multiple disconnections at the same time.
     * E.g: The main thread and the CLI thread could try to disconnect at the same time.
     * 
     * It sends an EXIT request to the server to notify the disconnection.
     * It closes the output stream and the socket.
     * After that, the client is disconnected and the object will be dead.
     * 
     * @throws RuntimeException If the client is already disconnected.
     * @throws IOException If there is a socket I/O error.
     * 
     */
    public synchronized void disconnectClient() throws RuntimeException, IOException {

        // Already not connected check.
        if (this.socket == null) {
            throw new RuntimeException("Client already not connected. Nothing to disconnect.");
        }

        String messageError = null;
        try {

            // Send an EXIT request to the server to exit gracefully.
            ClientActionsUtils.ClientActions action = ClientActionsUtils.ClientActions.EXIT;
            Request request = new Request(action, null);
            String json = request.toJSONString();
            try {
                this.sendJSONToServer(json);
            }catch (IOException ex) {
                // Trying to continue to close the output stream and the socket anyway.
                System.err.println("Error sending EXIT request to the server while disconnecting. Closing anyway.");
            }

            // Close the output stream.
            this.outputStream.close();

        } catch (IOException ex) {
            messageError = "Error closing the output stream while disconnecting the client from the server.";
        }

        try {
            // Close the socket.
            this.socket.close();
        }catch (IOException ex) {
            if (messageError != null) {
                messageError += " Error closing the socket while disconnecting the client from the server.";
            }else {
                messageError = "Error closing the socket while disconnecting the client from the server.";
            }
        }

        if (messageError != null){
            System.err.println(messageError);
            throw new IOException(messageError);
        }
        
        System.out.println("Client disconnected succesfully!");

    }
    /**
     * 
     * Check if the client is connected to the server.
     * Used in the CLI.
     * 
     * @return Boolean, true if the client is connected, false otherwise.
     * 
     */
    public Boolean isClientConnected() {

        return this.socket != null;

    }

    // CLI
    /**
     * 
     * Command Line Interface to interact with the server.
     * Static, I cannot have multiple instances of the CLI.
     * 
     * Synchronized ON THE CLASS to avoid multiple CLI starts.
     * 
     * @param client The client to start the CLI for.
     * 
     * @throws RuntimeException If the client's socket is null (not connected to the server yet) or the CLI is already started.
     * @throws NullPointerException If the client is null.
     * 
     */
    public static void CLI(Client client) throws RuntimeException, NullPointerException {
        
        // Synchronized ON THE CLASS to avoid multiple CLI starts.
        synchronized (Client.class) {

            // Check for null client.
            if (client == null) {
                throw new NullPointerException("Client to be used in the CLI cannot be null.");
            }

            // Check for null socket.
            if (client.getSocket() == null) {
                throw new RuntimeException("Client's socket cannot be null in CLI. Call connectClient() before.");
            }

            // Check already started CLI.
            if (Client.clientCLI != null) {
                throw new RuntimeException("Client's CLI already started.");
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
     * Cuold be used indipendently from the CLI, so to print them, but not to interact with the server.
     * 
     * Synchronized to avoid multiple starts by different threads.
     * 
     * Cannot have more threads started at the same time for the same client object.
     * 
     * @throws RuntimeException If the client's socket is null or the responses thread is already started.
     * 
     */
    public synchronized void responsesStart() throws RuntimeException {
        
        // Check for null socket.
        if (this.getSocket() == null || this.outputStream == null) {
            throw new RuntimeException("Client's socket / output stream cannot be null to handle server's responses. Call connectClient() before.");
        }

        // Check already started responses thread.
        if (this.responsesThread != null) {
            throw new RuntimeException("Client's responses thread already started.");
        }

        // Get and start a thread.
        ResponsesThread responsesThread = new ResponsesThread(this);
        responsesThread.start();

        this.responsesThread = responsesThread;

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

        return this.pathToConfigPropertiesFile;

    }
    /**
     * 
     * Get the server port.
     * 
     * @return Integer rapresenting the server's port used by the client to connect.
     * 
     */
    public Integer getServerPort() {

        return this.serverPort;

    }
    /**
     * 
     * Get the socket. Private, used only in this class (CLI method).
     * 
     * @return Socket rapresenting the client's socket.
     * 
     */
    private Socket getSocket() {

        return this.socket;

    }
    /**
     * 
     * Get the input stream.
     * Visible only to the package, used only in the ResponsesThread.
     * 
     * @return InputStream rapresenting the client's input stream.
     * 
     * @throws RuntimeException If the client is not connected.
     * @throws IOException If there is an I/O error getting the input stream.
     * 
     */
    InputStream getInputStream() throws RuntimeException, IOException {

        // Connection check.
        if (this.socket == null) {
            throw new RuntimeException("Client's socket cannot be null to get the input stream from the server. Call connectClient() before.");
        }

        try {
            return this.socket.getInputStream();
        } catch (IOException ex) {
            throw new IOException("Error getting the client's input stream from the server socket.");
        }

    }
     /**
     * 
     * Get the connected user.
     * 
     * @return An User object rapresenting the current connected user.
     * 
     */   
    public User getConnectedUser() {

        return this.connectedUser;

    }

    @Override
    public String toString() {

        String connected = this.socket != null ? "connected" : "not connected";
        return String.format("Client Info's [Server IP [%s] - Server port [%s] - Config file path [%s] - Status [%s]]", this.serverAddress, this.getServerPort(), this.getPathToConfigPropertiesFile(), connected);

    }

    /**
     * 
     * Send a JSON string to the server.
     * The JSON is sent through the socket and rapresent a request to the server.
     * 
     * Synchronized to avoid multiple requests at the same time on the same socket.
     * E.g: The main thread and the CLI thread could try to send a request at the same time.
     * 
     * @param json The JSON to send to the server.
     * 
     * @throws NullPointerException If the JSON is null.
     * @throws RuntimeException If the client is not connected.
     * @throws IOException If there is an I/O error sending the JSON to the server.
     * 
     */
    public synchronized void sendJSONToServer(String json) throws NullPointerException, RuntimeException, IOException {

        // No output stream / socket check.
        if (this.outputStream == null || this.socket == null) {
            throw new RuntimeException("Output stream / socket cannot be null to send JSON to the server. Call connectClient() before.");
        }

        // Null check.
        if (json == null) 
            throw new NullPointerException("JSON to send to the server cannot be null.");

        try {
            // Buffered to optimize the performance.
            BufferedOutputStream outputStreamBuff = new BufferedOutputStream(this.outputStream);
            
            outputStreamBuff.write(json.getBytes());
            outputStreamBuff.flush();
        } catch (IOException ex) {
            System.err.println("Error sending your JSON request to the server. Ignoring it.");
            // I decided to not close the output stream and the socket here for only one bad request.
        }

    }

}
