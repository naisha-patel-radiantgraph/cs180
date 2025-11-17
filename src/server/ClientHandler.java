package server;

import database.Database;
import user.User;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Server server;
    private final Database db;

    private BufferedReader in;
    private PrintWriter out;

    private User loggedInUser = null;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.db = server.getDatabase();
    }

    @Override
    public void run() {
        try {
            setupStreams();
            out.println("CONNECTED TO CLIENT HANDLER");

            String input;
            while ((input = in.readLine()) != null) {

                String response = handleCommand(input);
                out.println(response);

            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());

        } finally {
            closeEverything();
        }
    }


    private void setupStreams() throws IOException {
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true); // Note: auto-flush is on
    }

    private void closeEverything() {
        try {
            if (in != null) in.close();
        } catch (IOException e) { }

        if (out != null) out.close();

        try {
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) { }
    }


    private String handleCommand(String input) {

        String[] parts = input.split("|"); //this has to be modified based on the protocol Naisha and Arbin decide

        String command = parts[0].toUpperCase();

        switch (command) {

            case "LOGIN":
                return handleLogin(parts);

            case "REGISTER":
                return handleRegister(parts);

            case "VIEWMOVIES":
                return handleViewMovies();

            case "ADD_MOVIE":
                return handleAddMovie(parts);

            case "DISCONNECT":
                return "GOODBYE";

            default:
                return "UNKNOWN_COMMAND";
        }
    }

    // HANDLE ALL COMMANDS

    private String handleLogin(String[] parts) {
        return "NOT_IMPLEMENTED";
    }

    private String handleRegister(String[] parts) {
        return "NOT_IMPLEMENTED";
    }

    private String handleViewMovies() {
        return "NOT_IMPLEMENTED";
    }

    private String handleAddMovie(String[] parts) {
        return "NOT_IMPLEMENTED";
    }
}
