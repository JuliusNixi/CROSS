package CROSS.Client;

/**
 * 
 * This class is responsible for handling the responses from the server.
 * It's a thread started with the start() method from the Client class.
 * It has an associated client that will be used.
 * 
 * Could be used indipendently from the Client's CLI.
 * 
 * @version 1.0
 * @author Giulio Nisi
 * @see Client
 * 
 */
public class ResponsesThread extends Thread {

    // The client object that will be used with this thread to receive responses from the server.
    private Client client = null;

    /**
     * 
     * Constructor of the class.
     * 
     * @param client The client object that will be used with this thread.
     * 
     * @throws NullPointerException If the client object is null.
     * 
     */
    public ResponsesThread(Client client) throws NullPointerException {

        // Null check.
        if (client == null) {
            throw new NullPointerException("The client object cannot be null.");
        }

        this.client = client;

    }
    

    @Override
    public void run() {

        while (true) {

            System.out.printf("\n ECCOMI SONO UNA NOTIFICA / RISPOSTA DAL SERVER \n");

            try {
                Thread.sleep(3000 * 3);
            } catch (InterruptedException ex) {
                // TODO: Handle this exception.
            }

        }

    }


}
