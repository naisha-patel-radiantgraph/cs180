package interfaces;

import user.User;
import reservation.Reservation;
import movie.Movie;
import showtime.Showtime;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface IDatabase {
    void addUser(User u);                         // Adds a user to the database
    void removeUser(String username);             // Removes a user by username
    User findUser(String username);               // Finds and returns a user by username
    List<User> getUsers();                        // Returns all registered users

    void addMovie(Movie m);                       // Adds a new movie
    void removeMovie(String title);               // Removes a movie by its title
    List<Movie> getMovies();                      // Returns all available movies

    void addShowtime(Showtime s);                 // Adds a showtime to the list
    Showtime findShowtime(Movie m, LocalDateTime dt); // Finds a showtime for given movie and time
    List<Showtime> getShowtimes();                // Returns all showtimes

    void addReservation(Reservation r);
    void removeReservation(String bookingID); // Removes reservation by booking ID
    Reservation findReservation(String bookingID);// Finds reservation by booking ID
    List<Reservation> getReservations();          // Returns all reservations

    void saveDatabase() throws IOException;       // Saves serialized database to file
    IDatabase loadDatabase() throws IOException, ClassNotFoundException; // Loads database from file

    void clearAll();  // Clears all stored data (used for testing)

    // --- PHASE 2 ADDITIONS ---

    boolean usernameExists(String username);

    boolean movieExists(String title);

    boolean isShowtimeConflict(Movie m, LocalDateTime dt);

    void promoteUserToAdmin(String username);
}
