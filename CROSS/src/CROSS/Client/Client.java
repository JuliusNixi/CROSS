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

import CROSS.Exceptions.InvalidIPOrPort;

// The idea is i can have multiple instances of the client, but only one CLI.
public class Client {

    // Thread for the CLI.
    private static ClientCLI clientCLI = null;

    private String pathToConfigPropertiesFile = null;
    private Integer portInt = null;
    private InetAddress serverAddress = null;

    private Socket socket = null;

    private OutputStream outputStream = null;

    public Client(String pathToConfigPropertiesFile) {

        File configFile = new File(pathToConfigPropertiesFile);
        Properties props = new Properties();
        
        try (FileReader reader = new FileReader(configFile)) {
            
            // Read the properties file.
            props.load(reader);
            String server = props.getProperty("server_ip");
            String port = props.getProperty("server_port");

            if (server == null || port == null) {
                throw new InvalidIPOrPort("Invalid server IP or port.");
            }

            // Parsing IP and port.
            this.portInt = Integer.parseInt(port);
            if (portInt < 0 || portInt > 65535) {
                throw new InvalidIPOrPort("Invalid port number.");
            }
            this.serverAddress = InetAddress.getByName(server);

            this.pathToConfigPropertiesFile = pathToConfigPropertiesFile;

            System.out.print(this);

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
            System.err.printf("Port number format error reading file %s.\n", pathToConfigPropertiesFile);
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
        catch (InvalidIPOrPort ex) {
            System.err.printf("Invalid server IP or port reading file %s.\n", pathToConfigPropertiesFile);
        }

        // Generic exception.
        catch (Exception ex) {
            System.err.printf("Generic error reading file %s.\n", pathToConfigPropertiesFile);
        }

    }
    
    // Connect to the server.
    // Does not return anything.
    public void connectClient() {

        try {
            this.socket = new Socket(serverAddress, portInt);
            // 10 seconds timeout.
            socket.setSoTimeout(10 * 1000);
            this.outputStream = socket.getOutputStream();
        } catch (UnknownHostException ex) {
            // TODO: Error handling.
        } catch (IOException ex) {
            // TODO: Error handling.
        }

    }

    // Command Line Interface.
    // Static, i cannot have multiple instances of the CLI.
    // Returns the CLI thread.
    public static ClientCLI CLI(Client client) throws IOException {
        
        // Start the CLI.
        if (clientCLI != null) {
            throw new IOException("CLI already started.");
        }

        // Get and start a thread.
        ClientCLI clientCLI = new ClientCLI(client);
        clientCLI.start();
        Client.clientCLI = clientCLI; 

        return clientCLI;

    }
    public static ClientCLI getClientCLI() {
        return clientCLI;
    }    
  
    public String getPathToConfigPropertiesFile() {
        return pathToConfigPropertiesFile;
    }
    public Integer getPortInt() {
        return portInt;
    }
    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public Socket getSocket() {
        return this.socket;
    }

    @Override
    public String toString() {
        return String.format("Client Info's:\nServer IP: %s.\nServer port: %s.\nConfig file path: %s.\n", this.getServerAddress(), this.getPortInt(), this.getPathToConfigPropertiesFile());
    }

    public void sendJSON(String json) throws NullPointerException, IOException {
        if (this.socket == null) {
            throw new NullPointerException("Socket is null.");
        }
        if (this.outputStream == null) {
            throw new NullPointerException("Output stream is null.");
        }
        try {
            BufferedOutputStream outputStreamBuff = new BufferedOutputStream(this.outputStream);
            outputStreamBuff.write(json.getBytes());
            outputStreamBuff.flush();
        } catch (IOException ex) {
            throw new IOException("Error sending JSON.");
        }
    }

}
