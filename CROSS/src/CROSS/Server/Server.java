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
import CROSS.Exceptions.InvalidMaxConnections;

public class Server {

    private String pathToConfigPropertiesFile = null;
    private Integer portInt = null;
    private Integer maxConnectionsInt = null;
    private InetAddress serverAddress = null;

    private ServerSocket serverSocket = null;

    private AcceptThread acceptThread = null;

    public Server(String pathToConfigPropertiesFile) {

        File configFile = new File(pathToConfigPropertiesFile);
        Properties props = new Properties();
        
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
            this.portInt = Integer.parseInt(port);
            if (portInt < 0 || portInt > 65535) {
                throw new InvalidConfig("Invalid port number.");
            }
            this.serverAddress = InetAddress.getByName(server);

            // Max connections checks.
            if (maxConnections == null) {
                maxConnections = "100";
                System.out.printf("Max connections not set in %s file. Using the default one: %s.\n", pathToConfigPropertiesFile, maxConnections);
            }
            this.maxConnectionsInt = Integer.parseInt(maxConnections);
            if (maxConnectionsInt < 1) {
                throw new InvalidMaxConnections("Invalid max connections number.");
            }

            this.pathToConfigPropertiesFile = pathToConfigPropertiesFile;

        // Throwed by getByName.
        }catch (UnknownHostException ex) {
            System.err.printf("Unknown server host reading file %s.\n", pathToConfigPropertiesFile);
        }

        // Throwed by FileReader.
        catch (FileNotFoundException ex) {
            System.err.printf("File %s not found.\n", pathToConfigPropertiesFile);
        }

        // parseInt exception.
        catch (NumberFormatException ex) {
            System.err.printf("Port number or max connections format error reading file %s.\n", pathToConfigPropertiesFile);
        }
        
        // Throwed by Properties.load().
        catch (IllegalArgumentException ex) {
            System.err.printf("Illegal argument reading file %s.\n", pathToConfigPropertiesFile);
        } catch (IOException ex) {
            System.err.printf("I/O error reading file %s.\n", pathToConfigPropertiesFile);
        }
        catch (NullPointerException ex) {
            System.err.printf("Null pointer reading file %s.\n", pathToConfigPropertiesFile);
        } 

        // InvalidIPOrPort exception.
        catch (InvalidConfig ex) {
            System.err.printf("Invalid server IP or port reading file %s.\n", pathToConfigPropertiesFile);
        }
        // InvalidMaxConnections exception.
        catch (InvalidMaxConnections ex) {
            System.err.printf("Invalid server max connections reading file %s.\n", pathToConfigPropertiesFile);
        }

        // Generic exception.
        catch (Exception ex) {
            System.err.printf("Generic error reading file %s.\n", pathToConfigPropertiesFile);
        }
        
    } // End of constructor.

    public void startServer(){
        if (this.serverSocket != null) {
            System.err.printf("Server already started.\n");
            return;
        }
        System.out.printf("Starting server with these following args...\n%s", this.toString());
        try {
            // Start the server.
            this.serverSocket = new ServerSocket(portInt, maxConnectionsInt, serverAddress);
            System.out.printf("Server started succesfully.\n");
        } catch (IOException ex) {
            System.err.printf("Error IOException starting server.\n");
        }
        // Generic exception.
        catch (Exception ex) {
            System.err.printf("Generic error during the creation of the socket.\n");
        }
    }

    // Returns the thread that accepts clients.
    public AcceptThread startAccept() throws IOException {
        if (this.serverSocket == null) {
            throw new IOException("Server socket is null.");
        }
        if (this.acceptThread != null) {
            throw new IOException("Accept thread already started.");
        }
        AcceptThread acceptThread = new AcceptThread(this);
        acceptThread.start();
        this.acceptThread = acceptThread;
        return acceptThread;
    }
  
   public String getPathToConfigPropertiesFile() {
        return pathToConfigPropertiesFile;
    }
    public Integer getPortInt() {
        return portInt;
    }
    public Integer getMaxConnectionsInt() {
        return maxConnectionsInt;
    }
    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public Thread getAcceptThread()  {
        return acceptThread;
    }

    @Override
    public String toString() {
        return String.format("Server IP: %s.\nServer port: %s.\nMax connections: %s.\nConfig file path: %s.\n", this.getServerAddress(), this.getPortInt(), this.getMaxConnectionsInt(), this.getPathToConfigPropertiesFile());
    }
    
}

