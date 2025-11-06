package interfaces;

import showtime.Showtime;
import user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;

public interface IReservation {

    String getBookingID();                      //returns the reservation's unique booking ID.
    User getUser();                             //returns the user who made the reservation.
    Showtime getShowtime();                     //returns the showtime of the movie reservation.
    ArrayList<Seat> getBookedSeats();           //returns the list of seats reserved.
    LocalDateTime getBookingTime();             //returns the time of when the reservation was created.
    double getTotalPrice();                     //returns the total cost of all booked seats.
    void cancelAllSeats();                      //Free the seats in the reservation to cancel them.

}
