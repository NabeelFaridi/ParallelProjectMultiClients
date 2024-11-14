import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class TCPServer {
    private static final int SERVER_PORT = 12346;
    private int threadCount;

    // Constructor to initialize the server with a specified thread count
    public TCPServer(int threadCount) {
        this.threadCount = threadCount > 0 ? threadCount : 1; // Default to 1 if invalid
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server running on port " + SERVER_PORT + " with " + threadCount + " threads...");

            // Initialize thread pool with specified number of threads
            ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new StrassenMatrixMultiplication(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
