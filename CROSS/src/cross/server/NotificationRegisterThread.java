package cross.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class NotificationRegisterThread extends Thread {

    private final Server server;

    public NotificationRegisterThread(Server server) {
        this.server = server;
    }

    @Override
    public void run() {

        System.out.println("Notification register thread started.");

        DatagramSocket datagramSocket = server.getDatagramSocket();
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength()).toLowerCase().trim();

                // Check if the received message is a registration request.
                if (message.startsWith("register")) {

                    String ipAndPortTCP = message.split(" ")[1];
                    String ipTCP = ipAndPortTCP.split(":")[0];
                    String portTCP = ipAndPortTCP.split(":")[1];
                    int portTCPInt = Integer.parseInt(portTCP);
                    InetAddress inetAddressTCP = InetAddress.getByName(ipTCP);
                    InetSocketAddress socketAddressTCP = new InetSocketAddress(inetAddressTCP, portTCPInt);
                    String parsedIpAndPortTCP = String.format("%s:%d", socketAddressTCP.getAddress(), socketAddressTCP.getPort());

                    InetAddress inetAddressUDP = packet.getAddress();
                    int portUDPInt = packet.getPort();
                    InetSocketAddress socketAddressUDP = new InetSocketAddress(inetAddressUDP, portUDPInt);

                    // Avoid duplicate registrations
                    if (server.getUdpSocketAddressForTcpSocketAddress(parsedIpAndPortTCP) != null) {
                        System.out.printf("Client %s:%d already registered for notifications.\n", socketAddressTCP.getAddress(), socketAddressTCP.getPort());
                        continue;
                    }

                    server.registerClientForNotifications(parsedIpAndPortTCP, socketAddressUDP);
                    System.out.printf("Registered client: %s:%d for notifications binding to %s.\n", socketAddressUDP.getAddress(), socketAddressUDP.getPort(), parsedIpAndPortTCP);

                }
            } catch (Exception ex) {
                // TODO: Handle exception.
                ex.printStackTrace();
            }
        }

    }
    
}
