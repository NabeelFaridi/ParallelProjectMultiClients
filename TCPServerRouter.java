import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class TCPServerRouter {
    private static final int ROUTER_PORT = 12345;
    private ConcurrentHashMap<Integer, Socket> connectionMap = new ConcurrentHashMap<>();

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

                // Route the client to a server using SThread
                SThread thread = new SThread(clientSocket, this);
                new Thread(thread).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addConnection(int clientId, Socket serverSocket) {
        connectionMap.put(clientId, serverSocket);
    }

    public Socket getConnection(int clientId) {
        return connectionMap.get(clientId);
    }

    public void removeConnection(int clientId) {
        connectionMap.remove(clientId);
    }
}
