import java.io.*;
import java.net.Socket;

public class StrassenMatrixMultiplication implements Runnable {
    private Socket socket;

    public StrassenMatrixMultiplication(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            // Receive matrices from the client
            int[][] matrixA = (int[][]) in.readObject();
            int[][] matrixB = (int[][]) in.readObject();

            // Measure parallel execution time
            long startTime = System.nanoTime();
            int[][] result = multiply(matrixA, matrixB); // Perform matrix multiplication
            long endTime = System.nanoTime();

            long parallelExecutionTime = endTime - startTime;

            // Measure sequential execution time for speed-up calculation
            long sequentialExecutionTime = sequentialMultiplyTime(matrixA, matrixB);
            double speedUp = (double) sequentialExecutionTime / parallelExecutionTime;
            double efficiency = speedUp / Runtime.getRuntime().availableProcessors();

            // Send result and metrics back to the client
            out.writeObject(result);
            out.writeLong(parallelExecutionTime);
            out.writeDouble(speedUp);
            out.writeDouble(efficiency);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Strassen's matrix multiplication implementation
    public static int[][] multiply(int[][] A, int[][] B) {
        int n = A.length;
        if (n <= 2) {
            return standardMultiply(A, B); // Base case for small matrices
        }

        int newSize = n / 2;
        int[][] A11 = new int[newSize][newSize];
        int[][] A12 = new int[newSize][newSize];
        int[][] A21 = new int[newSize][newSize];
        int[][] A22 = new int[newSize][newSize];
        int[][] B11 = new int[newSize][newSize];
        int[][] B12 = new int[newSize][newSize];
        int[][] B21 = new int[newSize][newSize];
        int[][] B22 = new int[newSize][newSize];

        splitMatrix(A, A11, A12, A21, A22);
        splitMatrix(B, B11, B12, B21, B22);

        int[][] M1 = multiply(add(A11, A22), add(B11, B22));
        int[][] M2 = multiply(add(A21, A22), B11);
        int[][] M3 = multiply(A11, subtract(B12, B22));
        int[][] M4 = multiply(A22, subtract(B21, B11));
        int[][] M5 = multiply(add(A11, A12), B22);
        int[][] M6 = multiply(subtract(A21, A11), add(B11, B12));
        int[][] M7 = multiply(subtract(A12, A22), add(B21, B22));

        int[][] C11 = add(subtract(add(M1, M4), M5), M7);
        int[][] C12 = add(M3, M5);
        int[][] C21 = add(M2, M4);
        int[][] C22 = add(subtract(add(M1, M3), M2), M6);

        return combine(C11, C12, C21, C22);
    }

    // Helper methods to split, add, subtract, and combine matrices
    // Splits a matrix into four submatrices: P11, P12, P21, P22
    private static void splitMatrix(int[][] P, int[][] P11, int[][] P12, int[][] P21, int[][] P22) {
        int newSize = P11.length;
        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                P11[i][j] = P[i][j];                        // Top-left
                P12[i][j] = P[i][j + newSize];              // Top-right
                P21[i][j] = P[i + newSize][j];              // Bottom-left
                P22[i][j] = P[i + newSize][j + newSize];    // Bottom-right
            }
        }
    }

    // Adds two matrices A and B
    private static int[][] add(int[][] A, int[][] B) {
        int n = A.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = A[i][j] + B[i][j];
            }
        }
        return result;
    }

    // Subtracts matrix B from matrix A
    private static int[][] subtract(int[][] A, int[][] B) {
        int n = A.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = A[i][j] - B[i][j];
            }
        }
        return result;
    }

    // Combines four submatrices into one matrix
    private static int[][] combine(int[][] C11, int[][] C12, int[][] C21, int[][] C22) {
        int newSize = C11.length;
        int[][] result = new int[newSize * 2][newSize * 2];
        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                result[i][j] = C11[i][j];                  // Top-left
                result[i][j + newSize] = C12[i][j];        // Top-right
                result[i + newSize][j] = C21[i][j];        // Bottom-left
                result[i + newSize][j + newSize] = C22[i][j];  // Bottom-right
            }
        }
        return result;
    }


    // Standard matrix multiplication for base case
    private static int[][] standardMultiply(int[][] A, int[][] B) {
        int[][] result = new int[A.length][B[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B[0].length; j++) {
                for (int k = 0; k < A[0].length; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return result;
    }

    // Method to measure sequential execution time
    private long sequentialMultiplyTime(int[][] A, int[][] B) {
        long startTime = System.nanoTime();
        standardMultiply(A, B);
        return System.nanoTime() - startTime;
    }
}
