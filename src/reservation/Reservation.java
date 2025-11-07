package reservation;

import interfaces.IReservation;
import seat.Seat;
import showtime.Showtime;
import user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Reservation implements IReservation {


    @Override
    public String getBookingID() {
        return "";
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public Showtime getShowtime() {
        return null;
    }

    @Override
    public ArrayList<Seat> getBookedSeats() {
        return null;
    }

    @Override
    public LocalDateTime getBookingTime() {
        return null;
    }

    @Override
    public double getTotalPrice() {
        return 0;
    }

    @Override
    public void cancelAllSeats() {

    }
}
