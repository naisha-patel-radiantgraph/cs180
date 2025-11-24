package server;

import database.Database;
import user.User;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import interfaces.IServer;

public class Server implements Runnable, IServer {

    private static final int PORT = 4242;
    private ServerSocket serverSocket;
    private volatile boolean running;
    private final Database database;

    public Server() {
        Database loadedDb = null;
        try {
            loadedDb = (Database) new Database().loadDatabase();
            System.out.println("Database loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing database found, starting fresh.");
            loadedDb = new Database();
        }
        this.database = loadedDb;
        this.running = true;
        initializeDefaultAdmin();
    }

    /**
     * Creates the default admin account on server startup
     * Username: admin
     * Password: admin123
     * This admin can manage the system and promote other users
     */
    private void initializeDefaultAdmin() {
        if (database.findUser("admin") == null) {
            User adminUser = new User("admin", "admin123", "admin@cinema.com", true);
            database.addUser(adminUser);
            try {
                database.saveDatabase();
            } catch (IOException e) {
                System.out.println("Error saving database after creating admin: " + e.getMessage());
            }
            System.out.println("Default admin account created (username: admin, password: admin123)");
        } else {
            System.out.println("Admin account already exists");
        }
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
