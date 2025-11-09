package showtime;

import interfaces.IShowtime;
import movie.Movie;
import seat.Seat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Implementation of IShowtime that keeps a seating chart and a parallel booked[][] matrix.
 *
 * Notes:
 * - Booking/cancellation/read methods that rely on booking state are synchronized to ensure thread-safety.
 * - Seat objects are stored (if provided) but booking state is tracked in booked[][]. This avoids
 *   depending on any particular Seat implementation's internal synchronization.
 * - Row/col parameters are zero-based. IndexOutOfBoundsException is thrown for invalid coordinates.
 */
public class Showtime implements IShowtime, Serializable {

    private final Movie movie;
    private final LocalDateTime dateTime;
    private final Seat[][] seats;      // may contain nulls if Seat objects are not provided
    private final boolean[][] booked;  // true means booked
    private double basePrice;
    private String auditoriumName;

    /**
     * Construct a showtime with a provided seating chart.
     *
     * @param movie         non-null movie
     * @param dateTime      non-null show LocalDateTime
     * @param seats         non-null 2D Seat array with dimensions [rows][cols]; entries may be null
     * @param basePrice     initial base price (non-negative)
     * @param auditoriumName optional auditorium name
     */
    public Showtime(Movie movie, LocalDateTime dateTime, Seat[][] seats, double basePrice, String auditoriumName) {
        if (movie == null) throw new IllegalArgumentException("movie cannot be null");
        if (dateTime == null) throw new IllegalArgumentException("dateTime cannot be null");
        if (seats == null) throw new IllegalArgumentException("seats cannot be null");
        if (basePrice < 0) throw new IllegalArgumentException("basePrice cannot be negative");

        this.movie = movie;
        this.dateTime = dateTime;
        this.seats = new Seat[seats.length][];
        for (int r = 0; r < seats.length; r++) {
            if (seats[r] == null) throw new IllegalArgumentException("seat row cannot be null");
            this.seats[r] = new Seat[seats[r].length];
            System.arraycopy(seats[r], 0, this.seats[r], 0, seats[r].length);
        }
        this.booked = new boolean[this.seats.length][this.getColCount()];
        this.basePrice = basePrice;
        this.auditoriumName = auditoriumName;
    }

    /**
     * Convenience constructor to create an empty seating chart of the given size.
     * Seat objects will be null; callers can provide Seat objects later by modifying the array returned by getSeats()
     * (if they choose to). getSeat() will return null for positions with no Seat provided.
     *
     * @param movie     non-null Movie
     * @param dateTime  non-null LocalDateTime
     * @param rows      positive number of rows
     * @param cols      positive number of columns
     * @param basePrice base price (non-negative)
     */
    public Showtime(Movie movie, LocalDateTime dateTime, int rows, int cols, double basePrice, String auditoriumName) {
        if (rows <= 0 || cols <= 0) throw new IllegalArgumentException("rows and cols must be positive");
        Seat[][] s = new Seat[rows][cols];
        this.movie = Objects.requireNonNull(movie, "movie cannot be null");
        this.dateTime = Objects.requireNonNull(dateTime, "dateTime cannot be null");
        this.seats = s;
        this.booked = new boolean[rows][cols];
        if (basePrice < 0) throw new IllegalArgumentException("basePrice cannot be negative");
        this.basePrice = basePrice;
        this.auditoriumName = auditoriumName;
    }

    @Override
    public Movie getMovie() {
        return movie;
    }

    @Override
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    private void validateIndices(int row, int col) {
        if (row < 0 || row >= seats.length) throw new IndexOutOfBoundsException("row out of bounds: " + row);
        if (col < 0 || col >= seats[row].length) throw new IndexOutOfBoundsException("col out of bounds: " + col);
    }

    @Override
    public Seat getSeat(int row, int col) {
        validateIndices(row, col);
        return seats[row][col];
    }

    @Override
    public synchronized boolean bookSeat(int row, int col) {
        validateIndices(row, col);
        if (booked[row][col]) return false;
        booked[row][col] = true;
        return true;
    }

    @Override
    public synchronized boolean cancelSeat(int row, int col) {
        validateIndices(row, col);
        if (!booked[row][col]) return false;
        booked[row][col] = false;
        return true;
    }

    @Override
    public synchronized boolean isSeatAvailable(int row, int col) {
        validateIndices(row, col);
        return !booked[row][col];
    }

    @Override
    public synchronized int getAvailableSeatCount() {
        int rows = seats.length;
        int cols = rows > 0 ? seats[0].length : 0;
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < seats[r].length; c++) {
                if (!booked[r][c]) count++;
            }
        }
        return count;
    }

    @Override
    public double getBasePrice() {
        return basePrice;
    }

    @Override
    public void setBasePrice(double price) {
        if (price < 0) throw new IllegalArgumentException("price cannot be negative");
        this.basePrice = price;
    }

    @Override
    public String getAuditoriumName() {
        return auditoriumName;
    }

    @Override
    public void setAuditoriumName(String name) {
        this.auditoriumName = name;
    }

    @Override
    public int getRowCount() {
        return seats.length;
    }

    @Override
    public int getColCount() {
        return seats.length == 0 ? 0 : seats[0].length;
    }

    @Override
    public Seat[][] getSeats() {
        // Return the internal array reference (interface allows either defensive copy or internal array).
        // If you prefer a defensive copy, create and return a deep copy here.
        return seats;
    }

    @Override
    public String toString() {
        return "Showtime{" +
                "movie=" + (movie != null ? movie.getTitle() : "null") +
                ", dateTime=" + dateTime +
                ", auditoriumName='" + auditoriumName + '\'' +
                ", rows=" + getRowCount() +
                ", cols=" + getColCount() +
                ", basePrice=" + basePrice +
                '}';
    }
}
