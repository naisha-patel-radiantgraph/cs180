package showtime;

import org.junit.Test;
import static org.junit.Assert.*;

import movie.Movie;
import seat.Seat;

import java.time.LocalDateTime;

public class ShowtimeTest {

    private Movie exampleMovie() {
        return new Movie("Inception", "Sci-Fi", "PG-13", 148, null);
    }

    @Test
    public void testConstructorAndGetters() {
        Movie m = exampleMovie();
        LocalDateTime now = LocalDateTime.now();
        Seat[][] seats = new Seat[2][3];
        seats[1][1] = new Seat("A2", 12.5);
        Showtime showtime = new Showtime(m, now, seats, 10.0, "Aud 1");
        assertEquals(m, showtime.getMovie());
        assertEquals(now, showtime.getDateTime());
        assertEquals(seats[1][1], showtime.getSeat(1, 1));
        assertEquals("Aud 1", showtime.getAuditoriumName());
        assertEquals(10.0, showtime.getBasePrice(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullMovie() {
        new Showtime(null, LocalDateTime.now(), new Seat[1][1], 10.0, "Main");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullDateTime() {
        new Showtime(exampleMovie(), null, new Seat[1][1], 10.0, "Main");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullSeats() {
        new Showtime(exampleMovie(), LocalDateTime.now(), null, 10.0, "Main");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNegativeBasePrice() {
        new Showtime(exampleMovie(), LocalDateTime.now(), new Seat[1][1], -1.0, "Main");
    }

    @Test
    public void testBookSeatAndCancelSeat() {
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
    public void testGetAvailableSeatCount() {
        Showtime showtime = new Showtime(exampleMovie(), LocalDateTime.now(), 2, 2, 12, null);
        assertEquals(4, showtime.getAvailableSeatCount());
        showtime.bookSeat(0,0);
        showtime.bookSeat(1,1);
        assertEquals(2, showtime.getAvailableSeatCount());
    }

    @Test
    public void testSetAndGetBasePrice() {
        Showtime showtime = new Showtime(exampleMovie(), LocalDateTime.now(), 1, 1, 5.0, null);
        showtime.setBasePrice(7.5);
        assertEquals(7.5, showtime.getBasePrice(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBasePriceRejectsNegative() {
        Showtime showtime = new Showtime(exampleMovie(), LocalDateTime.now(), 1, 1, 5.0, null);
        showtime.setBasePrice(-8.2);
    }

    @Test
    public void testAuditoriumNameMutator() {
        Showtime showtime = new Showtime(exampleMovie(), LocalDateTime.now(), 1, 1, 5.0, "Old Name");
        showtime.setAuditoriumName("New Name");
        assertEquals("New Name", showtime.getAuditoriumName());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSeatBounds() {
        Showtime showtime = new Showtime(exampleMovie(), LocalDateTime.now(), 1, 1, 5.0, null);
        showtime.getSeat(1,1); // Only row/col 0/0 is valid
    }
}
