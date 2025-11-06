package interfaces;

import java.time.LocalDateTime;

/**
 * Public interface for Showtime domain objects.
 *
 * Represents a specific screening of a movie at a given date/time and an
 * associated seating chart. Implementations must ensure thread-safety for
 * booking/cancellation operations because the server supports multiple
 * simultaneous clients and seat state must remain consistent.
 *
 * Suggested implementation notes:
 * - Use synchronization, java.util.concurrent.locks, or atomics to protect
 *   seat booking/cancellation so two threads cannot double-book the same seat.
 * - The seat indices used by the methods are zero-based by convention (row 0..rows-1,
 *   col 0..cols-1). Document any different convention in the implementor class.
 * - Implementations may throw IndexOutOfBoundsException for invalid row/col inputs.
 */
public interface Showtime {

    /**
     * Returns the Movie shown at this showtime.
     * @return non-null Movie instance
     */
    Movie getMovie();

    /**
     * Returns the scheduled start date & time for this showtime.
     * @return LocalDateTime representing the show start
     */
    LocalDateTime getDateTime();

    /**
     * Returns the Seat object at the provided row/column.
     *
     * Implementations should define whether returned Seat objects are live
     * references to the internal seating chart or defensive copies.
     *
     * @param row zero-based row index
     * @param col zero-based column index
     * @return Seat at the requested location
     */
    Seat getSeat(int row, int col);

    /**
     * Attempts to book the seat at the specified position.
     *
     * This method should be atomic: it must return true only if the seat was
     * available and is now booked by the caller; if the seat was already booked
     * it should return false and not modify state.
     *
     * @param row zero-based row index
     * @param col zero-based column index
     * @return true if the booking succeeded, false if the seat was already booked
     */
    boolean bookSeat(int row, int col);

    /**
     * Attempts to cancel the booking for the seat at the specified position.
     *
     * This method should be atomic and return true only if the seat was booked
     * and is now freed.
     *
     * @param row zero-based row index
     * @param col zero-based column index
     * @return true if cancellation succeeded, false if the seat was not booked
     */
    boolean cancelSeat(int row, int col);

    /**
     * Returns whether a seat at the given location is currently available.
     *
     * @param row zero-based row index
     * @param col zero-based column index
     * @return true if the seat is free (not booked), false otherwise
     */
    boolean isSeatAvailable(int row, int col);

    /**
     * Returns the number of currently unbooked seats for this showtime.
     *
     * Implementations should compute this efficiently if possible, or cache
     * the value and update it on each book/cancel operation while respecting
     * thread-safety.
     *
     * @return count of available seats (>= 0)
     */
    int getAvailableSeatCount();

    /**
     * Returns the base ticket price for this showtime. Individual seats may
     * override this via their Seat.getPrice().
     *
     * @return base price in the same currency units used by the application
     */
    double getBasePrice();

    /**
     * Updates the base ticket price for this showtime.
     * @param price new base price (implementations should validate non-negative)
     */
    void setBasePrice(double price);

    /**
     * Returns the auditorium name / identifier for this showtime (e.g. "Auditorium A").
     * @return auditorium name; may be null if not set
     */
    String getAuditoriumName();

    /**
     * Sets or updates the auditorium name/identifier.
     * @param name auditorium name; may be null to clear
     */
    void setAuditoriumName(String name);

    /**
     * Returns the number of rows in the seating chart.
     * @return positive integer rows count
     */
    int getRowCount();

    /**
     * Returns the number of columns (seats per row) in the seating chart.
     * @return positive integer column count
     */
    int getColCount();

    /**
     * Returns the full seating chart as a 2D Seat array. Implementations may
     * return the internal array or a defensive copy depending on design.
     *
     * @return Seat[][] with dimensions [rows][cols]
     */
    Seat[][] getSeats();
}
