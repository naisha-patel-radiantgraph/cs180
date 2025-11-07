package showtime;

import interfaces.IShowtime;
import movie.Movie;
import seat.Seat;

import java.time.LocalDateTime;

public class Showtime implements IShowtime {
    @Override
    public Movie getMovie() {
        return null;
    }

    @Override
    public LocalDateTime getDateTime() {
        return null;
    }

    @Override
    public Seat getSeat(int row, int col) {
        return null;
    }

    @Override
    public boolean bookSeat(int row, int col) {
        return false;
    }

    @Override
    public boolean cancelSeat(int row, int col) {
        return false;
    }

    @Override
    public boolean isSeatAvailable(int row, int col) {
        return false;
    }

    @Override
    public int getAvailableSeatCount() {
        return 0;
    }

    @Override
    public double getBasePrice() {
        return 0;
    }

    @Override
    public void setBasePrice(double price) {

    }

    @Override
    public String getAuditoriumName() {
        return "";
    }

    @Override
    public void setAuditoriumName(String name) {

    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getColCount() {
        return 0;
    }

    @Override
    public Seat[][] getSeats() {
        return new Seat[0][];
    }
}
