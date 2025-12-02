package test;

import user.User;
import movie.Movie;
import showtime.Showtime;
import seat.Seat;
import reservation.Reservation;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationTest {

    private User testUser;
    private Movie testMovie;
    private Showtime testShowtime;
    private Seat seat1;
    private Seat seat2;
    private double expectedPrice;
    private Reservation reservation;

    @BeforeEach
    public void setUp() {
        testUser = new User("testUser", "pass123", "test@example.com", false);
        testMovie = new Movie("Dune: Part Two", "Sci-Fi", "PG-13", 166, null);
        LocalDateTime testDateTime = LocalDateTime.of(2025, 11, 15, 20, 0);

        int rows = 5;
        int cols = 5;
        double basePrice = 12.50;
        double premiumPrice = 18.00;
        Seat[][] seatingChart = new Seat[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double price = (r == 4) ? premiumPrice : basePrice;
                seatingChart[r][c] = new Seat(r, c, price);
            }
        }

        testShowtime = new Showtime(testMovie, testDateTime, seatingChart, basePrice, "Auditorium 1");

        ArrayList<Seat> seatsToBook = new ArrayList<>();
        seat1 = testShowtime.getSeat(2,3);
        seat2 = testShowtime.getSeat(4, 4);
        seatsToBook.add(seat1);
        seatsToBook.add(seat2);

        expectedPrice = seat1.getPrice() + seat2.getPrice();

        reservation = new Reservation(testUser, testShowtime, seatsToBook, "1234567891011121", "02/27", "123");
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals(testUser, reservation.getUser(), "getUser() returned incorrect user.");
        assertEquals(testShowtime, reservation.getShowtime(), "getShowtime() returned incorrect showtime.");
        assertEquals(2, reservation.getBookedSeats().size(), "getBookedSeats() list size is wrong.");
        assertTrue(reservation.getBookedSeats().contains(seat1), "Booked seats list missing seat1.");
        assertTrue(reservation.getBookedSeats().contains(seat2), "Booked seats list missing seat2.");
        assertNotNull(reservation.getBookingID(), "Booking ID was null.");
        assertFalse(reservation.getBookingID().isEmpty(), "Booking ID was empty.");
        assertNotNull(reservation.getBookingTime(), "Booking time was null.");
    }

    @Test
    public void testTotalPriceCalculation() {
        assertEquals(expectedPrice, reservation.getTotalPrice(), 0.001, "getTotalPrice() was incorrect.");
    }

    @Test
    public void testShowtimeStateAfterBooking() {
        assertFalse(testShowtime.isSeatAvailable(seat1.getRow(), seat1.getNumber()),
                "Seat 1 was not marked as unavailable in Showtime.");
        assertFalse(testShowtime.isSeatAvailable(seat2.getRow(), seat2.getNumber()),
                "Seat 2 was not marked as unavailable in Showtime.");
    }

    @Test
    public void testReservationCancellation() {
        reservation.cancelAllSeats();

        assertTrue(reservation.getBookedSeats().isEmpty(),
                "cancelAllSeats() did not clear the internal list.");
        assertTrue(testShowtime.isSeatAvailable(seat1.getRow(), seat1.getNumber()),
                "Seat 1 was not freed in Showtime after cancellation.");
        assertTrue(testShowtime.isSeatAvailable(seat2.getRow(), seat2.getNumber()),
                "Seat 2 was not freed in Showtime after cancellation.");
    }

    // --- PHASE 2 TESTS ---

    @Test
    public void testSummary() {
        String summary = reservation.summary();

        assertNotNull(summary, "summary should not be null.");
        assertTrue(summary.contains(reservation.getBookingID()), "Summary should contain the booking ID.");
        assertTrue(summary.contains("Dune: Part Two"), "Summary should contain the movie title.");
        assertTrue(summary.contains("Auditorium 1"), "Summary should contain the auditorium name.");

        String priceString = String.format("%.2f", reservation.getTotalPrice());
        assertTrue(summary.contains(priceString), "Summary should contain correctly formatted price.");

        assertTrue(summary.contains(seat1.getSeatLabel()), "Summary should contain seat 1 label.");
        assertTrue(summary.contains(seat2.getSeatLabel()), "Summary should contain seat 2 label");
    }

}