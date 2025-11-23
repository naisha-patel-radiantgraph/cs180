package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import interfaces.IClient;

public class Client implements IClient{

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
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ignored) {
            }
        }
    }


    private void mainMenu() throws IOException {
        while (true) {
            if (!isLoggedIn) {
                System.out.println("\n---------------------");
                System.out.println("CINEMA BOOKING SYSTEM");
                System.out.println("---------------------");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("3. Exit");
                System.out.print("Choose: ");
                String choice = userIn.nextLine().trim();

                switch (choice) {
                    case "1":
                        login();
                        break;
                    case "2":
                        register();
                        break;
                    case "3":
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                        break;
                }
            } else {
                if (isAdmin) {
                    adminMenu();
                } else {
                    guestMenu();
                }
            }
        }
    }


    private void login() throws IOException {
        System.out.println("\n-----");
        System.out.println("Login");
        System.out.println("-----");
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
        System.out.println("\n--------");
        System.out.println("Register");
        System.out.println("--------");
        System.out.print("Username: ");
        String username = userIn.nextLine().trim();
        System.out.print("Password (min 6 chars): ");
        String password = userIn.nextLine().trim();
        System.out.print("Email: ");
        String email = userIn.nextLine().trim();

        serverOut.println("REGISTER|" + username + "|" + password + "|" + email);
        String response = serverIn.readLine();
        if (response != null && response.startsWith("SUCCESS")) {
            System.out.println("Account created successfully.");
        } else {
            System.out.println("Error: " + (response != null ? response.replace("ERROR|", "") : "Unknown error"));
        }
    }

    private void logout() {
        serverOut.println("LOGOUT");
        isLoggedIn = false;
        currentUsername = null;
        isAdmin = false;
        System.out.println("Logged out successfully.");
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
            System.out.print("Choose: ");
            String choice = userIn.nextLine().trim();

            switch (choice) {
                case "1":
                    listMovies();
                    break;
                case "2":
                    bookSeats();
                    break;
                case "3":
                    viewMyBookings();
                    break;
                case "4":
                    logout();
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
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
            System.out.print("Choose: ");
            String choice = userIn.nextLine().trim();

            switch (choice) {
                case "1":
                    listMovies();
                    break;
                case "2":
                    bookSeats();
                    break;
                case "3":
                    viewMyBookings();
                    break;
                case "4":
                    addMovie();
                    break;
                case "5":
                    addShowtime();
                    break;
                case "6":
                    promoteUser();
                    break;
                case "7":
                    logout();
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }



    private void listMovies() throws IOException {
        serverOut.println("LIST_MOVIES");
        String response = serverIn.readLine();

        if (response == null) {
            System.out.println("No response from server.");
            return;
        }

        if (!response.startsWith("SUCCESS")) {
            System.out.println("Error: " + response.replace("ERROR|", ""));
            return;
        }

        String[] parts = response.split("\\|");
        int movieCount = 0;
        if (parts.length > 1) {
            try {
                movieCount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (movieCount == 0) {
            System.out.println("\nNo movies available.");
            // Consume END_LIST
            serverIn.readLine();
            return;
        }

        List<String> movieIds = new ArrayList<String>();
        List<String> movieTitles = new ArrayList<String>();

        System.out.println("\nAvailable movies:");
        for (int i = 0; i < movieCount; i++) {
            String movieLine = serverIn.readLine();
            if (movieLine != null && movieLine.startsWith("MOVIE")) {
                String[] fields = movieLine.split("\\|");

                if (fields.length >= 6) {
                    String movieId = fields[1];
                    String title = fields[2];
                    String genre = fields[3];
                    String rating = fields[4];
                    String runtime = fields[5];

                    System.out.println((i + 1) + ". " + title +
                            " (ID: " + movieId + ", Genre: " + genre +
                            ", Rating: " + rating + ", Runtime: " + runtime + " mins)");

                    movieIds.add(movieId);
                    movieTitles.add(title);
                }
            }
        }


        serverIn.readLine();

        System.out.print("\nView showtimes for one of these movies? (y/n): ");
        String answer = userIn.nextLine().trim().toLowerCase();
        if (answer.startsWith("y")) {
            System.out.print("Enter movie number (1-" + movieIds.size() + ", or 0 to cancel): ");
            String choice = userIn.nextLine().trim();
            int index;
            try {
                index = Integer.parseInt(choice);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
                return;
            }
            if (index == 0) {
                return;
            }
            if (index < 1 || index > movieIds.size()) {
                System.out.println("Invalid choice.");
                return;
            }
            String movieId = movieIds.get(index - 1);
            String title = movieTitles.get(index - 1);
            showShowtimesForMovie(movieId, title);
        }
    }


    private void showShowtimesForMovie(String movieId, String movieTitle) throws IOException {
        serverOut.println("LIST_SHOWTIMES|" + movieId);
        String response = serverIn.readLine();

        if (response == null) {
            System.out.println("No response from server.");
            return;
        }

        if (!response.startsWith("SUCCESS")) {
            System.out.println("Error: " + response.replace("ERROR|", ""));
            return;
        }

        String[] parts = response.split("\\|");
        int showtimeCount = 0;
        if (parts.length > 1) {
            try {
                showtimeCount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (showtimeCount == 0) {
            System.out.println("\nNo showtimes available for \"" + movieTitle + "\".");
            // Consume END_LIST
            serverIn.readLine();
            return;
        }

        System.out.println("\nShowtimes for \"" + movieTitle + "\":");
        for (int i = 0; i < showtimeCount; i++) {
            String line = serverIn.readLine();
            if (line != null && line.startsWith("SHOWTIME")) {
                String[] f = line.split("\\|");
                // SHOWTIME|showtimeId|dateTime|availableSeats|totalSeats|price|auditorium
                if (f.length >= 7) {
                    String showtimeId = f[1];
                    String dateTime = f[2];
                    String availableSeats = f[3];
                    String totalSeats = f[4];
                    String price = f[5];
                    String auditorium = f[6];
                    System.out.println("- ID: " + showtimeId + " | " + dateTime +
                            " | Seats: " + availableSeats + "/" + totalSeats +
                            " | Price: $" + price +
                            " | Auditorium: " + auditorium);
                }
            }
        }


        serverIn.readLine();
    }



    private void bookSeats() throws IOException {
        System.out.println("\n=====================");
        System.out.println("   BOOKING SEATS");
        System.out.println("=====================");


        serverOut.println("LIST_MOVIES");
        String response = serverIn.readLine();

        if (response == null || !response.startsWith("SUCCESS")) {
            System.out.println("Error loading movies: " +
                    (response != null ? response.replace("ERROR|", "") : "Unknown error"));
            return;
        }

        String[] parts = response.split("\\|");
        int movieCount = 0;
        if (parts.length > 1) {
            try {
                movieCount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (movieCount == 0) {
            System.out.println("No movies available to book.");
            serverIn.readLine();
            return;
        }

        List<String> movieIds = new ArrayList<String>();
        List<String> movieTitles = new ArrayList<String>();

        System.out.println("\nAvailable movies:");
        for (int i = 0; i < movieCount; i++) {
            String line = serverIn.readLine();
            if (line != null && line.startsWith("MOVIE")) {
                String[] f = line.split("\\|");
                if (f.length >= 6) {
                    String movieId = f[1];
                    String title = f[2];
                    String genre = f[3];
                    String rating = f[4];
                    String runtime = f[5];
                    System.out.println((i + 1) + ". " + title +
                            " (ID: " + movieId + ", Genre: " + genre +
                            ", Rating: " + rating + ", " + runtime + " mins)");
                    movieIds.add(movieId);
                    movieTitles.add(title);
                }
            }
        }


        serverIn.readLine();

        System.out.print("\nChoose a movie to book (1-" + movieIds.size() +
                ", or 0 to cancel): ");
        String movieChoice = userIn.nextLine().trim();
        int movieIndex;
        try {
            movieIndex = Integer.parseInt(movieChoice);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return;
        }
        if (movieIndex == 0) {
            return;
        }
        if (movieIndex < 1 || movieIndex > movieIds.size()) {
            System.out.println("Invalid movie choice.");
            return;
        }

        String chosenMovieId = movieIds.get(movieIndex - 1);
        String chosenMovieTitle = movieTitles.get(movieIndex - 1);

        serverOut.println("LIST_SHOWTIMES|" + chosenMovieId);
        response = serverIn.readLine();

        if (response == null || !response.startsWith("SUCCESS")) {
            System.out.println("Error loading showtimes: " +
                    (response != null ? response.replace("ERROR|", "") : "Unknown error"));
            return;
        }

        parts = response.split("\\|");
        int showtimeCount = 0;
        if (parts.length > 1) {
            try {
                showtimeCount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (showtimeCount == 0) {
            System.out.println("No showtimes available for \"" + chosenMovieTitle + "\".");
            serverIn.readLine();
            return;
        }

        List<String> showtimeIds = new ArrayList<String>();

        System.out.println("\nShowtimes for \"" + chosenMovieTitle + "\":");
        for (int i = 0; i < showtimeCount; i++) {
            String line = serverIn.readLine();
            if (line != null && line.startsWith("SHOWTIME")) {
                String[] f = line.split("\\|");
                if (f.length >= 7) {
                    String showtimeId = f[1];
                    String dateTime = f[2];
                    String availableSeats = f[3];
                    String totalSeats = f[4];
                    String price = f[5];
                    String auditorium = f[6];

                    System.out.println((i + 1) + ". ID: " + showtimeId +
                            " | " + dateTime +
                            " | Seats: " + availableSeats + "/" + totalSeats +
                            " | Price: $" + price +
                            " | Auditorium: " + auditorium);

                    showtimeIds.add(showtimeId);
                }
            }
        }


        serverIn.readLine();

        System.out.print("\nChoose a showtime (1-" + showtimeIds.size() +
                ", or 0 to cancel): ");
        String stChoice = userIn.nextLine().trim();
        int stIndex;
        try {
            stIndex = Integer.parseInt(stChoice);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return;
        }
        if (stIndex == 0) {
            return;
        }
        if (stIndex < 1 || stIndex > showtimeIds.size()) {
            System.out.println("Invalid showtime choice.");
            return;
        }

        String chosenShowtimeId = showtimeIds.get(stIndex - 1);


        int[][] seatsAvailable = viewSeatMap(chosenShowtimeId);
        if (seatsAvailable == null) {
            return;
        }

        int rows = seatsAvailable.length;
        int cols = rows > 0 ? seatsAvailable[0].length : 0;

        System.out.print("\nHow many seats would you like to book? (0 to cancel): ");
        String seatCountStr = userIn.nextLine().trim();
        int seatCount;
        try {
            seatCount = Integer.parseInt(seatCountStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return;
        }

        if (seatCount == 0) {
            return;
        }
        if (seatCount < 0 || seatCount > 50) {
            System.out.println("Seat count must be between 1 and 50.");
            return;
        }

        List<String> chosenSeats = new ArrayList<String>();
        for (int i = 0; i < seatCount; i++) {
            while (true) {
                System.out.print("Seat " + (i + 1) +
                        " (row:col, e.g. 1:3) or 'b' to cancel booking: ");
                String seatInput = userIn.nextLine().trim();
                if (seatInput.equalsIgnoreCase("b")) {
                    System.out.println("Booking cancelled.");
                    return;
                }

                if (!seatInput.matches("\\d+:\\d+")) {
                    System.out.println("Invalid format. Use row:col with numbers, e.g. 1:3.");
                    continue;
                }

                String[] rc = seatInput.split(":");
                int row;
                int col;
                try {
                    row = Integer.parseInt(rc[0]); // 1-based
                    col = Integer.parseInt(rc[1]); // 1-based
                } catch (NumberFormatException e) {
                    System.out.println("Row and column must be numbers.");
                    continue;
                }

                if (row < 1 || row > rows || col < 1 || col > cols) {
                    System.out.println("Seat out of range. Please choose within the displayed seat map.");
                    continue;
                }


                if (seatsAvailable[row - 1][col - 1] == 0) {
                    System.out.println("That seat is already booked. Please choose another.");
                    continue;
                }

                chosenSeats.add(row + ":" + col);
                break;
            }
        }


        StringBuilder cmd = new StringBuilder();
        cmd.append("BOOK|").append(chosenShowtimeId).append("|").append(seatCount);
        for (int i = 0; i < chosenSeats.size(); i++) {
            cmd.append("|").append(chosenSeats.get(i));
        }

        serverOut.println(cmd.toString());
        response = serverIn.readLine();

        if (response != null && response.startsWith("SUCCESS")) {
            String[] f = response.split("\\|");
            String bookingId = f.length > 1 ? f[1] : "Unknown";
            String totalCost = f.length > 2 ? f[2] : "0.00";
            System.out.println("\nBooking confirmed!");
            System.out.println("Booking ID: " + bookingId);
            System.out.println("Total cost: $" + totalCost);
        } else {
            System.out.println("Error: " + (response != null ? response.replace("ERROR|", "") : "Unknown error"));
        }
    }


    private int[][] viewSeatMap(String showtimeId) throws IOException {
        serverOut.println("VIEW_SEATS|" + showtimeId);
        String response = serverIn.readLine();

        if (response == null) {
            System.out.println("No response from server.");
            return null;
        }

        if (!response.startsWith("SUCCESS")) {
            System.out.println("Error: " + response.replace("ERROR|", ""));
            return null;
        }

        String[] parts = response.split("\\|");
        if (parts.length < 3) {
            System.out.println("Invalid seat map response.");
            return null;
        }

        int rows = 0;
        int cols = 0;
        try {
            rows = Integer.parseInt(parts[1]);
            cols = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid row/column info from server.");
            return null;
        }

        int[][] seats = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            String line = serverIn.readLine();
            if (line == null) {
                System.out.println("Unexpected end of seat data.");
                return null;
            }
            if (!line.startsWith("ROW|")) {
                System.out.println("Unexpected seat row format.");
                return null;
            }
            String[] rowParts = line.split("\\|");
            for (int c = 0; c < cols; c++) {
                int index = 2 + c;
                if (index < rowParts.length) {
                    seats[r][c] = "1".equals(rowParts[index]) ? 1 : 0;
                } else {
                    seats[r][c] = 0;
                }
            }
        }


        String endLine = serverIn.readLine();
        if (endLine == null || !endLine.equals("END_SEATS")) {
        }


        System.out.println("\n        SCREEN");
        System.out.println("--------------------------------");

        for (int r = 0; r < rows; r++) {
            char rowLabel;
            if (r < 26) {
                rowLabel = (char) ('A' + r);
            } else {
                rowLabel = '?';
            }

            StringBuilder line = new StringBuilder();
            line.append(rowLabel).append(": ");
            for (int c = 0; c < cols; c++) {
                if (seats[r][c] == 1) {
                    line.append("[O]");
                } else {
                    line.append("[X]");
                }
            }
            System.out.println(line.toString());
        }

        System.out.println("--------------------------------");
        System.out.println("O = available, X = booked");
        System.out.println("Rows are labeled A, B, C... (Row 1 = A, Row 2 = B, etc.)");
        System.out.println("You will enter seats using numbers like 1:3 (row:col).");

        return seats;
    }


    private void viewMyBookings() throws IOException {
        serverOut.println("MY_BOOKINGS");
        String response = serverIn.readLine();

        if (response == null) {
            System.out.println("No response from server.");
            return;
        }

        if (!response.startsWith("SUCCESS")) {
            System.out.println("Error: " + response.replace("ERROR|", ""));
            return;
        }

        String[] parts = response.split("\\|");
        int count = 0;
        if (parts.length > 1) {
            try {
                count = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (count == 0) {
            System.out.println("You have no bookings.");
            serverIn.readLine();
            return;
        }

        System.out.println("\nYour bookings:");
        for (int i = 0; i < count; i++) {
            String bookingLine = serverIn.readLine();
            if (bookingLine != null && bookingLine.startsWith("BOOKING|")) {
                String[] f = bookingLine.split("\\|");
                if (f.length >= 6) {
                    String bookingId = f[1];
                    String movieTitle = f[2];
                    String dateTime = f[3];
                    String seatList = f[4];
                    String cost = f[5];

                    System.out.println("-----------------------------");
                    System.out.println("Booking ID: " + bookingId);
                    System.out.println("Movie: " + movieTitle);
                    System.out.println("Showtime: " + dateTime);
                    System.out.println("Seats: " + seatList);
                    System.out.println("Total Cost: $" + cost);
                } else {
                    System.out.println(bookingLine.replace("BOOKING|", ""));
                }
            }
        }

        serverIn.readLine();
    }

    private void addMovie() throws IOException {
        System.out.print("Enter movie title: ");
        String title = userIn.nextLine().trim();
        System.out.print("Enter genre: ");
        String genre = userIn.nextLine().trim();
        System.out.print("Enter rating: ");
        String rating = userIn.nextLine().trim();
        System.out.print("Enter runtime (minutes): ");
        String runtime = userIn.nextLine().trim();

        serverOut.println("ADMIN_ADD_MOVIE|" + title + "|" + genre + "|" + rating + "|" + runtime);
        String response = serverIn.readLine();

        if (response != null && response.startsWith("SUCCESS")) {
            System.out.println("Movie added successfully.");
        } else {
            System.out.println("Error: " + (response != null ? response.replace("ERROR|", "") : "Unknown error"));
        }
    }

    private void addShowtime() throws IOException {
        System.out.print("Enter movie ID (title): ");
        String movieId = userIn.nextLine().trim();
        System.out.print("Enter dateTime (yyyy-MM-dd HH:mm): ");
        String dateTime = userIn.nextLine().trim();
        System.out.print("Enter number of rows: ");
        String rows = userIn.nextLine().trim();
        System.out.print("Enter number of columns: ");
        String cols = userIn.nextLine().trim();
        System.out.print("Enter base price: ");
        String price = userIn.nextLine().trim();
        System.out.print("Enter auditorium name: ");
        String auditorium = userIn.nextLine().trim();

        serverOut.println("ADMIN_ADD_SHOWTIME|" + movieId + "|" + dateTime + "|" +
                rows + "|" + cols + "|" + price + "|" + auditorium);
        String response = serverIn.readLine();

        if (response != null && response.startsWith("SUCCESS")) {
            System.out.println("Showtime added successfully.");
        } else {
            System.out.println("Error: " + (response != null ? response.replace("ERROR|", "") : "Unknown error"));
        }
    }

    private void promoteUser() throws IOException {
        System.out.print("Enter username to promote: ");
        String username = userIn.nextLine().trim();
        serverOut.println("ADMIN_PROMOTE|" + username);
        String response = serverIn.readLine();

        if (response != null && response.startsWith("SUCCESS")) {
            System.out.println("User promoted to admin.");
        } else {
            System.out.println("Error: " + (response != null ? response.replace("ERROR|", "") : "Unknown error"));
        }
    }


    public static void main(String[] args) {
        Client client = new Client("localhost", 4242);
        client.start();
    }
}
