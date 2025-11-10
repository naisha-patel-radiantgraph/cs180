package seat;

import interfaces.ISeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SeatTest {

    private ISeat seat;

    @BeforeEach
    public void setUp() {
        seat = new Seat(2, 7, 16.79); // Should produce label C8
    }

    @Test
    public void testInitialValues() {
        assertEquals(2, seat.getRow());
        assertEquals(7, seat.getNumber());
        assertEquals(16.79, seat.getPrice());
        assertFalse(seat.isBooked());
        assertEquals("C8", seat.getSeatLabel());
    }

    @Test
    public void testBookSeat() {
        assertFalse(seat.isBooked());
        seat.book();
        assertTrue(seat.isBooked());
    }

    @Test
    public void testCancelSeat() {
        seat.book();
        assertTrue(seat.isBooked());
        seat.cancel();
        assertFalse(seat.isBooked());
    }

    @Test
    public void testDoubleBookingDoesNotUnbook() {
        seat.book();
        seat.book();
        assertTrue(seat.isBooked());
    }

    @Test
    public void testCancelWhenNotBookedDoesNothing() {
        assertFalse(seat.isBooked());
        seat.cancel();
        assertFalse(seat.isBooked());
    }

    @Test
    public void testPriceUpdate() {
        seat.setPrice(25.89);
        assertEquals(25.89, seat.getPrice());
    }

    @Test
    public void testAnotherSeatLabel() {
        ISeat secondSeat = new Seat(6, 8, 16.09); // Should produce label G9
        assertEquals("G9", secondSeat.getSeatLabel());
    }
}
