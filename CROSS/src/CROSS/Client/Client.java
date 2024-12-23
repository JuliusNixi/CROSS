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

    /**
     * Constructor of the client.
     * 
     * @param pathToConfigPropertiesFile Path to the client config file.
     * @throws NullPointerException If the path to the client config file is null.
     */
    public Client(String pathToConfigPropertiesFile) throws NullPointerException {

        if (pathToConfigPropertiesFile == null) {
            throw new NullPointerException("Path to client config file is null.");
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

            System.out.print(this);

        // Throwed by getByName.
        }catch (UnknownHostException ex) {
            System.err.printf("Unknown server host reading file %s.\n", pathToConfigPropertiesFile);
            Thread.currentThread().interrupt();   
        }

        // Throwed by FileReader.
        catch (FileNotFoundException ex) {
            System.err.printf("File %s not found.\n", pathToConfigPropertiesFile);
            Thread.currentThread().interrupt();
        }

        // parseInt exception.
        catch (NumberFormatException ex) {
            System.err.printf("Port number format error reading file %s.\n", pathToConfigPropertiesFile);
            Thread.currentThread().interrupt();
        }
        
        // Throwed by Properties.load().
        catch (IllegalArgumentException ex) {
            System.err.printf("Illegal argument reading file %s.\n", pathToConfigPropertiesFile);
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            System.err.printf("I/O error reading file %s.\n", pathToConfigPropertiesFile);
            Thread.currentThread().interrupt();
        }
        catch (NullPointerException ex) {
            System.err.printf("Null pointer reading file %s.\n", pathToConfigPropertiesFile);
            Thread.currentThread().interrupt();
        } 

        // InvalidConfig exception.
        catch (InvalidConfig ex) {
            System.err.printf("Invalid configuration file %s.\n", pathToConfigPropertiesFile);
            Thread.currentThread().interrupt();
        }

        // Generic exception.
        catch (Exception ex) {
            System.err.printf("Generic error reading file %s.\n", pathToConfigPropertiesFile);
            Thread.currentThread().interrupt();
        }

    }
    
    /**
     * Connects the client to the server.
     */
    public void connectClient() {

        try {
            this.socket = new Socket(serverAddress, serverPort);
            // 10 seconds timeout.
            socket.setSoTimeout(10 * 1000);
            this.outputStream = socket.getOutputStream();
        } catch (NullPointerException ex) {
            System.err.println("Null pointer exception in connecting the client to the server.");
            Thread.currentThread().interrupt();
        } catch (IllegalArgumentException ex) {
            System.err.println("Illegal argument in connecting the client to the server.");
            Thread.currentThread().interrupt();
        } catch (UnknownHostException ex) {
            System.err.println("Unknown host in connecting the client to the server.");
            Thread.currentThread().interrupt();
        } catch (SocketException ex) {
            System.err.println("Socket exception in connecting the client to the server.");
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            System.err.println("I/O exception in connecting the client to the server.");
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            System.err.println("Generic exception in connecting the client to the server.");
            Thread.currentThread().interrupt();
        }

    }

    // CLI
    /**
     * Command Line Interface.
     * Static, I cannot have multiple instances of the CLI.
     * Returns the CLI thread.
     * @param client The client.
     * @return ClientCLIThread
     * @throws IOException If the CLI is already started.
     */
    public static ClientCLIThread CLI(Client client) throws IOException {
        
        // Start the CLI.
        if (clientCLI != null) {
            throw new IOException("CLI already started.");
        }

        // Get and start a thread.
        ClientCLIThread clientCLI = new ClientCLIThread(client);
        clientCLI.start();
        Client.clientCLI = clientCLI; 

        return clientCLI;

    }
    public static ClientCLIThread getClientCLI() {
        return clientCLI;
    }    
  
    // GETTERS
    /**
     * Get the path to the client's configuration file.
     * @return String
     */
    public String getPathToConfigPropertiesFile() {
        return pathToConfigPropertiesFile;
    }
    /**
     * Get the server port.
     * @return Integer
     */
    public Integer getServerPort() {
        return serverPort;
    }
    /**
     * Get the server address.
     * @return InetAddress
     */
    public InetAddress getServerAddress() {
        return serverAddress;
    }
    /**
     * Get the socket.
     * @return Socket
     */
    public Socket getSocket() {
        return this.socket;
    }
    /**
     * Get the output stream.
     * @return OutputStream
     */
    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    public String toString() {
        return String.format("Client Info's:\nServer IP: %s.\nServer port: %s.\nConfig file path: %s.\n", this.getServerAddress(), this.getServerPort(), this.getPathToConfigPropertiesFile());
    }

    /**
     * 
     * Send a JSON to the server.
     * @param json The JSON to send to the server.
     * @throws NullPointerException If the socket or the output stream are null.
     */
    public void sendJSONToServer(String json) throws NullPointerException {
        if (this.socket == null) {
            throw new NullPointerException("Socket is null.");
        }
        if (this.outputStream == null) {
            throw new NullPointerException("Output stream is null.");
        }
        try {
            // Buffered to optimize the performance.
            BufferedOutputStream outputStreamBuff = new BufferedOutputStream(this.outputStream);
            outputStreamBuff.write(json.getBytes());
            outputStreamBuff.flush();
        } catch (IOException ex) {
            System.err.println("I/O error sending JSON to the server.");
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            System.err.println("Generic error sending JSON to the server.");
            Thread.currentThread().interrupt();
        }
    }

}
