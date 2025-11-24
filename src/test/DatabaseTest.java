package test;


import database.Database;
import movie.Movie;
import reservation.Reservation;
import seat.Seat;
import showtime.Showtime;
import user.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class DatabaseTest {

    private Database db;
    private User testUser;
    private Movie testMovie;
    private Showtime testShowtime;
    private Reservation testReservation;
    private ArrayList<Seat> bookedSeats;

    @BeforeEach
    public void setUp() {
        db = new Database();


        testUser = new User("gaurav", "password123", "gaurav@purdue.edu", false);
        testMovie = new Movie("Inception", "Sci-Fi", "PG-13", 148, "posters/inception.jpg");
        testShowtime = new Showtime(
                testMovie,
                LocalDateTime.of(2025, 11, 9, 20, 0),
                5,
                5,
                10.0,
                "Auditorium 1"
        );


        bookedSeats = new ArrayList<>();
        bookedSeats.add(new Seat(0, 0, 10.0));
        bookedSeats.add(new Seat(0, 1, 10.0));


        testReservation = new Reservation(testUser, testShowtime, bookedSeats);
    }

    @AfterEach
    public void tearDown() {
        db.clearAll();
        File f = new File("myDataBase.ser");
        if (f.exists()) f.delete();
    }

    @Test
    public void testAddAndFindUser() {
        db.addUser(testUser);
        User found = db.findUser("gaurav");
        assertNotNull(found, "User should be found after addition");
        assertEquals("gaurav", found.getUsername());
    }

    @Test
    public void testRemoveUser() {
        db.addUser(testUser);
        db.removeUser("gaurav");
        assertNull(db.findUser("gaurav"), "User should be removed successfully");
    }

    @Test
    public void testAddMovieAndGetMovies() {
        db.addMovie(testMovie);
        List<Movie> movies = db.getMovies();
        assertEquals(1, movies.size(), "Exactly one movie should exist in DB");
        assertEquals("Inception", movies.get(0).getTitle());
    }

    @Test
    public void testRemoveMovie() {
        db.addMovie(testMovie);
        db.removeMovie("Inception");
        assertTrue(db.getMovies().isEmpty(), "Movie list should be empty after removal");
    }

    @Test
    public void testAddAndFindShowtime() {
        db.addShowtime(testShowtime);
        Showtime found = db.findShowtime(testMovie, testShowtime.getDateTime());
        assertNotNull(found, "Showtime should be found in DB");
        assertEquals(testShowtime.getDateTime(), found.getDateTime());
    }

    @Test
    public void testAddAndFindReservation() {
        db.addReservation(testReservation);
        Reservation found = db.findReservation(testReservation.getBookingID());
        assertNotNull(found, "Reservation should be retrievable by booking ID");
        assertEquals(testUser.getUsername(), found.getUser().getUsername());
    }

    public void testRemoveReservation() {
        db.addReservation(testReservation);
        db.removeReservation(testReservation.getBookingID());
        assertNull(db.findReservation(testReservation.getBookingID()), "Reservation should be removed");
    }

    @Test
    public void testSaveAndLoadDatabase() throws Exception {
        db.addUser(testUser);
        db.addMovie(testMovie);
        db.addShowtime(testShowtime);
        db.addReservation(testReservation);


        db.saveDatabase();


        Database loaded = (Database) db.loadDatabase();

        assertNotNull(loaded.findUser("gaurav"), "User should persist after reload");
        assertNotNull(loaded.findReservation(testReservation.getBookingID()), "Reservation should persist after reload");
        assertEquals("Inception", loaded.getMovies().get(0).getTitle(), "Movie title should persist correctly");
    }

    @Test
    public void testClearAll() {
        db.addUser(testUser);
        db.addMovie(testMovie);
        db.addShowtime(testShowtime);
        db.addReservation(testReservation);
        db.clearAll();

        assertTrue(db.getMovies().isEmpty(), "Movies should be cleared");
        assertTrue(db.getReservations().isEmpty(), "Reservations should be cleared");
        assertTrue(db.getShowtimes().isEmpty(), "Showtimes should be cleared");
        assertNull(db.findUser("gaurav"), "Users should be cleared");
    }

    @Test
    public void testSeatBookingReflectsInShowtime() {
        db.addShowtime(testShowtime);
        assertFalse(testShowtime.isSeatAvailable(0, 0), "Seat (0,0) should be booked after reservation");
        assertFalse(testShowtime.isSeatAvailable(0, 1), "Seat (0,1) should be booked after reservation");
        assertTrue(testShowtime.isSeatAvailable(0, 2), "Seat (0,2) should still be available");
    }

    // --- PHASE 2 TESTS ---

    @Test
    public void testMovieExists() {
        db.addMovie(testMovie);
        assertTrue(db.movieExists("Inception"), "Existing movie should return true.");
        assertFalse(db.movieExists("Non-existent"), "Non-existent movie should return false.");
    }

    @Test
    public void testUsernameExists() {
        db.addUser(testUser);
        assertTrue(db.usernameExists("gaurav"), "Username should exist and should return true.");
        assertFalse(db.usernameExists("unknown"), "Username should not exist and should return false.");
    }

    @Test
    public void testIsShowtimeConflict() {
        db.addShowtime(testShowtime);
        assertTrue(db.isShowtimeConflict(testMovie, testShowtime.getDateTime()),
                "Should detect conflict for same movie and time.");
        assertFalse(db.isShowtimeConflict(testMovie, testShowtime.plusHours(3)),
                "Should not conflict with different.");
    }

    @Test
    public void testPromoteUserToAdmin() {
        db.addUser(testUser);
        assertFalse(db.findUser("gaurav").isAdmin(), "User shouldn't be an admin.");

        db.promoteUserToAdmin("gaurav");

        User promoted =  db.findUser("gaurav");
        assertTrue(promoted.isAdmin(), "User should be promoted to the admin.");
    }

}
