import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class TCPServerRouter {
    private static final int ROUTER_PORT = 12345;
    private ConcurrentHashMap<String, Socket> RTable = new ConcurrentHashMap<>(); // Dynamic routing table

    public static void main(String[] args) {
        TCPServerRouter router = new TCPServerRouter();
        router.startRouter();
    }

    public void startRouter() {
        try (ServerSocket serverSocket = new ServerSocket(ROUTER_PORT)) {
            System.out.println("Server Router is running on port " + ROUTER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a new SThread with the routing table and client socket
                SThread thread = new SThread(RTable, clientSocket);
                new Thread(thread).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
