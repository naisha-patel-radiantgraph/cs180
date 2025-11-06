package interfaces;

import java.util.ArrayList;
import reservation.Reservation;

public interface IUser {

    String getUsername(); //returns username
    String getEmail(); //returns email
    boolean isAdmin(); //returns true if the user is an admin

    String getPasswordHash(); //returns the hashed password
    boolean verifyPassword(String password); //checks if stored password meets the entered password

    ArrayList<Reservation> getReservations(); //returns all reservations of the user
    void addReservation(Reservation reservation); //adds a new reservation
    void removeReservation(String bookingID); //removes reservation by booking ID
}
