package cross.client;

import cross.api.responses.Response;
import cross.api.responses.orders.CancelResponse;
import cross.api.responses.orders.ExecutionResponse;
import cross.api.responses.pricehistory.PriceHistoryResponse;
import cross.api.responses.user.UserResponse;
import cross.utils.ClientActionsUtils.ClientActions;
import java.io.BufferedInputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 *
 * This class is responsible for handling the responses from the server, so to print them on the console.
 * It's a dedicated thread, started with the responsesStart() method from the Client class.
 *
 * It has an associated client that will be used to read from the TCP socket the responses.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Client
 * 
 * @see Response
 * @see CancelResponse
 * @see ExecutionResponse
 * @see UserResponse
 * 
 * @see ClientActions
 * 
 */
class ResponsesThread extends Thread {

    // The client object that will be used with this thread to receive responses from the server.
    private final Client client;

    /**
     *
     * Constructor of the class.
     *
     * @param client The client object that will be used with this thread to receive responses from the server.
     *
     * @throws NullPointerException If the client object is null.
     *
     */
    public ResponsesThread(Client client) throws NullPointerException {

        // Null check.
        if (client == null) {
            throw new NullPointerException("The client object to use to receive responses from the server cannot be null.");
        }

        this.client = client;

    }

    @Override
    public void run() {

        BufferedInputStream bin;
        try {
            bin = client.getBufferedInputStream();
        } catch (IllegalStateException ex) {
            // Since this is a dedicated thread, I don't backward the exception.
            System.err.println("The client is not connected to the server. No responses from the server will be received.");
            // Terminate the thread.
            return;
        }

        try (Scanner scanner = new Scanner(bin)) {
            
            String JSONresponse;
            while (!Thread.currentThread().isInterrupted() && scanner.hasNextLine()) {
                
                try {
                    // JSON responses from the server are all '\n' terminated.
                    JSONresponse = scanner.nextLine();
                } catch (NoSuchElementException ex) {
                    // Trying to continue the thread ignoring the exception.
                    continue;
                }
                
                Response response;
                try {
                    response = new Response(JSONresponse);
                } catch (IllegalArgumentException ex) {
                    // Since this is a dedicated thread, I don't backward the exception.
                    System.err.println("The JSON response received from the server is not valid. Continuing...");
                    continue;
                }

                System.out.printf("\nServer response -> ");
                switch (response.getType()) {
                    // All these cases are for the user's data requests and handled in the same way.
                    case REGISTER, LOGIN, UPDATE_CREDENTIALS, LOGOUT, CANCEL_ORDER -> {
                        Integer code;
                        String message;
                        if (response.getType() == ClientActions.CANCEL_ORDER) {
                            CancelResponse cancelResponse = (CancelResponse) response.getResponse();
                            code = cancelResponse.getResponseCode().getCode();
                            message = cancelResponse.getMessage();
                        }else {
                            UserResponse userResponse = (UserResponse) response.getResponse();
                            code = userResponse.getResponseCode().getCode();
                            message = userResponse.getMessage();
                        }
                        if (code == 100) {
                            System.out.println("Code: " + code + " Message: " + message);
                        } else {
                            System.err.println("Code: " + code + " Message: " + message);
                        }
                    }
                    // Limit and stop orders execution responses are also handled here for simplicity.
                    // Since they have the same response format with only the orderId.
                    case INSERT_MARKET_ORDER -> {
                        ExecutionResponse executionResponse = (ExecutionResponse) response.getResponse();
                        Number orderId = executionResponse.getOrderId();
                        if (orderId.intValue() == -1) {
                            System.err.println("Error. Order ID: " + orderId);
                        } else {
                            System.out.println("OK. Order ID: " + orderId);
                            client.addExecutedOrder(Long.valueOf(orderId.longValue()));
                        }
                    }
                    case GET_PRICE_HISTORY -> {
                        PriceHistoryResponse priceHistoryResponse = (PriceHistoryResponse) response.getResponse();
                        System.out.println("\n" + priceHistoryResponse.toString());
                    }
                    default -> System.err.println("The received response type got from the server is not valid.");
                }

                System.out.print("Client CLI -> ");

            }
        
            System.out.println("\nServer closed the connection.");
            System.exit(0);

        }

    }
    
}
