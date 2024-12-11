package CROSS.Exceptions;

// Throwed when the max connections is invalid (readed from config file) for the server.
public class InvalidMaxConnections extends Exception {
    public InvalidMaxConnections(String message) {
        super(message);
    }
    
}
