public class Main {
    public static void main(String[] args) {
        // Set thread count for the server (can be configured as needed)
        int serverThreadCount = 1;

        // Start the Router
        Thread routerThread = new Thread(() -> {
            TCPServerRouter router = new TCPServerRouter();
            router.startRouter();
        });
        routerThread.start();
        System.out.println("TCPServerRouter started...");

        // Start the Server with the specified thread count
        Thread serverThread = new Thread(() -> {
            TCPServer server = new TCPServer(serverThreadCount);
            server.startServer();
        });
        serverThread.start();
        System.out.println("TCPServer started with " + serverThreadCount + " threads...");

        // Optionally, start multiple clients for testing purposes
        int numberOfClients = 10; // Number of client instances to start
        for (int i = 0; i < numberOfClients; i++) {
            final int clientId = i + 1;
            new Thread(() -> {
                TCPClient client = new TCPClient();
                System.out.println("Starting TCPClient #" + clientId);
                client.startClient();
            }).start();
        }
    }
}
