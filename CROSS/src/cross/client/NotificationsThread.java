package cross.client;

import cross.api.notifications.Notification;
import cross.api.notifications.Trade;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 *
 * This class is responsible for handling the notifications from the server, so to print them on the console.
 * It's a dedicated thread, started with the notificationsStart() method from the Client class.
 *
 * It has an associated client that will be used to read from the UDP socket the notifications.
 * 
 * The notifications are received from the server with UDP and DatagramSocket.
 *
 * @version 1.0
 * @author Giulio Nisi
 *
 * @see Client
 * 
 */
class NotificationsThread extends Thread {
    
    // The client object that will be used with this thread to receive notifications from the server.
    private final Client client;

    /**
     *
     * Constructor of the class.
     *
     * @param client The client object that will be used with this thread to receive notifications from the server.
     *
     * @throws NullPointerException If the client object is null.
     *
     */
    public NotificationsThread(Client client) throws NullPointerException {

        // Null check.
        if (client == null) {
            throw new NullPointerException("The client object to use to receive notifications from the server cannot be null.");
        }

        this.client = client;

    }

    @Override
    public void run() {

        // Registration for receiving notifications.
        // These REGISTER data is not about UDP, it's about TCP, used to link the TCP socket with the user authentication and the notifications system server-side.
        String registerMessage = String.format("register %s:%d", client.getSocket().getInetAddress().getHostAddress(), client.getSocket().getLocalPort());
        byte[] data = registerMessage.getBytes();
        DatagramPacket regPacket = new DatagramPacket(data, data.length, client.getServerAddress(), client.getServerNotificationsPort());
        try {
            client.getDatagramSocket().send(regPacket);
            System.out.println("Registration message sent to the server for receiving notifications.");
        } catch (IOException ex) {
            // Since this is a dedicated thread, I don't backward the exception.
            System.err.println("Error while sending the registration message to the server for receiving notifications. No notifications will be received.");
            // Terminate the thread.
            return;
        }

        while (!Thread.currentThread().isInterrupted()) {
            
            byte[] buffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            try {
                client.getDatagramSocket().receive(receivePacket);
            } catch (IOException ex) {
                // Since this is a dedicated thread, I don't backward the exception.
                try {
                    Thread.sleep(1000); // Wait a second before trying again.
                } catch (InterruptedException ex2) {
                    // If the thread is interrupted, we exit the loop.
                    return;
                }
                System.err.println("Error while receiving the notifications from the server. Continuing...");
                // Trying to continue the thread ignoring the exception.
                continue;
            }
            
            String JSONnotification = new String(receivePacket.getData(), 0, receivePacket.getLength());
            //System.out.println("DEBUG: JSONnotification " + JSONnotification);
            
            Notification notification;
            try {
                notification = new Notification(JSONnotification);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                // Since this is a dedicated thread, I don't backward the exception.
                System.err.println("The JSON notification received from the server is not valid. Continuing...");
                // Trying to continue the thread ignoring the exception.
                continue;
            }

            Long orderId = null;
            // attemps is needed because the notification may arrive before the order is saved in the executed list.
            // a more elegant solution would be to wait for the order to be saved in the executed list before printing the notification.
            // but that implementation would be more complex and it's not worth it since all is just for a pretty print.
            // this is a simple solution that works, wait 1 second x5 times and try to find the order in the executed list.
            // if the order is not found, it will print the notification without the pretty print.
            Integer attempts = 5;
            while (true) {
                for (Trade trade : notification.getTrades()) {
                    Long cmpOrderId = Long.valueOf(trade.getOrderId().longValue());
                    if (client.containsExecutedOrder(cmpOrderId)) {
                        orderId = trade.getOrderId().longValue();
                        break;
                    }
                }
                if (orderId != null || attempts == 0) break;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
                attempts--;
            }
            synchronized (Client.clientCLI.buffer) {
                System.out.printf("\nServer notification ->\n\n");
                System.out.println(notification.toString(orderId));
                System.out.print("Client CLI -> " + Client.clientCLI.buffer.toString());
            }

        }

    }

}