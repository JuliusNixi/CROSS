package CROSS.Exceptions;

// Throwed when the IP or port are invalid (readed from config file).
public class InvalidIPOrPort extends Exception {
    public InvalidIPOrPort(String message) {
        super(message);
    }   
}
