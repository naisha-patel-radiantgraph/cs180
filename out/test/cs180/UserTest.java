package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.User;
import showtime.Showtime;
import reservation.Reservation;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User regularUser;
    private User adminUser;

    @BeforeEach
    public void setUp() {
        regularUser = new User("john_doe", "password123", "john@example.com", false);
        adminUser = new User("admin_user", "securePass!", "admin@example.com", true);
    }

    @Test
    public void testGetUsername() {
        assertEquals("john_doe", regularUser.getUsername());
    }

    @Test
    public void testGetEmail() {
        assertEquals("john@example.com", regularUser.getEmail());
    }

    @Test
    public void testIsAdminFlag() {
        assertFalse(regularUser.isAdmin());
        assertTrue(adminUser.isAdmin());
    }

    @Test
    public void testPasswordIsHashed() {
        String storedHash = regularUser.getPasswordHash();
        assertNotEquals("password123", storedHash, "Password should be stored as a hash, not plain text");
    }

    @Test
    public void testVerifyPasswordCorrect() {
        assertTrue(regularUser.verifyPassword("password123"), "Correct password should pass verification");
    }

    @Test
    public void testVerifyPasswordIncorrect() {
        assertFalse(regularUser.verifyPassword("wrongpass"), "Incorrect password should fail verification");
    }

    @Test
    public void testEnsureHashConsistency() {
        String hash1 = regularUser.getPasswordHash();
        String hash2 = regularUser.getPasswordHash();
        assertEquals(hash1, hash2, "Hash should remain consistent for the same password");
    }

    @Test
    public void testAddReservation() {
        // dummy setup
        Showtime s = new Showtime(
                new movie.Movie("Matrix", "Sci-Fi", "PG-13", 136, "matrix.jpg"),
                java.time.LocalDateTime.now(),
                5, 5, 10.0,
                "Auditorium 1"
        );

        seat.Seat seat = new seat.Seat(1, 2, 12.5);
        ArrayList<seat.Seat> seats = new ArrayList<>();
        seats.add(seat);

        Reservation r1 = new Reservation(regularUser, s, seats);
        regularUser.addReservation(r1);

        assertEquals(1, regularUser.getReservations().size());
        assertTrue(regularUser.getReservations().contains(r1));
    }

    @Test
    public void testRemoveReservation() {
        Showtime s = new Showtime(
                new movie.Movie("Avatar", "Fantasy", "PG-13", 162, "avatar.jpg"),
                java.time.LocalDateTime.now(),
                5, 5, 10.0,
                "Auditorium 2"
        );

        seat.Seat seat = new seat.Seat(0, 0, 9.0);
        ArrayList<seat.Seat> seats = new ArrayList<>();
        seats.add(seat);

        Reservation r1 = new Reservation(regularUser, s, seats);
        regularUser.addReservation(r1);

        assertEquals(1, regularUser.getReservations().size());

        regularUser.removeReservation(r1.getBookingID());
        assertEquals(0, regularUser.getReservations().size());
    }

    @Test
    public void testMultipleReservations() {
        Showtime s1 = new Showtime(
                new movie.Movie("Interstellar", "Sci-Fi", "PG-13", 169, "poster1.jpg"),
                java.time.LocalDateTime.now(),
                5, 5, 10.0,
                "Auditorium 1"
        );

        Showtime s2 = new Showtime(
                new movie.Movie("Tenet", "Action", "PG-13", 150, "poster2.jpg"),
                java.time.LocalDateTime.now().plusHours(3),
                5, 5, 10.0,
                "Auditorium 2"
        );

        seat.Seat seat1 = new seat.Seat(0, 0, 9.0);
        seat.Seat seat2 = new seat.Seat(1, 1, 9.0);

        ArrayList<seat.Seat> seats1 = new ArrayList<>();
        seats1.add(seat1);

        ArrayList<seat.Seat> seats2 = new ArrayList<>();
        seats2.add(seat2);

        Reservation r1 = new Reservation(regularUser, s1, seats1);
        Reservation r2 = new Reservation(regularUser, s2, seats2);

        regularUser.addReservation(r1);
        regularUser.addReservation(r2);

        assertEquals(2, regularUser.getReservations().size());
        assertTrue(regularUser.getReservations().contains(r1));
        assertTrue(regularUser.getReservations().contains(r2));
    }

    @Test
    public void testAddNullReservationIgnored() {
        regularUser.addReservation(null);
        assertEquals(0, regularUser.getReservations().size(), "Adding null reservation should not modify list");
    }

    @Test
    public void testRemoveNonExistentReservationDoesNothing() {
        // no reservations added yet
        regularUser.removeReservation("fakeID");
        assertEquals(0, regularUser.getReservations().size(),
                "Removing invalid ID should not throw errors or modify the list");
    }

    @Test
    public void testToStringOrHashCodeNotNull() {
        assertNotNull(regularUser.toString());
        assertNotNull(regularUser.hashCode());
    }

}

