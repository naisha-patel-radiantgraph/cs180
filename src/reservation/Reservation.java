package reservation;

import interfaces.IReservation;
import seat.Seat;
import showtime.Showtime;
import user.User;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Reservation implements IReservation, Serializable {

    private final String bookingID;
    private final User user;
    private final Showtime showtime;
    private final ArrayList<Seat> bookedSeats;
    private final LocalDateTime bookingTime;

    public Reservation(User user, Showtime showtime, ArrayList<Seat> seats) {
        this.user = user;
        this.showtime = showtime;
        this.bookedSeats = seats;

        this.bookingTime = LocalDateTime.now();

        this.bookingID =  UUID.randomUUID().toString();

        for(int i = 0; i < bookedSeats.size(); i++) {
            Seat seat = bookedSeats.get(i);
            this.showtime.bookSeat(seat.getRow(), seat.getNumber());
        }
    }

    @Override
    public String getBookingID() {
        return this.bookingID;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public Showtime getShowtime() {
        return this.showtime;
    }

    @Override
    public ArrayList<Seat> getBookedSeats() {
        return this.bookedSeats;
    }

    @Override
    public LocalDateTime getBookingTime() {
        return this.bookingTime;
    }

    @Override
    public double getTotalPrice() {
        double total = 0.0;
        for(int i = 0; i < bookedSeats.size(); i++) {
            total += bookedSeats.get(i).getPrice();
        }
        return total;
    }

    @Override
    public void cancelAllSeats() {
        if (this.bookedSeats != null) {
            for (int i = 0; i < this.bookedSeats.size(); i++) {
                Seat seat = this.bookedSeats.get(i);
                this.showtime.cancelSeat(seat.getRow(), seat.getNumber());
            }
            this.bookedSeats.clear();
        }
    }

    // --- PHASE 2 ADDITIONS ---

    @Override
    public String summary() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mma");
        String formattedDateTime = showtime.getDateTime().format(formatter);

        String seatList = "";
        for(int i = 0; i < bookedSeats.size(); i++) {
            seatList += bookedSeats.get(i).getSeatLabel();
            if(i != bookedSeats.size() - 1) {
                seatList += ", ";
            }
        }

        String result = "-----------------------------\n";
        result += String.format("Booking ID: %s\n", getBookingID());
        result += String.format("Movie: %s\n", showtime.getMovie().getTitle());
        result += String.format("Showtime: %s\n", formattedDateTime);
        result += String.format("Auditorium: %s\n", showtime.getAuditoriumName());
        result += String.format("Seats: %s\n", seatList);
        result += String.format("Total Cost: $%.2f\n", getTotalPrice());
        result += "----------------------------";

        return result;
    }
}
