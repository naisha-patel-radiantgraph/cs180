package server;

import database.Database;
import user.User;
import movie.Movie;
import showtime.Showtime;
import seat.Seat;
import reservation.Reservation;
import interfaces.IClientHandler;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * ClientHandler manages communication with a single connected client.
 * Runs in its own thread and processes all client commands according to the protocol.
 */
public class ClientHandler implements Runnable, IClientHandler {

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
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.db = server.getDatabase();
    }


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
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            // ignore
        }

        if (out != null) {
            out.close();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }


    private void send(String message) {
        out.println(message);
    }

    private void sendError(String message) {
        send(Protocol.ERROR + Protocol.DELIMITER + message);
    }


    private void sendSuccess(String message) {
        send(Protocol.SUCCESS + Protocol.DELIMITER + message);
    }


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


    private void handleRegister(String[] parts) {
        if (parts.length < 4) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        String username = parts[1];
        String password = parts[2];
        String email = parts[3];


        if (!USERNAME_PATTERN.matcher(username).matches()) {
            sendError("Username must be alphanumeric, 3-20 characters");
            return;
        }


        if (password.length() < 6) {
            sendError("Password must be at least 6 characters");
            return;
        }


        if (!EMAIL_PATTERN.matcher(email).matches()) {
            sendError("Invalid email format");
            return;
        }

        synchronized (db) {
            if (db.findUser(username) != null) {
                sendError("Username already exists");
                return;
            }


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


    private void handleLogout() {
        this.currentUser = null;
        this.isAuthenticated = false;
        sendSuccess("Logged out successfully");
    }



    private void handleListMovies() {
        synchronized (db) {
            List<Movie> movies = db.getMovies();
            send(Protocol.SUCCESS + Protocol.DELIMITER + movies.size());

            for (Movie movie : movies) {

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


    private void handleListShowtimes(String[] parts) {
        if (parts.length < 2) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        String movieTitle = parts[1];

        synchronized (db) {
            List<Showtime> allShowtimes = db.getShowtimes();
            List<Integer> indicesForMovie = new ArrayList<Integer>();

            for (int i = 0; i < allShowtimes.size(); i++) {
                Showtime st = allShowtimes.get(i);
                if (st.getMovie().getTitle().equals(movieTitle)) {
                    indicesForMovie.add(Integer.valueOf(i));
                }
            }

            int count = indicesForMovie.size();

            send(Protocol.SUCCESS + Protocol.DELIMITER + count);

            if (count == 0) {
                send(Protocol.END_LIST);
                return;
            }

            for (int j = 0; j < indicesForMovie.size(); j++) {
                int idx = indicesForMovie.get(j).intValue();
                Showtime st = allShowtimes.get(idx);

                int totalSeats = st.getRowCount() * st.getColCount();
                int availableSeats = st.getAvailableSeatCount();

                String showtimeData = String.join(Protocol.DELIMITER,
                        Protocol.SHOWTIME,
                        "ST_" + idx,
                        st.getDateTime().format(DATE_TIME_FORMATTER),
                        String.valueOf(availableSeats),
                        String.valueOf(totalSeats),
                        String.format("%.2f", st.getDynamicPrice()),
                        st.getAuditoriumName() != null ? st.getAuditoriumName() : ""
                );
                send(showtimeData);
            }

            send(Protocol.END_LIST);
        }
    }


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

        if (seatCount <= 0) {
            sendError("Seat count must be positive");
            return;
        }

        if (parts.length != 3 + seatCount + 3) {
            sendError(Protocol.ERROR_INVALID_FORMAT);
            return;
        }

        Showtime showtime = findShowtimeById(showtimeId);
        if (showtime == null) {
            sendError("Showtime not found");
            return;
        }

        // *** Phase 3 addition to check if showtime has started ***
        if (showtime.hasStarted()) {
            sendError("Time to book seats has expired");
            return;
        }

        // *** End of phase 3 addition ***

        List<int[]> seatPositions = new ArrayList<>();
        for (int i = 3; i < 3 + seatCount; i++) {
            String[] rowCol = parts[i].split(Protocol.SEAT_DELIMITER);
            if (rowCol.length != 2) {
                sendError("Invalid seat format (expected row:col)");
                return;
            }

            try {
                int userRow = Integer.parseInt(rowCol[0]);
                int userCol = Integer.parseInt(rowCol[1]);


                int row = userRow - 1;
                int col = userCol - 1;

                seatPositions.add(new int[]{row, col});
            } catch (NumberFormatException e) {
                sendError("Invalid numeric seat coordinates");
                return;
            }
        }


        for (int i = 0; i < seatPositions.size(); i++) {
            for (int j = i + 1; j < seatPositions.size(); j++) {
                int[] a = seatPositions.get(i);
                int[] b = seatPositions.get(j);
                if (a[0] == b[0] && a[1] == b[1]) {
                    sendError("Duplicate seat selection detected");
                    return;
                }
            }
        }

        synchronized (showtime) {


            for (int[] pos : seatPositions) {
                int row = pos[0];
                int col = pos[1];

                if (row < 0 || row >= showtime.getRowCount() ||
                        col < 0 || col >= showtime.getColCount()) {
                    sendError("Seat out of range");
                    return;
                }

                if (!showtime.isSeatAvailable(row, col)) {
                    sendError("One or more selected seats are already booked");
                    return;
                }
            }


            ArrayList<Seat> bookedSeats = new ArrayList<>();
            for (int[] pos : seatPositions) {
                Seat seat = new Seat(pos[0], pos[1], showtime.getDynamicPrice());
                bookedSeats.add(seat);
            }
            String cardNumber = parts[parts.length - 3];
            String expiry = parts[parts.length - 2];
            String cvv = parts[parts.length - 1];

            Reservation reservation = new Reservation (currentUser, showtime, bookedSeats, cardNumber, expiry, cvv);

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

            sendSuccess(
                    bookingId + Protocol.DELIMITER +
                            String.format("%.2f", totalCost) + Protocol.DELIMITER +
                            "Booking confirmed"
            );
        }
    }


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


            if (!reservation.getUser().getUsername().equals(currentUser.getUsername())) {
                sendError("Not authorized to cancel this booking");
                return;
            }


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


    private void handleMyBookings() {
        if (!isAuthenticated) {
            sendError(Protocol.ERROR_AUTH_REQUIRED);
            return;
        }

        synchronized (db) {
            List<Reservation> userReservations = currentUser.getReservations();
            send(Protocol.SUCCESS + Protocol.DELIMITER + userReservations.size());

            for (int i = 0; i < userReservations.size(); i++) {
                Reservation res = userReservations.get(i);

                StringBuilder seatList = new StringBuilder();
                ArrayList<Seat> seats = res.getBookedSeats();
                for (int j = 0; j < seats.size(); j++) {
                    if (j > 0) {
                        seatList.append(Protocol.SEAT_SEPARATOR);
                    }
                    seatList.append(seats.get(j).getSeatLabel());
                }


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
            if (db.movieExists(title)) {
                sendError("Movie already exists");
                return;
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
        int rows;
        int cols;
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
            Movie movie = null;
            List<Movie> allMovies = db.getMovies();
            for (int i = 0; i < allMovies.size(); i++) {
                Movie m = allMovies.get(i);
                if (m.getTitle().equals(movieTitle)) {
                    movie = m;
                    break;
                }
            }

            if (movie == null) {
                sendError("Movie not found");
                return;
            }


            if (db.isShowtimeConflict(movie, dateTime)) {
                sendError("Time conflict detected for this movie and time");
                return;
            }

            Showtime newShowtime = new Showtime(movie, dateTime, rows, cols, basePrice, auditorium);
            db.addShowtime(newShowtime);
            try {
                db.saveDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }


            int newIndex = db.getShowtimes().size() - 1;
            sendSuccess("Showtime added with ID: ST_" + newIndex);
        }
    }


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

            if (user.isAdmin()) {
                sendError("User is already an admin");
                return;
            }

            // Actually promote the user and persist
            db.promoteUserToAdmin(username);

            // If the current user just promoted themselves, update flag
            if (currentUser.getUsername().equals(username)) {
                currentUser.setAdmin(true);
            }

            sendSuccess("User promoted to admin");
        }
    }


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

            for (int i = 0; i < allReservations.size(); i++) {
                Reservation res = allReservations.get(i);


                StringBuilder seatList = new StringBuilder();
                ArrayList<Seat> seats = res.getBookedSeats();
                for (int j = 0; j < seats.size(); j++) {
                    if (j > 0) {
                        seatList.append(Protocol.SEAT_SEPARATOR);
                    }
                    seatList.append(seats.get(j).getSeatLabel());
                }


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
