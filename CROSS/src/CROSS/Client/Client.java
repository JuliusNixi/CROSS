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

import CROSS.API.RequestResponse;
import CROSS.API.RequestResponse.AllResponses;
import CROSS.API.RequestResponse.ResponseType;
import CROSS.API.Responses.ResponseAndMessage;
import CROSS.Exceptions.InvalidConfig;
import CROSS.Users.User;

/**
 * 
 * The client class.
 * 
 * The idea is I could have multiple instances of the client, but only one CLI.
 * The CLI is handled by a dedicated thread.
 * 
 * The client is used to connect to the server and send requests.
 * 
 * The client use a configuration file to set the server's IP and port to connect to.
 * The file must have .properties extension.
 * 
 * There is also a dedicated thread to handle the responses from the server.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * @see ClientCLIThread
 * @see ResponsesThread
 * @see User
 * 
 */
public class Client {

    // Thread for the CLI.
    private static ClientCLIThread clientCLI = null;

    // Thread for responses from server.
    private ResponsesThread responsesThread = null;

    // Path to the client's configuration file and the parameters read from it.
    private String pathToConfigPropertiesFile = null;
    private Integer serverPort = null;
    private InetAddress serverAddress = null;

    // TCP server socket.
    private Socket socket = null;

    // Used to send requests to the server.
    private OutputStream outputStream = null;

    // Also checked server side.
    // Also checked client side to avoid spamming the server with wrong requests.
    private User userLogged = null;

    /**
     * 
     * Constructor of the Client class.
     * 
     * @param pathToConfigPropertiesFile Path to the client's config file.
     * 
     * @throws NullPointerException If the path to the client's config file is null.
     * @throws InvalidConfig If the server's IP or port are invalid or the file has not a .properties extension.
     * @throws FileNotFoundException If the client's config file is not found.
     * @throws IOException If there is I/O an error reading the client's config file.
     * @throws IllegalArgumentException If there is an error reading the client's config file.
     * @throws Exception If there is an unknown error.
     * 
     */
    public Client(String pathToConfigPropertiesFile) throws NullPointerException, InvalidConfig, FileNotFoundException, IOException, IllegalArgumentException, Exception {

        // Null check.
        if (pathToConfigPropertiesFile == null) {
            throw new NullPointerException("Path to client's config file cannot be null.");
        }

        // Check for .properties extension.
        if (!pathToConfigPropertiesFile.endsWith(".properties")) {
            throw new InvalidConfig("Client's config file must have .properties extension.");
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
                throw new InvalidConfig("Invalid server IP or port to connect to.");
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
            throw new IOException("Error reading client config file.");
        }

        // InvalidConfig exception.
        catch (InvalidConfig ex) {
            // Forward the exception.
            throw new InvalidConfig(ex.getMessage());
        }

        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Illegal argument in client creation.");
        }

        // Generic exception.
        catch (Exception ex) {
            throw new Exception("Unknown error in client creation.");
        }

    }
  
    /**
     * 
     * Connects the client to the server.
     * 
     * It's a synchronized method to avoid multiple connections at the same time.
     * E.g: The main thread and the CLI thread could try to connect at the same time.
     * 
     * @throws RuntimeException If the client is already connected.
     * @throws NullPointerException If the server address is null.
     * @throws IllegalArgumentException If the arguments are invalid.
     * @throws SocketException If there is an error with the socket.
     * @throws IOException If there is an I/O socket error.
     * @throws Exception If there is an unknown error.
     * 
     */
    public synchronized void connectClient() throws RuntimeException, NullPointerException, IllegalArgumentException, SocketException, IOException, Exception {

        // Already connected check.
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
            throw new NullPointerException("Server address to connect null.");
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid arguments in client connection.");
        } catch (SocketException ex) {
            try {
                if (this.outputStream != null) {
                    this.outputStream.close();
                }
                if (this.socket != null) {
                    this.socket.close();
                }
            } catch (IOException ex2) {
                throw new IOException("Error closing the socket with the server.");
            }
            throw new SocketException("Error with the server socket to connect to.");
        } catch (IOException ex) {
            try {
                if (this.outputStream != null) {
                    this.outputStream.close();
                }
                if (this.socket != null) {
                    this.socket.close();
                }
            } catch (IOException ex2) {
                throw new IOException("Error closing the socket with the server.");
            }
            throw new IOException("I/O error connecting to the server.");
        } catch (Exception ex) {
            try {
                if (this.outputStream != null) {
                    this.outputStream.close();
                }
                if (this.socket != null) {
                    this.socket.close();
                }
            } catch (IOException ex2) {
                throw new IOException("Error closing the socket with the server.");
            }
            throw new Exception("Unknown error in client connection.");
        }

    }
    /**
     * 
     * Disconnects the client from the server.
     * 
     * It's a synchronized method to avoid multiple disconnections at the same time.
     * E.g: The main thread and the CLI thread could try to disconnect at the same time.
     * 
     * It closes the output stream and the socket. After that, the client is disconnected and the object will be dead.
     * 
     * @throws RuntimeException If the client is already disconnected.
     * @throws IOException If there is a socket I/O error.
     * 
     */
    public synchronized void disconnectClient() throws RuntimeException, IOException {

        // Already not connected check.
        if (this.socket == null) {
            throw new RuntimeException("Client already not connected.");
        }

        try {

            // Create the response.
            ResponseType responseType = ResponseType.EXIT;
            AllResponses responseContent = AllResponses.EXIT;
            RequestResponse responseCode = new RequestResponse(responseType, responseContent);
            ResponseAndMessage response = new ResponseAndMessage(responseCode, "Client disconnection...");
            String json = response.toJSON(false);

            // Send the response.
            try {
                this.outputStream.write(json.getBytes());
                this.outputStream.flush();
            } catch (IOException ex) {
                System.err.println("Error sending the exit message to the server, closing connection anyway.");
                // Continue with the disconnection below.
            }

            // Close the output stream.
            this.outputStream.close();
            this.outputStream = null;

            // Close the socket.
            this.socket.close();
            this.socket = null;

            // Reset the user logged.
            this.userLogged = null;

            System.out.println("Client disconnected succesfully!");
        } catch (IOException ex) {
            throw new IOException("Error disconnecting the client from the server.");
        }

    }

    // CLI
    /**
     * 
     * Command Line Interface to interact with the server by using the client.
     * Static, I cannot have multiple instances of the CLI.
     * 
     * Returns the CLI thread.
     * 
     * @param client The client to start the CLI for.
     * 
     * @return ClientCLIThread, the CLI thread.
     * 
     * @throws RuntimeException If the client's socket is null or the CLI is already started.
     * @throws NullPointerException If the client is null.
     * 
     */
    public static ClientCLIThread CLI(Client client) throws RuntimeException, NullPointerException {
        
        // Check for null client.
        if (client == null) {
            throw new NullPointerException("Client cannot be null.");
        }

        // Check for null socket.
        if (client.getSocket() == null) {
            throw new RuntimeException("Client's socket cannot be null. Call connectClient() before.");
        }

        // Check already started CLI.
        if (Client.clientCLI != null) {
            throw new RuntimeException("CLI already started.");
        }

        // Get and start a thread.
        ClientCLIThread clientCLI = new ClientCLIThread(client);
        clientCLI.start();
        
        Client.clientCLI = clientCLI;

        return Client.clientCLI;

    }

    // RESPONSES
    /**
     * 
     * Start the thread to handle the responses from the server.
     * Cuold be used indipendently from the CLI.
     * 
     * @return ResponsesThread, the thread to handle the responses.
     * 
     * @throws RuntimeException If the client's socket is null or the responses thread is already started.
     * 
     */
    public ResponsesThread responsesStart() throws RuntimeException {
        
        // Check for null socket.
        if (this.getSocket() == null || this.getOutputStream() == null) {
            throw new RuntimeException("Client's socket cannot be null. Call connectClient() before.");
        }

        // Check already started responses thread.
        if (this.responsesThread != null) {
            throw new RuntimeException("Responses thread already started.");
        }

        // Get and start a thread.
        ResponsesThread responsesThread = new ResponsesThread(this);
        responsesThread.start();

        this.responsesThread = responsesThread;

        return this.responsesThread;

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
     * @return Integer rapresenting the server's port used by the client to connect.
     * 
     */
    public Integer getServerPort() {
        return Integer.valueOf(this.serverPort);
    }
    /**
     * 
     * Get the server address.
     * 
     * @return InetAddress rapresenting the server's address to connect.
     * 
     */
    public InetAddress getServerAddress() {
        return this.serverAddress;
    }
    /**
     * 
     * Get the socket.
     * 
     * @return Socket rapresenting the client's socket.
     * 
     */
    public Socket getSocket() {
        return this.socket;
    }
    /**
     * 
     * Get the output stream.
     * 
     * @return OutputStream of the client.
     * 
     */
    public OutputStream getOutputStream() {
        return this.outputStream;
    }
    /**
     * 
     * Get the logged user.
     * 
     * The logging status is also indipendently managed by the server to ensure more security.
     * 
     * @return User rapresenting the logged user or null if no user is logged.
     * 
     */
    public User getLoggedUser() {

        // Null check.
        if (this.userLogged == null) {
            return null;
        }
        
        // Syncronized no needed, since username and password cannot be changed.
        return new User(this.userLogged.getUsername(), this.userLogged.getPassword());

    }
    /**
     * 
     * Get the client CLI's thread.
     * 
     * @return The client CLI's thread.
     * 
     */
    public static ClientCLIThread getClientCLI() {
        return Client.clientCLI;
    }    
    
    // SETTERS
    /**
     * 
     * Set the user logged. Call this method after a successful login.
     * 
     * @param userLogged The now logged user.
     * 
     * @throws NullPointerException If the now logged user is null.
     * @throws RuntimeException If the output stream is null.
     * 
     */
    public void setLoggedUser(User userLogged) throws NullPointerException, RuntimeException {
        
        // Null check.
        if (userLogged == null) {
            throw new NullPointerException("User now logged cannot be null.");
        }

        // No output stream check.
        if (this.outputStream == null || this.socket == null) {
            throw new RuntimeException("Output stream cannot be null. Call connectClient() before.");
        }

        // Syncronized no needed, since username and password cannot be changed.
        this.userLogged = new User(userLogged.getUsername(), userLogged.getPassword());

    }

    @Override
    public String toString() {
        return String.format("Client Info's [Server IP [%s] - Server port [%s] - Config file path [%s]]", this.getServerAddress(), this.getServerPort(), this.getPathToConfigPropertiesFile());
    }

    /**
     * 
     * Send a JSON to the server.
     * 
     * Syncronized to avoid multiple requests at the same time on the same socket.
     * E.g: The main thread and the CLI thread could try to send a request at the same time.
     * 
     * @param json The JSON to send to the server.
     * 
     * @throws NullPointerException If the json is null.
     * @throws RuntimeException If the client is not connected.
     * @throws IOException If there is an I/O error sending the JSON to the server.
     * @throws Exception If there is an unknown error.
     * 
     */
    public synchronized void sendJSONToServer(String json) throws NullPointerException, RuntimeException, IOException, Exception {

        // No output stream check.
        if (this.outputStream == null || this.socket == null) {
            throw new RuntimeException("Output stream cannot be null. Call connectClient() before.");
        }

        // Null check.
        if (json == null) 
            throw new NullPointerException("JSON to send cannot be null.");

        try {
            // Buffered to optimize the performance.
            BufferedOutputStream outputStreamBuff = new BufferedOutputStream(this.outputStream);
            
            outputStreamBuff.write(json.getBytes());
            outputStreamBuff.flush();
        } catch (IOException ex) {
            throw new IOException("Error sending JSON to the server.");
        } catch (Exception ex) {
            throw new Exception("Unknown error sending JSON to the server.");
        }

    }

}
