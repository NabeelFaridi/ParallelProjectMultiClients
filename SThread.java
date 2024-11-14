import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class SThread implements Runnable {
	private ConcurrentHashMap<String, Socket> RTable; // Routing table shared across instances
	private Socket clientSocket;
	private String addr;

	public SThread(ConcurrentHashMap<String, Socket> Table, Socket toClient) throws IOException {
		this.RTable = Table;
		this.clientSocket = toClient;
		this.addr = toClient.getInetAddress().getHostAddress();
		RTable.put(addr, toClient); // Add client to routing table
	}

	@Override
	public void run() {
		try (
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())
		) {
			// Step 1: Receive matrices from the client
			int[][] matrixA = (int[][]) in.readObject();
			int[][] matrixB = (int[][]) in.readObject();

			// Step 2: Connect to secondary server to perform matrix operations
			Socket serverSocket = new Socket("localhost", 12346);
			ObjectOutputStream serverOut = new ObjectOutputStream(serverSocket.getOutputStream());
			serverOut.writeObject(matrixA);
			serverOut.writeObject(matrixB);

			// Step 3: Receive results and performance metrics from the server
			ObjectInputStream serverIn = new ObjectInputStream(serverSocket.getInputStream());
			int[][] result = (int[][]) serverIn.readObject();
			long parallelExecutionTime = serverIn.readLong();
			double speedUp = serverIn.readDouble();
			double efficiency = serverIn.readDouble();

			// Step 4: Send result and metrics back to the client
			out.writeObject(result);
			out.writeLong(parallelExecutionTime);
			out.writeDouble(speedUp);
			out.writeDouble(efficiency);

			// Clean up
			RTable.remove(addr); // Remove client from routing table
			serverSocket.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
