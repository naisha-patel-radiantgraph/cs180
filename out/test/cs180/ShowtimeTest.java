package showtime;

import movie.Movie;
import seat.Seat;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ShowtimeTest {

    private Movie exampleMovie() {
        return new Movie("Inception", "Sci-Fi", "PG-13", 148, null);
    }

    @Test
    void testConstructorAndGetters() {
        Movie m = exampleMovie();
        LocalDateTime now = LocalDateTime.now();
        Seat[][] seats = new Seat[2][3];
        seats[1][1] = new Seat(1, 1, 12.5);
        Showtime showtime = new Showtime(m, now, seats, 10.0, "Aud 1");
        assertEquals(m, showtime.getMovie());
        assertEquals(now, showtime.getDateTime());
        assertEquals(seats[1][1], showtime.getSeat(1, 1));
        assertEquals("Aud 1", showtime.getAuditoriumName());
        assertEquals(10.0, showtime.getBasePrice());
    }

    @Test
    void testConstructorRejectsNullMovie() {
        assertThrows(IllegalArgumentException.class, () ->
                new Showtime(null, LocalDateTime.now(), new Seat[1][1], 10.0, "Main"));
    }

    @Test
    void testConstructorRejectsNullDateTime() {
        assertThrows(IllegalArgumentException.class, () ->
                new Showtime(exampleMovie(), null, new Seat[1][1], 10.0, "Main"));
    }

    @Test
    void testConstructorRejectsNullSeats() {
        assertThrows(IllegalArgumentException.class, () ->
                new Showtime(exampleMovie(), LocalDateTime.now(), null, 10.0, "Main"));
    }

    @Test
    void testConstructorRejectsNegativeBasePrice() {
        assertThrows(IllegalArgumentException.class, () ->
                new Showtime(exampleMovie(), LocalDateTime.now(), new Seat[1][1], -1.0, "Main"));
    }

    @Test
    void testBookSeatAndCancelSeat() {
        Showtime showtime = new Showtime(exampleMovie(), LocalDateTime.now(), 2, 2, 8.0, "Little");
        assertTrue(showtime.isSeatAvailable(0, 0));
        assertTrue(showtime.bookSeat(0, 0));
        assertFalse(showtime.isSeatAvailable(0, 0));
        assertFalse(showtime.bookSeat(0, 0));
        assertTrue(showtime.cancelSeat(0, 0));
        assertTrue(showtime.isSeatAvailable(0, 0));
        assertFalse(showtime.cancelSeat(0, 0));
    }

    @Test
    void testGetAvailableSeatCount() {
        Showtime showtime = new Showtime(exampleMovie(), LocalDateTime.now(), 2, 2, 12, null);
        assertEquals(4, showtime.getAvailableSeatCount());
        showtime.bookSeat(0,0);
        showtime.bookSeat(1,1);
        assertEquals(2, showtime.getAvailableSeatCount());
    }

    @Test
    void testSetAndGetBasePrice() {
        Showtime showtime = new Showtime(exampleMovie(), LocalDateTime.now(), 1, 1, 5.0, null);
        showtime.setBasePrice(7.5);
        assertEquals(7.5, showtime.getBasePrice());
    }

    @Test
    void testSetBasePriceRejectsNegative() {
        Showtime showtime = new Showtime(exampleMovie(), LocalDateTime.now(), 1, 1, 5.0, null);
        assertThrows(IllegalArgumentException.class, () -> showtime.setBasePrice(-8.2));
    }

    @Test
    void testAuditoriumNameMutator() {
        Showtime showtime = new Showtime(exampleMovie(), LocalDateTime.now(), 1, 1, 5.0, "Old Name");
        showtime.setAuditoriumName("New Name");
        assertEquals("New Name", showtime.getAuditoriumName());
    }

    @Test
    void testSeatBounds() {
        Showtime showtime = new Showtime(exampleMovie(), LocalDateTime.now(), 1, 1, 5.0, null);
        assertThrows(IndexOutOfBoundsException.class, () -> showtime.getSeat(1,1));
    }

    @Test
    public void testGetDynamicPrice() {
        Movie m = new Movie("DynamicMovie", "Drama", "PG", 120, null);

        Showtime st = new Showtime(
                m,
                LocalDateTime.of(2025, 1, 1, 20, 0),
                3, 4,
                10.0,              // base price
                "MainHall"
        );

        double base = 10.0;

        assertEquals(base, st.getDynamicPrice(), 0.0001,
                "Price should equal base price when no seats booked");


        for (int i = 0; i < 6; i++) {
            int row = i / 4;
            int col = i % 4;
            st.bookSeat(row, col);
        }

        double expected50 = base * (1 + 0.5);   // 10 * 1.5 = 15
        assertEquals(expected50, st.getDynamicPrice(), 0.0001,
                "Price should be 150% of base at 50% occupancy");

        for (int i = 6; i < 10; i++) {
            int row = i / 4;
            int col = i % 4;
            st.bookSeat(row, col);
        }

        double ratio83 = 10.0 / 12.0;
        double expected83 = base * (1 + ratio83);

        assertEquals(expected83, st.getDynamicPrice(), 0.0001,
                "Price should scale smoothly with occupancy (~83%)");
    }

    @Test
    void testHasStarted() {
        Movie m =  exampleMovie();

        Showtime pastShowtime = new Showtime(m, LocalDateTime.now().minusHours(1), 1, 1, 10.0, "Aud1");
        assertTrue(pastShowtime.hasStarted(), "Should return true for past showtimes");

        Showtime futureShowtime = new Showtime(m, LocalDateTime.now().plusHours(1), 1, 1, 10.0, "Aud1");
        assertFalse(futureShowtime.hasStarted(), "Should return false for future showtimes");
    }
}
