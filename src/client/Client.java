package client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;
    private final Scanner userIn;

    private boolean isLoggedIn = false;
    private String currentUsername = null;
    private boolean isAdmin = false;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        this.userIn = new Scanner(System.in);
    }

    public void start() {
        try {
            socket = new Socket(host, port);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOut = new PrintWriter(socket.getOutputStream(), true);

            String welcome = serverIn.readLine();
            if (welcome != null && welcome.startsWith("CONNECTED|")) {
                System.out.println(welcome.replace("CONNECTED|", ""));
            } else {
                System.out.println(welcome);
            }

            mainMenu();

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException ignored) {}
        }
    }

    private void mainMenu() throws IOException {
        while (true) {
            if (!isLoggedIn) {
                System.out.println("\n---------------------\nCINEMA BOOKING SYSTEM\n---------------------");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("3. Exit");
                System.out.print("Choose: ");
                String choice = userIn.nextLine().trim();
                switch (choice) {
                    case "1" -> login();
                    case "2" -> register();
                    case "3" -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } else {
                if (isAdmin) adminMenu();
                else guestMenu();
            }
        }
    }

    private void login() throws IOException {
        System.out.println("\n-----\nLogin\n-----");
        System.out.print("Username: ");
        String username = userIn.nextLine().trim();
        System.out.print("Password: ");
        String password = userIn.nextLine().trim();

        serverOut.println("LOGIN|" + username + "|" + password);
        String response = serverIn.readLine();

        if (response != null && response.startsWith("SUCCESS")) {
            String[] parts = response.split("\\|", 3);
            currentUsername = username;
            isLoggedIn = true;
            if (parts.length >= 3) {
                isAdmin = Boolean.parseBoolean(parts[2]);
            } else {
                isAdmin = false;
            }
            System.out.println(parts[1]);
        } else {
            System.out.println("Error: " + (response != null ? response.replace("ERROR|", "") : "Unknown error"));
        }
    }

    private void register() throws IOException {
        System.out.println("\n--------\nRegister\n--------");
        System.out.print("Username: ");
        String username = userIn.nextLine().trim();
        System.out.print("Password: ");
        String password = userIn.nextLine().trim();
        System.out.print("Email: ");
        String email = userIn.nextLine().trim();

        serverOut.println("REGISTER|" + username + "|" + password + "|" + email);
        String response = serverIn.readLine();
        if (response != null && response.startsWith("SUCCESS")) {
            System.out.println("Account created successfully");
        } else {
            System.out.println("Error: Could not register (maybe username exists)");
        }
    }

    private void guestMenu() throws IOException {
        while (isLoggedIn && !isAdmin) {
            System.out.println("\n---------------------");
            System.out.println("GUEST MENU - Logged in as " + currentUsername);
            System.out.println("---------------------");
            System.out.println("1. List Movies");
            System.out.println("2. Book Seats");
            System.out.println("3. My Bookings");
            System.out.println("4. Logout");
            System.out.println("5. Exit");
            System.out.print("Choose: ");
            String choice = userIn.nextLine().trim();
            switch (choice) {
                case "1" -> listMovies();
                case "2" -> bookSeats();
                case "3" -> viewMyBookings();
                case "4" -> logout();
                case "5" -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void adminMenu() throws IOException {
        while (isLoggedIn && isAdmin) {
            System.out.println("\n---------------------");
            System.out.println("ADMIN MENU - Logged in as " + currentUsername);
            System.out.println("---------------------");
            System.out.println("1. List Movies");
            System.out.println("2. Book Seats");
            System.out.println("3. My Bookings");
            System.out.println("4. Add Movie");
            System.out.println("5. Add Showtime");
            System.out.println("6. Promote User");
            System.out.println("7. Logout");
            System.out.println("8. Exit");
            System.out.print("Choose: ");
            String choice = userIn.nextLine().trim();
            switch (choice) {
                case "1" -> listMovies();
                case "2" -> bookSeats();
                case "3" -> viewMyBookings();
                case "4" -> addMovie();
                case "5" -> addShowtime();
                case "6" -> promoteUser();
                case "7" -> logout();
                case "8" -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void listMovies() throws IOException {
        serverOut.println("LIST_MOVIES");
        String response = serverIn.readLine();
        if (response != null && response.startsWith("SUCCESS")) {
            String[] parts = response.split("\\|");
            int count = 0;
            if (parts.length > 1) {
                try {
                    count = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {}
            }
            System.out.println("Movies available: " + count);
            for (int i = 0; i < count; i++) {
                String movieLine = serverIn.readLine();
                System.out.println(movieLine.replace("MOVIE|", ""));
            }
            System.out.println(serverIn.readLine());
        } else {
            System.out.println("Error: " + (response != null ? response.replace("ERROR|", "") : "Unknown error"));
        }
    }

    private void bookSeats() throws IOException {
        System.out.println("Booking seats...");
        System.out.print("Enter showtime ID: ");
        String showtimeId = userIn.nextLine().trim();
        if (showtimeId.isEmpty()) {
            System.out.println("Invalid showtime ID. Returning to menu.");
            return;
        }

        System.out.print("Number of seats: ");
        int seatCount;
        try {
            seatCount = Integer.parseInt(userIn.nextLine().trim());
            if (seatCount <= 0 || seatCount > 50) {
                System.out.println("Invalid number of seats. Returning to menu.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Returning to menu.");
            return;
        }

        List<String> seats = new ArrayList<>();
        for (int i = 0; i < seatCount; i++) {
            System.out.print("Seat " + (i + 1) + " (row:col): ");
            String seat = userIn.nextLine().trim();
            if (!seat.matches("\\d+:\\d+")) {
                System.out.println("Invalid seat format. Returning to menu.");
                return;
            }
            seats.add(seat);
        }

        StringBuilder cmd = new StringBuilder("BOOK|" + showtimeId + "|" + seatCount);
        for (String seat : seats) cmd.append("|").append(seat);
        serverOut.println(cmd);

        String response = serverIn.readLine();
        if (response != null && response.startsWith("SUCCESS")) {
            System.out.println("Booking confirmed: " + response);
        } else {
            System.out.println("Error: " + (response != null ? response.replace("ERROR|", "") : "Unknown error"));
        }
    }

    private void viewMyBookings() throws IOException {
        serverOut.println("MY_BOOKINGS");
        String response = serverIn.readLine();
        if (response != null && response.startsWith("SUCCESS")) {
            String[] parts = response.split("\\|");
            int count = 0;
            if (parts.length > 1) {
                try {
                    count = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {}
            }
            System.out.println("Bookings: " + count);
            for (int i = 0; i < count; i++) {
                String bookingLine = serverIn.readLine();
                System.out.println(bookingLine.replace("BOOKING|", ""));
            }
            System.out.println(serverIn.readLine());
        } else {
            System.out.println("Error: " + (response != null ? response.replace("ERROR|", "") : "Unknown error"));
        }
    }

    private void addMovie() throws IOException {
        System.out.print("Enter movie title: ");
        String title = userIn.nextLine().trim();
        System.out.print("Enter genre: ");
        String genre = userIn.nextLine().trim();
        System.out.print("Enter rating: ");
        String rating = userIn.nextLine().trim();
        System.out.print("Enter runtime: ");
        String runtime = userIn.nextLine().trim();

        serverOut.println("ADMIN_ADD_MOVIE|" + title + "|" + genre + "|" + rating + "|" + runtime);
        String response = serverIn.readLine();
        System.out.println(response != null && response.startsWith("SUCCESS")
                ? "Movie added successfully"
                : "Error: " + (response != null ? response.replace("ERROR|", "") : "Unknown error"));
    }

    private void addShowtime() throws IOException {
        System.out.print("Enter movie ID: ");
        String movieId = userIn.nextLine().trim();
        System.out.print("Enter dateTime (yyyy-MM-dd HH:mm): ");
        String dateTime = userIn.nextLine().trim();
        System.out.print("Enter rows: ");
        String rows = userIn.nextLine().trim();
        System.out.print("Enter cols: ");
        String cols = userIn.nextLine().trim();
        System.out.print("Enter base price: ");
        String price = userIn.nextLine().trim();
        System.out.print("Enter auditorium: ");
        String auditorium = userIn.nextLine().trim();

        serverOut.println("ADMIN_ADD_SHOWTIME|" + movieId + "|" + dateTime + "|" + rows + "|" + cols + "|" + price + "|" + auditorium);
        String response = serverIn.readLine();
        System.out.println(response != null && response.startsWith("SUCCESS")
                ? "Showtime added successfully"
                : "Error: " + (response != null ? response.replace("ERROR|", "") : "Unknown error"));
    }

    private void promoteUser() throws IOException {
        System.out.print("Enter username to promote: ");
        String username = userIn.nextLine().trim();
        serverOut.println("ADMIN_PROMOTE|" + username);
        String response = serverIn.readLine();
        System.out.println(response != null && response.startsWith("SUCCESS")
                ? "User promoted to admin"
                : "Error: " + (response != null ? response.replace("ERROR|", "") : "Unknown error"));
    }

    private void logout() {
        serverOut.println("LOGOUT");
        isLoggedIn = false;
        currentUsername = null;
        isAdmin = false;
        System.out.println("Logged out successfully.");
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 4242);
        client.start();
    }
}













