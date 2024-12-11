package CROSS.Exceptions;

// Throwed when the IP or port are invalid (readed from config file) for server or client.
public class InvalidIPOrPort extends Exception {
    public InvalidIPOrPort(String message) {
        super(message);
    }   
}
