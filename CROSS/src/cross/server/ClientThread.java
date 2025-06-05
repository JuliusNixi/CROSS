package cross.server;

import cross.api.requests.Request;
import cross.api.requests.orders.CancelRequest;
import cross.api.requests.orders.CreateRequest;
import cross.api.requests.pricehistory.PriceHistoryRequest;
import cross.api.requests.user.RegisterLoginRequest;
import cross.api.requests.user.UpdateCredentialsRequest;
import cross.api.responses.Response;
import cross.api.responses.ResponseCode;
import cross.api.responses.ResponseCode.ResponseContent;
import cross.api.responses.ResponseCode.ResponseType;
import cross.api.responses.orders.CancelResponse;
import cross.api.responses.orders.ExecutionResponse;
import cross.api.responses.pricehistory.PriceHistoryResponse;
import cross.api.responses.user.UserResponse;
import cross.exceptions.InvalidOrder;
import cross.exceptions.InvalidUser;
import cross.orderbook.OrderBook;
import cross.orders.LimitOrder;
import cross.orders.MarketOrder;
import cross.orders.Order;
import cross.orders.StopOrder;
import cross.orders.db.Orders;
import cross.types.Currency;
import cross.types.Quantity;
import cross.types.price.PriceType;
import cross.types.price.SpecificPrice;
import cross.users.User;
import cross.users.db.Users;
import cross.utils.ClientActionsUtils;
import cross.utils.ClientActionsUtils.ClientActions;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 *
 * This class rapresent a thread that will handle a specific client.
 * Each client will have its own dedicated thread with this class that will serve its requests.
 *
 * This thread is started after a new client connection acceptance by the AcceptThread class.
 * This thread is then submitted to CachedThreadPool to be executed by the AcceptThread.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see AcceptThread
 *
 */
class ClientThread implements Runnable {

    // Specific client's socket. Will not change, so it's final.
    private final Socket clientSocket;

    // Output and input streams for the client's socket.
    private InputStream in = null;
    private OutputStream out = null;

    // Buffered streams for the client's socket. To optimize performance.
    private BufferedInputStream bin = null;
    private BufferedOutputStream bout = null;


    /**
     *
     * Constructor of the class.
     *
     * @param clientSocket The socket of the client that this thread will handle.
     * @param server The server that this thread belongs to.
     *
     * @throws NullPointerException If the client's socket is null.
     *
     */
    public ClientThread(Socket clientSocket) throws NullPointerException {


        // Null check.
        if (clientSocket == null)
            throw new NullPointerException("Client's socket in the client's thread cannot be null.");

        this.clientSocket = clientSocket;

    }

    // Main loop for client's actions requests handling logic.
    @Override
    public void run() {

        System.out.printf("%s thread started successfully.\n", this.toString());

        // Input from extern to our server.
        // Output from our server to extern.
        // UTF-8 is the default encoding.

        // Getting input and output streams.
        try {
            this.in = this.clientSocket.getInputStream();
            this.out = this.clientSocket.getOutputStream();
            this.bin = new BufferedInputStream(this.in);
            this.bout = new BufferedOutputStream(this.out);
        }catch (IOException ex) {

            // This is a dediacted thread, so I don't backward the exception, instead I print it and I try to continue with the whole program, but the thread will be terminated.

            System.err.printf("Error while getting input and output streams from client %s:%s. Closing this connection and terminating the thread...\n", this.getClientIP(), this.getClientPort());

            // Always close the buffered input (the outer) stream first.
            if (this.bin != null) {
                try {
                    this.bin.close();
                }catch (IOException ex2) {
                    System.err.printf("Error while closing buffered input stream from client %s:%s.\n", this.getClientIP(), this.getClientPort());
                }
            }else{
                // Maybe an error occurred while creating the buffered input stream, but the input stream is still valid.
                if (this.in != null) {
                    try {
                        this.in.close();
                    }catch (IOException ex2) {
                        System.err.printf("Error while closing input stream from client %s:%s.\n", this.getClientIP(), this.getClientPort());
                    }
                }
            }

            if (this.bout != null) {
                try {
                    this.bout.close();
                }catch (IOException ex2) {
                    System.err.printf("Error while closing buffered output stream from client %s:%s.\n", this.getClientIP(), this.getClientPort());
                }
            }else{
                // Maybe an error occurred while creating the buffered output stream, but the output stream is still valid.
                if (this.out != null) {
                    try {
                        this.out.close();
                    }catch (IOException ex2) {
                        System.err.printf("Error while closing output stream from client %s:%s.\n", this.getClientIP(), this.getClientPort());
                    }
                }
            }

            try {
                if (this.clientSocket != null)
                    this.clientSocket.close();
            }catch (IOException ex2) {
                System.err.printf("Error while closing client socket %s:%s.\n", this.getClientIP(), this.getClientPort());
            }

            // Terminate the thread.
            return;

        }

        Scanner scanner = null;
        // No exception should be thrown here, just to avoid the compiler warning.
        try {
            scanner = new Scanner(this.bin);
        } catch (Exception ex) {}

        while (scanner != null && scanner.hasNextLine()) {

            Boolean exit = false;
            String data = null;
            try {
                // JSONs sent (also the ones received from the client) are always '\n' terminated.
                data = scanner.nextLine();

                if (data == null || data.isEmpty() || data.isBlank()) {
                    throw new NoSuchElementException("");
                }
            } catch (NoSuchElementException ex) {
                // Nothing received or '\n' NOT received.
                // Ignoring request.
                continue;
            }

            System.out.println("DEBUG: " + data);

            // TODO: FINISH: Decode the JSON. Handle the request as server. Encode the response. Send the response to the client. Remove this exit command.
            Request req = null;
            ClientActions action = null;
            Response response = null;
            User user = null;
            RegisterLoginRequest registerLoginRequest = null;
            PriceType priceType = null;
            SpecificPrice specificPrice = null;
            Quantity quantity = null;
            CreateRequest createRequest = null;
            OrderBook orderBook = OrderBook.getOrderBookByCurrencies(Currency.getDefaultPrimaryCurrency(), Currency.getDefaultSecondaryCurrency());
            if (orderBook == null) {
                orderBook = OrderBook.getMainOrderBook();
            }
            Currency primaryCurrency = orderBook.getPrimaryCurrency();
            if (primaryCurrency == null) {
                primaryCurrency = Currency.getDefaultPrimaryCurrency();
            }
            Currency secondaryCurrency = orderBook.getSecondaryCurrency();
            if (secondaryCurrency == null) {
                secondaryCurrency = Currency.getDefaultSecondaryCurrency();
            }
            ResponseContent responseContent = null;
            ResponseType responseType = null;
            ClientActions clientAction = null;
            ResponseCode responseCode = null;
            UserResponse userResponse = null;
            ExecutionResponse executionResponse = null;
            try {
                req = new Request(data, true);
                action = ClientActionsUtils.actionFromString(req.getOperation());
            } catch (IllegalArgumentException | IllegalStateException | NullPointerException | InvalidUser ex) {
                System.out.println("Invalid request received from client.");
                continue;
            }
            switch (action) {
                case REGISTER:
                    responseType = ResponseType.REGISTER;
                    clientAction = ClientActions.REGISTER;
                    try {
                        req = new Request(data, false);
                        action = ClientActionsUtils.actionFromString(req.getOperation());
                        try {
                            registerLoginRequest = (RegisterLoginRequest) req.getValues();
                            user = registerLoginRequest.getUser();
                            Users.addUser(user);
                            responseContent = ResponseContent.OK;
                        }catch (InvalidUser ex) {
                            responseContent = ResponseContent.USERNAME_NOT_AVAILABLE;
                        } catch (NoSuchMethodException ex) {
                            responseContent = ResponseContent.OTHER_ERROR;
                        } catch (IOException ex) {
                            responseContent = ResponseContent.OTHER_ERROR;
                        } catch (IllegalStateException ex) {
                            responseContent = ResponseContent.OTHER_ERROR;
                        }
                    } catch (IllegalArgumentException | IllegalStateException | NullPointerException ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    } catch (InvalidUser ex) {
                        responseContent = ResponseContent.INVALID_PASSWORD;
                    }
                    responseCode = new ResponseCode(responseType, responseContent);
                    userResponse = new UserResponse(responseCode, responseCode.getDefaultMessage());
                    response = new Response(userResponse, clientAction);

                    break;
                case LOGIN:
                    responseType = ResponseType.LOGIN;
                    clientAction = ClientActions.LOGIN;
                    try {
                        req = new Request(data, false);
                        action = ClientActionsUtils.actionFromString(req.getOperation());
                        try {
                            registerLoginRequest = (RegisterLoginRequest) req.getValues();
                            user = registerLoginRequest.getUser();
                            Users.login(user, this.clientSocket);
                            responseContent = ResponseContent.OK;
                        }catch (InvalidUser ex) {
                            responseContent = ResponseContent.INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST;
                        } catch (IllegalStateException ex) {
                            responseContent = ResponseContent.OTHER_ERROR;
                        } catch (IllegalAccessException  ex) {
                            responseContent = ResponseContent.USER_ALREADY_LOGGED_IN;
                        }
                    } catch (IllegalArgumentException | IllegalStateException | NullPointerException ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    } catch (InvalidUser ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    }
                    responseCode = new ResponseCode(responseType, responseContent);
                    userResponse = new UserResponse(responseCode, responseCode.getDefaultMessage());
                    response = new Response(userResponse, clientAction);

                    break;
                case UPDATE_CREDENTIALS:
                    responseType = ResponseType.UPDATE_CREDENTIALS;
                    clientAction = ClientActions.UPDATE_CREDENTIALS;
                    try {
                        req = new Request(data, false);
                        action = ClientActionsUtils.actionFromString(req.getOperation());
                        try {
                            UpdateCredentialsRequest updateCredentialsRequest = (UpdateCredentialsRequest) req.getValues();
                            User userOld = updateCredentialsRequest.getUserOld();
                            User userNew = updateCredentialsRequest.getUser();
                            if (Users.isLoggedIn(this.clientSocket) == true) {
                                responseContent = ResponseContent.USER_CURRENTLY_LOGGED_IN;
                            } else {
                                Users.updateUser(userOld, userNew);
                                responseContent = ResponseContent.OK;
                            }
                        }catch (InvalidUser | IllegalAccessException ex) {
                            responseContent = ResponseContent.INVALID_USERNAME_PASSWORD_MATCH_OR_USERNAME_NOT_EXIST;
                        } catch (NoSuchMethodException ex) {
                            responseContent = ResponseContent.OTHER_ERROR;
                        } catch (IOException ex) {
                            responseContent = ResponseContent.OTHER_ERROR;
                        } catch (IllegalStateException ex) {
                            responseContent = ResponseContent.OTHER_ERROR;
                        } catch (IllegalArgumentException ex) {
                            responseContent = ResponseContent.OTHER_ERROR;
                        } catch (SecurityException ex) {
                            responseContent = ResponseContent.NEW_PASSWORD_EQUAL_OLD;
                        } catch (RuntimeException ex) {
                            responseContent = ResponseContent.OTHER_ERROR;
                        }
                    } catch (IllegalArgumentException | IllegalStateException | NullPointerException ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    } catch (InvalidUser ex) {
                        responseContent = ResponseContent.INVALID_NEW_PASSWORD;
                    }
                    responseCode = new ResponseCode(responseType, responseContent);
                    userResponse = new UserResponse(responseCode, responseCode.getDefaultMessage());
                    response = new Response(userResponse, clientAction);

                    break;
                case LOGOUT:
                    responseType = ResponseType.LOGOUT;
                    clientAction = ClientActions.LOGOUT;
                    try {
                        req = new Request(data, false);
                        action = ClientActionsUtils.actionFromString(req.getOperation());
                        try {
                            // LogoutRequest logoutRequest = (LogoutRequest) req.getValues();
                            if (Users.isLoggedIn(this.clientSocket)) {
                                user = Users.getLoggedInUser(this.clientSocket);
                                // the user log out but could login back, the tcp connection is still open.
                                Users.logout(user, this.clientSocket);
                                responseContent = ResponseContent.OK;
                            } else {
                                responseContent = ResponseContent.USER_NOT_LOGGED_IN_OR_OTHER_ERROR;
                            }
                        }catch (IllegalArgumentException ex) {
                            responseContent = ResponseContent.USER_NOT_LOGGED_IN_OR_OTHER_ERROR;
                        }
                    } catch (IllegalArgumentException | IllegalStateException | NullPointerException ex) {
                        responseContent = ResponseContent.USER_NOT_LOGGED_IN_OR_OTHER_ERROR;
                    } catch (InvalidUser ex) {
                        responseContent = ResponseContent.USER_NOT_LOGGED_IN_OR_OTHER_ERROR;
                    }
                    responseCode = new ResponseCode(responseType, responseContent);
                    userResponse = new UserResponse(responseCode, responseCode.getDefaultMessage());
                    response = new Response(userResponse, clientAction);
                    
                    break;  
                case INSERT_MARKET_ORDER:
                    try {
                        req = new Request(data, false);
                        action = ClientActionsUtils.actionFromString(req.getOperation());
                    } catch (IllegalArgumentException | IllegalStateException | NullPointerException ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    } catch (InvalidUser ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    }
                    createRequest = (CreateRequest) req.getValues();
                    MarketOrder marketOrder = null;
                    priceType = createRequest.getType();
                    specificPrice = createRequest.getPrice();
                    quantity = createRequest.getSize();
                    marketOrder = new MarketOrder(priceType, primaryCurrency, secondaryCurrency, quantity);

                    try {
                        if (Users.isLoggedIn(clientSocket)) {
                            User userLogged = Users.getLoggedInUser(clientSocket);
                            marketOrder.setUser(userLogged);
                            Boolean executed = false;
                            if (marketOrder.getQuantity().getValue() > 0) {
                                try {
                                    executed = orderBook.executeOrder(marketOrder);
                                } catch (InvalidOrder ex) {
                                    marketOrder.setId(-1);
                                }
                                System.out.println("DEBUG: Market order executed: " + executed);
                                if (!executed) {
                                    marketOrder.setId(-1);
                                }
                            } else {
                                marketOrder.setId(-1);
                            }
                        } else {
                            marketOrder.setId(-1);
                        }
                    } catch (IllegalArgumentException ex) {
                    } catch (IllegalStateException ex) {
                    } catch (NullPointerException ex) {
                    }
                    
                    executionResponse = new ExecutionResponse(marketOrder);
                    response = new Response(executionResponse, clientAction);

                    break;
                case INSERT_LIMIT_ORDER:
                    try {
                        req = new Request(data, false);
                        action = ClientActionsUtils.actionFromString(req.getOperation());
                    } catch (IllegalArgumentException | IllegalStateException | NullPointerException ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    } catch (InvalidUser ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    }
                    createRequest = (CreateRequest) req.getValues();
                    LimitOrder limitOrder = null;
                    priceType = createRequest.getType();
                    specificPrice = createRequest.getPrice();
                    quantity = createRequest.getSize();

                    try {
                        limitOrder = new LimitOrder(specificPrice, quantity, false);
                        if (Users.isLoggedIn(clientSocket)) {
                            User userLogged = Users.getLoggedInUser(clientSocket);
                            limitOrder.setUser(userLogged);
                            if (limitOrder.getQuantity().getValue() > 0) {
                                orderBook.executeOrder(limitOrder);
                            } else {
                                limitOrder.setId(-1);
                            }
                        } else {
                            limitOrder.setId(-1);
                        }
                    } catch (IllegalArgumentException ex) {
                        limitOrder = new LimitOrder(specificPrice, quantity, true);
                        limitOrder.setId(-1);
                    } catch (IllegalStateException ex) {
                        limitOrder = new LimitOrder(specificPrice, quantity, true);
                        limitOrder.setId(-1);
                    } catch (NullPointerException ex) {
                        limitOrder = new LimitOrder(specificPrice, quantity, true);
                        limitOrder.setId(-1);
                    }
                    
                    executionResponse = new ExecutionResponse(limitOrder);
                    response = new Response(executionResponse, clientAction);

                    break;
                case INSERT_STOP_ORDER:
                    try {
                        req = new Request(data, false);
                        action = ClientActionsUtils.actionFromString(req.getOperation());
                    } catch (IllegalArgumentException | IllegalStateException | NullPointerException ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    } catch (InvalidUser ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    }
                    createRequest = (CreateRequest) req.getValues();
                    StopOrder stopOrder = null;
                    priceType = createRequest.getType();
                    specificPrice = createRequest.getPrice();
                    quantity = createRequest.getSize();

                    try {
                        stopOrder = new StopOrder(specificPrice, quantity, false);
                        if (Users.isLoggedIn(clientSocket)) {
                            User userLogged = Users.getLoggedInUser(clientSocket);
                            stopOrder.setUser(userLogged);
                            if (stopOrder.getQuantity().getValue() > 0) {
                                orderBook.executeOrder(stopOrder);
                            } else {
                                stopOrder.setId(-1);
                            }
                        } else {
                            stopOrder.setId(-1);
                        }
                    } catch (IllegalArgumentException ex) {
                        stopOrder = new StopOrder(specificPrice, quantity, true);
                        stopOrder.setId(-1);
                    } catch (IllegalStateException ex) {
                        stopOrder = new StopOrder(specificPrice, quantity, true);
                        stopOrder.setId(-1);
                    } catch (NullPointerException ex) {
                        stopOrder = new StopOrder(specificPrice, quantity, true);
                        stopOrder.setId(-1);
                    }
                    
                    executionResponse = new ExecutionResponse(stopOrder);
                    response = new Response(executionResponse, clientAction);

                    break;
                case CANCEL_ORDER:
                    try {
                        req = new Request(data, false);
                        action = ClientActionsUtils.actionFromString(req.getOperation());
                    } catch (IllegalArgumentException | IllegalStateException | NullPointerException ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    } catch (InvalidUser ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    }

                    CancelRequest cancelRequest = (CancelRequest) req.getValues();
                    Number orderID = cancelRequest.getOrderId();

                    if (Users.isLoggedIn(clientSocket)) {
                        User userLogged = Users.getLoggedInUser(clientSocket);
                        Order order = orderBook.getOrderById(orderID.longValue());
                        if (order == null) {
                            responseContent = ResponseContent.ORDER_DOES_NOT_EXIST_OR_BELONGS_TO_DIFFERENT_USER_OR_HAS_ALREADY_BEEN_FINALIZED_OR_OTHER_ERROR_CASES;
                        } else {
                            if (order.getUser() != null && !order.getUser().equals(userLogged)) {
                                responseContent = ResponseContent.ORDER_DOES_NOT_EXIST_OR_BELONGS_TO_DIFFERENT_USER_OR_HAS_ALREADY_BEEN_FINALIZED_OR_OTHER_ERROR_CASES;
                            } else {
                                Boolean canceled = orderBook.cancelOrder(orderID.longValue());
                                if (canceled) {
                                    responseContent = ResponseContent.OK;
                                } else {
                                    responseContent = ResponseContent.ORDER_DOES_NOT_EXIST_OR_BELONGS_TO_DIFFERENT_USER_OR_HAS_ALREADY_BEEN_FINALIZED_OR_OTHER_ERROR_CASES;
                                }
                            }
                        }
                    } else {
                        responseContent = ResponseContent.ORDER_DOES_NOT_EXIST_OR_BELONGS_TO_DIFFERENT_USER_OR_HAS_ALREADY_BEEN_FINALIZED_OR_OTHER_ERROR_CASES;
                    }

                    responseType = ResponseType.CANCEL_ORDER;
                    responseCode = new ResponseCode(responseType, responseContent);
                    CancelResponse cancelResponse = new CancelResponse(responseCode, responseCode.getDefaultMessage());
                    response = new Response(cancelResponse, clientAction);

                    break;
                case GET_PRICE_HISTORY:
                    try {
                        req = new Request(data, false);
                        action = ClientActionsUtils.actionFromString(req.getOperation());
                    } catch (IllegalArgumentException | IllegalStateException | NullPointerException ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    } catch (InvalidUser ex) {
                        responseContent = ResponseContent.OTHER_ERROR;
                    }

                    PriceHistoryRequest priceHistoryRequest = (PriceHistoryRequest) req.getValues();
                    String month = priceHistoryRequest.getMonth();

                    PriceHistoryResponse priceHistoryResponse = Orders.getPriceHistory(month);
                    response = new Response(priceHistoryResponse, clientAction);

                    break;
                case EXIT:
                    exit = true;
                    break;
            }

            // here to exit before without sending a response in case of exit command.
            if (exit) break;

            try {
                System.out.println("DEBUG: sending response to client " + this.getClientIP() + ":" + this.getClientPort() + " with response " + response.toJSONString());
                System.out.println("DEBUG: limit book: " + orderBook.toStringWithLimitBook());
                System.out.println("DEBUG: stop book: " + orderBook.toStringWithStopBook());
                // System.out.println("DEBUG: orders DB: " + Orders.toStringOrders());
                this.bout.write(response.toJSONString().getBytes());
                this.bout.flush();
            } catch (IOException ex) {
                System.err.printf("Error while sending response to client %s:%s. Continuing...\n", this.getClientIP(), this.getClientPort());
            }


        } // End While.

        // to clean notifications.
        if (Users.isLoggedIn(this.clientSocket)) {
            User user = Users.getLoggedInUser(this.clientSocket);
            Users.logout(user, this.clientSocket);
        }
        Users.closeNotificationSocket(this.clientSocket);

        // Disconnect the client.

        // Clean up.
        Boolean error = false;

        try {
            this.bin.close();
        }catch (IOException ex) {
            error = true;
            System.err.printf("Error while closing buffered input stream from %s:%s.\n", this.getClientIP(), this.getClientPort());
            try {
                this.in.close();
            }catch (IOException ex2) {
                System.err.printf("Error while closing input stream from %s:%s.\n", this.getClientIP(), this.getClientPort());
            }
        }

        try {
            this.bout.close();
        }catch (IOException ex) {
            error = true;
            System.err.printf("Error while closing buffered output stream from %s:%s.\n", this.getClientIP(), this.getClientPort());
            try {
                this.out.close();
            }catch (IOException ex2) {
                System.err.printf("Error while closing output stream from %s:%s.\n", this.getClientIP(), this.getClientPort());
            }
        }

        // No exception should be thrown here.
        if (scanner != null)
            scanner.close();

        try {
            this.clientSocket.close();
        }catch (IOException ex) {
            error = true;
            System.err.printf("Error while closing socket from %s:%s.\n", this.getClientIP(), this.getClientPort());
        }

        if (!error)
            System.out.printf("%s closed all resources successfully.\n", this.toString());

        // Terminate the thread.

    }

    // GETTERS
    /**
     *
     * Getter for the client's IP.
     *
     * @return The client's IP as String.
     *
     */
    public String getClientIP() {

        return String.format("%s", this.clientSocket.getInetAddress().getHostAddress());

    }
    /**
     *
     * Getter for the client's port.
     *
     * @return The client's port as Integer.
     *
     */
    public Integer getClientPort() {

        return this.clientSocket.getPort();

    }

    @Override
    public String toString() {

        return String.format("Client's thread [Thread ID [%s] - IP [%s] - Port [%s]]", Thread.currentThread().threadId(), this.getClientIP(), this.getClientPort());

    }
    
}
