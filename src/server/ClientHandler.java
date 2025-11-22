package server;

import database.Database;
import user.User;
import movie.Movie;
import showtime.Showtime;
import seat.Seat;
import reservation.Reservation;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * ClientHandler manages communication with a single connected client
 * Runs in its own thread and processes all client commands according to the protocol
 *
 * @author Group 4, L26
 * @version Nov 18, 2025
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Server server;
    private final Database db;

    private BufferedReader in;
    private PrintWriter out;

    private User currentUser = null;
    private boolean isAuthenticated = false;

    // Validation patterns
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Constructor for ClientHandler
     * @param socket the client socket connection
     * @param server the server instance managing this handler
     */
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.db = server.getDatabase();
    }

    /**
     * Main thread loop - handles all client communication
     */
    @Override
    public void run() {
        try {
            setupStreams();
            send(Protocol.CONNECTED + Protocol.DELIMITER + "Welcome to Cinema Booking System");

            String input;
            while ((input = in.readLine()) != null) {
                if (input.trim().isEmpty()) {
                    continue;
                }

                try {
                    handleCommand(input);
                } catch (Exception e) {
                    sendError("An error occurred: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());

        } finally {
            closeEverything();
        }
    }

    /**
     * Initialize input and output streams
     */
    private void setupStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * Clean up all resources
     */
    private void closeEverything() {
        try {
            if (in != null) in.close();
        } catch (IOException e) {
        }

        if (out != null) out.close();

        try {
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
        }
    }

    /**
     * Send a message to the client
     * @param message the message to send
     */
    private void send(String message) {
        out.println(message);
    }

    /**
     * Send an error message to the client
     * @param message the error message
     */
    private void sendError(String message) {
        send(Protocol.ERROR + Protocol.DELIMITER + message);
    }

    /**
     * Send a success message to the client
     * @param message the success message
     */
    private void sendSuccess(String message) {
        send(Protocol.SUCCESS + Protocol.DELIMITER + message);
    }

    /**
     * Main command handler - routes commands to appropriate methods
     * @param input the raw command string from the client
     */
    private void handleCommand(String input) {
        String[] parts = input.split("\\" + Protocol.DELIMITER, -1);

        if (parts.length == 0) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        String command = parts[0].toUpperCase();

        switch (command) {
            case Protocol.LOGIN:
                handleLogin(parts);
                break;

            case Protocol.REGISTER:
                handleRegister(parts);
                break;

            case Protocol.LOGOUT:
                handleLogout();
                break;

            case Protocol.LIST_MOVIES:
                handleListMovies();
                break;

            case Protocol.LIST_SHOWTIMES:
                handleListShowtimes(parts);
                break;

            case Protocol.VIEW_SEATS:
                handleViewSeats(parts);
                break;

            case Protocol.BOOK:
                handleBookSeats(parts);
                break;

            case Protocol.CANCEL:
                handleCancelReservation(parts);
                break;

            case Protocol.MY_BOOKINGS:
                handleMyBookings();
                break;

            case Protocol.ADMIN_ADD_MOVIE:
                handleAdminAddMovie(parts);
                break;

            case Protocol.ADMIN_ADD_SHOWTIME:
                handleAdminAddShowtime(parts);
                break;

            case Protocol.ADMIN_PROMOTE:
                handleAdminPromoteUser(parts);
                break;

            case Protocol.ADMIN_VIEW_ALL_BOOKINGS:
                handleAdminViewAllBookings();
                break;

            default:
                sendError(Protocol.ERROR_INVALID_COMMAND);
        }
    }

    // ==================== AUTHENTICATION METHODS ====================

    /**
     * Handle LOGIN command
     * Format: LOGIN|username|password
     * Response: SUCCESS|Welcome username!|isAdmin OR ERROR|Invalid credentials
     */
    private void handleLogin(String[] parts) {
        if (parts.length < 3) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        String username = parts[1];
        String password = parts[2];

        synchronized (db) {
            User user = db.findUser(username);

            if (user == null || !user.verifyPassword(password)) {
                sendError("Invalid credentials");
                return;
            }

            this.currentUser = user;
            this.isAuthenticated = true;
            sendSuccess("Welcome " + username + "!" + Protocol.DELIMITER + user.isAdmin());
        }
    }

    /**
     * Handle REGISTER command
     * Format: REGISTER|username|password|email
     * Response: SUCCESS|Account created successfully OR ERROR|<error message>
     */
    private void handleRegister(String[] parts) {
        if (parts.length < 4) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        String username = parts[1];
        String password = parts[2];
        String email = parts[3];

        // Validate username
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            sendError("Username must be alphanumeric, 3-20 characters");
            return;
        }

        // Validate password
        if (password.length() < 6) {
            sendError("Password must be at least 6 characters");
            return;
        }

        // Validate email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            sendError("Invalid email format");
            return;
        }

        synchronized (db) {
            // Check if username already exists
            if (db.findUser(username) != null) {
                sendError("Username already exists");
                return;
            }

            // Create new user (non-admin by default)
            User newUser = new User(username, password, email, false);
            db.addUser(newUser);
            try {
                db.saveDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendSuccess("Account created successfully");
        }
    }

    /**
     * Handle LOGOUT command
     * Response: SUCCESS|Logged out successfully
     */
    private void handleLogout() {
        this.currentUser = null;
        this.isAuthenticated = false;
        sendSuccess("Logged out successfully");
    }

    // ==================== MOVIE & SHOWTIME LISTING METHODS ====================

    /**
     * Handle LIST_MOVIES command
     * Response: SUCCESS|movieCount
     *           MOVIE|movieId|title|genre|rating|runtime
     *           ...
     *           END_LIST
     */
    private void handleListMovies() {
        synchronized (db) {
            List<Movie> movies = db.getMovies();
            send(Protocol.SUCCESS + Protocol.DELIMITER + movies.size());

            for (Movie movie : movies) {
                // MOVIE|movieId|title|genre|rating|runtime
                String movieData = String.join(Protocol.DELIMITER,
                        Protocol.MOVIE,
                        movie.getTitle(), // Using title as ID
                        movie.getTitle(),
                        movie.getGenre() != null ? movie.getGenre() : "",
                        movie.getRating() != null ? movie.getRating() : "",
                        String.valueOf(movie.getRuntime())
                );
                send(movieData);
            }

            send(Protocol.END_LIST);
        }
    }

    /**
     * Handle LIST_SHOWTIMES command
     * Format: LIST_SHOWTIMES|movieId (where movieId is the movie title)
     * Response: SUCCESS|showtimeCount
     *           SHOWTIME|showtimeId|dateTime|availableSeats|totalSeats|price|auditorium
     *           ...
     *           END_LIST
     */
    private void handleListShowtimes(String[] parts) {
        if (parts.length < 2) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        String movieTitle = parts[1];

        synchronized (db) {
            // Find the movie
            Movie targetMovie = null;
            for (Movie m : db.getMovies()) {
                if (m.getTitle().equals(movieTitle)) {
                    targetMovie = m;
                    break;
                }
            }

            if (targetMovie == null) {
                sendError("Movie not found");
                return;
            }

            // Find all showtimes for this movie
            List<Showtime> movieShowtimes = new ArrayList<>();
            for (Showtime st : db.getShowtimes()) {
                if (st.getMovie().getTitle().equals(movieTitle)) {
                    movieShowtimes.add(st);
                }
            }

            send(Protocol.SUCCESS + Protocol.DELIMITER + movieShowtimes.size());

            for (int i = 0; i < movieShowtimes.size(); i++) {
                Showtime st = movieShowtimes.get(i);
                int totalSeats = st.getRowCount() * st.getColCount();
                int availableSeats = st.getAvailableSeatCount();

                // SHOWTIME|showtimeId|dateTime|availableSeats|totalSeats|price|auditorium
                String showtimeData = String.join(Protocol.DELIMITER,
                        Protocol.SHOWTIME,
                        "ST_" + i, // Simple ID based on index
                        st.getDateTime().format(DATE_TIME_FORMATTER),
                        String.valueOf(availableSeats),
                        String.valueOf(totalSeats),
                        String.format("%.2f", st.getBasePrice()),
                        st.getAuditoriumName() != null ? st.getAuditoriumName() : ""
                );
                send(showtimeData);
            }

            send(Protocol.END_LIST);
        }
    }

    /**
     * Handle VIEW_SEATS command
     * Format: VIEW_SEATS|showtimeId
     * Response: SUCCESS|rows|cols
     *           ROW|rowNumber|1|0|1|...
     *           ...
     *           END_SEATS
     */
    private void handleViewSeats(String[] parts) {
        if (parts.length < 2) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        String showtimeId = parts[1];
        Showtime showtime = findShowtimeById(showtimeId);

        if (showtime == null) {
            sendError("Showtime not found");
            return;
        }

        synchronized (showtime) {
            int rows = showtime.getRowCount();
            int cols = showtime.getColCount();

            send(Protocol.SUCCESS + Protocol.DELIMITER + rows + Protocol.DELIMITER + cols);

            // Send each row with seat availability
            for (int r = 0; r < rows; r++) {
                StringBuilder rowData = new StringBuilder();
                rowData.append(Protocol.ROW).append(Protocol.DELIMITER).append(r + 1);

                for (int c = 0; c < cols; c++) {
                    rowData.append(Protocol.DELIMITER);
                    rowData.append(showtime.isSeatAvailable(r, c) ? "1" : "0");
                }

                send(rowData.toString());
            }

            send(Protocol.END_SEATS);
        }
    }

    // ==================== BOOKING METHODS ====================

    /**
     * Handle BOOK command
     * Format: BOOK|showtimeId|seatCount|row1:col1|row2:col2|...
     * Response: SUCCESS|bookingId|totalCost|Booking confirmed OR ERROR|<error message>
     */
    private void handleBookSeats(String[] parts) {
        if (!isAuthenticated) {
            sendError(Protocol.ERROR_AUTH_REQUIRED);
            return;
        }

        if (parts.length < 4) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        String showtimeId = parts[1];
        int seatCount;

        try {
            seatCount = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            sendError("Invalid seat count");
            return;
        }

        if (parts.length != 3 + seatCount) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        Showtime showtime = findShowtimeById(showtimeId);
        if (showtime == null) {
            sendError("Showtime not found");
            return;
        }

        // Parse seat selections
        List<int[]> seatPositions = new ArrayList<>();
        for (int i = 3; i < parts.length; i++) {
            String[] rowCol = parts[i].split(Protocol.SEAT_DELIMITER);
            if (rowCol.length != 2) {
                sendError("Invalid seat format");
                return;
            }

            try {
                int row = Integer.parseInt(rowCol[0]);
                int col = Integer.parseInt(rowCol[1]);
                seatPositions.add(new int[]{row, col});
            } catch (NumberFormatException e) {
                sendError("Invalid seat coordinates");
                return;
            }
        }

        synchronized (showtime) {
            // Check if all seats are available
            for (int[] pos : seatPositions) {
                if (!showtime.isSeatAvailable(pos[0], pos[1])) {
                    sendError("Seat(s) already booked");
                    return;
                }
            }

            // Create seat objects for reservation
            ArrayList<Seat> bookedSeats = new ArrayList<>();
            for (int[] pos : seatPositions) {
                Seat seat = new Seat(pos[0], pos[1], showtime.getBasePrice());
                bookedSeats.add(seat);
            }

            // Create reservation
            Reservation reservation = new Reservation(currentUser, showtime, bookedSeats);

            synchronized (db) {
                db.addReservation(reservation);
                currentUser.addReservation(reservation);
                try {
                    db.saveDatabase();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            double totalCost = reservation.getTotalPrice();
            String bookingId = reservation.getBookingID();

            sendSuccess(bookingId + Protocol.DELIMITER +
                    String.format("%.2f", totalCost) + Protocol.DELIMITER +
                    "Booking confirmed");
        }
    }

    /**
     * Handle CANCEL command
     * Format: CANCEL|bookingId
     * Response: SUCCESS|Reservation cancelled OR ERROR|<error message>
     */
    private void handleCancelReservation(String[] parts) {
        if (!isAuthenticated) {
            sendError(Protocol.ERROR_AUTH_REQUIRED);
            return;
        }

        if (parts.length < 2) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        String bookingId = parts[1];

        synchronized (db) {
            Reservation reservation = db.findReservation(bookingId);

            if (reservation == null) {
                sendError("Booking not found");
                return;
            }

            // Check if user owns this reservation
            if (!reservation.getUser().getUsername().equals(currentUser.getUsername())) {
                sendError("Not authorized to cancel this booking");
                return;
            }

            // Cancel the reservation
            reservation.cancelAllSeats();
            db.removeReservation(bookingId);
            currentUser.removeReservation(bookingId);
            try {
                db.saveDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }

            sendSuccess("Reservation cancelled");
        }
    }

    /**
     * Handle MY_BOOKINGS command
     * Response: SUCCESS|bookingCount
     *           BOOKING|bookingId|movieTitle|dateTime|seatList|totalCost
     *           ...
     *           END_LIST
     */
    private void handleMyBookings() {
        if (!isAuthenticated) {
            sendError(Protocol.ERROR_AUTH_REQUIRED);
            return;
        }

        synchronized (db) {
            List<Reservation> userReservations = currentUser.getReservations();
            send(Protocol.SUCCESS + Protocol.DELIMITER + userReservations.size());

            for (Reservation res : userReservations) {
                // Build seat list
                StringBuilder seatList = new StringBuilder();
                ArrayList<Seat> seats = res.getBookedSeats();
                for (int i = 0; i < seats.size(); i++) {
                    if (i > 0) seatList.append(Protocol.SEAT_SEPARATOR);
                    seatList.append(seats.get(i).getSeatLabel());
                }

                // BOOKING|bookingId|movieTitle|dateTime|seatList|totalCost
                String bookingData = String.join(Protocol.DELIMITER,
                        Protocol.BOOKING,
                        res.getBookingID(),
                        res.getShowtime().getMovie().getTitle(),
                        res.getShowtime().getDateTime().format(DATE_TIME_FORMATTER),
                        seatList.toString(),
                        String.format("%.2f", res.getTotalPrice())
                );
                send(bookingData);
            }

            send(Protocol.END_LIST);
        }
    }

    // ==================== ADMIN METHODS ====================

    /**
     * Handle ADMIN_ADD_MOVIE command
     * Format: ADMIN_ADD_MOVIE|title|genre|rating|runtime
     * Response: SUCCESS|Movie added with ID: movieId OR ERROR|<error message>
     */
    private void handleAdminAddMovie(String[] parts) {
        if (!isAuthenticated) {
            sendError(Protocol.ERROR_AUTH_REQUIRED);
            return;
        }

        if (!currentUser.isAdmin()) {
            sendError(Protocol.ERROR_ADMIN_REQUIRED);
            return;
        }

        if (parts.length < 5) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        String title = parts[1];
        String genre = parts[2];
        String rating = parts[3];
        int runtime;

        try {
            runtime = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) {
            sendError("Invalid runtime");
            return;
        }

        synchronized (db) {
            // Check if movie already exists
            for (Movie m : db.getMovies()) {
                if (m.getTitle().equals(title)) {
                    sendError("Movie already exists");
                    return;
                }
            }

            Movie newMovie = new Movie(title, genre, rating, runtime, null);
            db.addMovie(newMovie);
            try {
                db.saveDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendSuccess("Movie added with ID: " + title);
        }
    }

    /**
     * Handle ADMIN_ADD_SHOWTIME command
     * Format: ADMIN_ADD_SHOWTIME|movieId|dateTime|rows|cols|basePrice|auditorium
     * Response: SUCCESS|Showtime added with ID: showtimeId OR ERROR|<error message>
     */
    private void handleAdminAddShowtime(String[] parts) {
        if (!isAuthenticated) {
            sendError(Protocol.ERROR_AUTH_REQUIRED);
            return;
        }

        if (!currentUser.isAdmin()) {
            sendError(Protocol.ERROR_ADMIN_REQUIRED);
            return;
        }

        if (parts.length < 7) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        String movieTitle = parts[1];
        String dateTimeStr = parts[2];
        int rows, cols;
        double basePrice;
        String auditorium = parts[6];

        try {
            rows = Integer.parseInt(parts[3]);
            cols = Integer.parseInt(parts[4]);
            basePrice = Double.parseDouble(parts[5]);
        } catch (NumberFormatException e) {
            sendError("Invalid numeric values");
            return;
        }

        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            sendError("Invalid date format. Use yyyy-MM-dd HH:mm");
            return;
        }

        synchronized (db) {
            // Find the movie
            Movie movie = null;
            for (Movie m : db.getMovies()) {
                if (m.getTitle().equals(movieTitle)) {
                    movie = m;
                    break;
                }
            }

            if (movie == null) {
                sendError("Movie not found");
                return;
            }

            // Check for time conflicts
            for (Showtime st : db.getShowtimes()) {
                if (st.getMovie().getTitle().equals(movieTitle) &&
                        st.getDateTime().equals(dateTime)) {
                    sendError("Time conflict detected");
                    return;
                }
            }

            Showtime newShowtime = new Showtime(movie, dateTime, rows, cols, basePrice, auditorium);
            db.addShowtime(newShowtime);
            try {
                db.saveDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendSuccess("Showtime added with ID: ST_" + (db.getShowtimes().size() - 1));
        }
    }

    /**
     * Handle ADMIN_PROMOTE command
     * Format: ADMIN_PROMOTE|username
     * Response: SUCCESS|User promoted to admin OR ERROR|<error message>
     */
    private void handleAdminPromoteUser(String[] parts) {
        if (!isAuthenticated) {
            sendError(Protocol.ERROR_AUTH_REQUIRED);
            return;
        }

        if (!currentUser.isAdmin()) {
            sendError(Protocol.ERROR_ADMIN_REQUIRED);
            return;
        }

        if (parts.length < 2) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        String username = parts[1];

        synchronized (db) {
            User user = db.findUser(username);

            if (user == null) {
                sendError("User not found");
                return;
            }

            // Note: User class needs setAdmin method as per Phase 2 spec
            // For now, sending success - this will need to be updated when setAdmin is added
            try {
                db.saveDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendSuccess("User promoted to admin");
        }
    }

    /**
     * Handle ADMIN_VIEW_ALL_BOOKINGS command
     * Response: SUCCESS|totalBookings
     *           BOOKING_DETAIL|bookingId|username|movieTitle|dateTime|seats|cost
     *           ...
     *           END_LIST
     */
    private void handleAdminViewAllBookings() {
        if (!isAuthenticated) {
            sendError(Protocol.ERROR_AUTH_REQUIRED);
            return;
        }

        if (!currentUser.isAdmin()) {
            sendError(Protocol.ERROR_ADMIN_REQUIRED);
            return;
        }

        synchronized (db) {
            List<Reservation> allReservations = db.getReservations();
            send(Protocol.SUCCESS + Protocol.DELIMITER + allReservations.size());

            for (Reservation res : allReservations) {
                // Build seat list
                StringBuilder seatList = new StringBuilder();
                ArrayList<Seat> seats = res.getBookedSeats();
                for (int i = 0; i < seats.size(); i++) {
                    if (i > 0) seatList.append(Protocol.SEAT_SEPARATOR);
                    seatList.append(seats.get(i).getSeatLabel());
                }

                // BOOKING_DETAIL|bookingId|username|movieTitle|dateTime|seats|cost
                String bookingData = String.join(Protocol.DELIMITER,
                        Protocol.BOOKING_DETAIL,
                        res.getBookingID(),
                        res.getUser().getUsername(),
                        res.getShowtime().getMovie().getTitle(),
                        res.getShowtime().getDateTime().format(DATE_TIME_FORMATTER),
                        seatList.toString(),
                        String.format("%.2f", res.getTotalPrice())
                );
                send(bookingData);
            }

            send(Protocol.END_LIST);
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Find a showtime by its ID
     * ID format: ST_index (e.g., ST_0, ST_1)
     * @param showtimeId the showtime ID to search for
     * @return the Showtime object or null if not found
     */
    private Showtime findShowtimeById(String showtimeId) {
        if (!showtimeId.startsWith("ST_")) {
            return null;
        }

        try {
            int index = Integer.parseInt(showtimeId.substring(3));
            synchronized (db) {
                List<Showtime> showtimes = db.getShowtimes();
                if (index >= 0 && index < showtimes.size()) {
                    return showtimes.get(index);
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return null;
    }
}
