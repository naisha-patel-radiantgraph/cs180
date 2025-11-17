package server;

import database.Database;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

    private static final int PORT = 4242;
    private ServerSocket serverSocket;
    private volatile boolean running;
    private final Database database;

    public Server() {
        this.database = new Database();
        this.running = true;
    }

    public Database getDatabase() {
        return database;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server running on port " + PORT);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    new Thread(handler).start();
                }
                catch (IOException e) {
                    if (!running) break;
                    System.out.println("Connection error: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Failed to start server: " + e.getMessage());

        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed())
                    serverSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing server socket: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error stopping server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server s = new Server();
        new Thread(s).start();
    }
}
