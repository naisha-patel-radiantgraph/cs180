package user;

import interfaces.IUser;
import reservation.Reservation;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class User implements IUser, Serializable {

    private String username;                      // Unique username for login
    private String passwordHash;                  // Securely stored hashed password
    private String email;                         // Registered email for confirmation
    private boolean isAdmin;                      // Whether the user has admin privileges
    private ArrayList<Reservation> reservations; // All reservations owned by this user

    public User(String username, String password, String email, boolean isAdmin) {
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.email = email;
        this.isAdmin = isAdmin;
        this.reservations = new ArrayList<>();
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public boolean verifyPassword(String password) {
        return passwordHash.equals(hashPassword(password));
    }

    @Override
    public ArrayList<Reservation> getReservations() {
        return reservations;
    }

    @Override
    public void addReservation(Reservation reservation) {
        if (reservation != null) {
            reservations.add(reservation);
        }
    }

    @Override
    public void removeReservation(String bookingID) {
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getBookingID().equals(bookingID)) {
                reservations.remove(i);
                break;
            }
        }
    }


    @Override
    public String toString() {
        return String.format("User[username='%s', email='%s', admin=%b, reservations=%d]",
                username, email, isAdmin, reservations.size());
    }

    // --- PHASE 2 ADDITIONS ---

    @Override
    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public boolean hasReservation(String bookingID) {
        for (Reservation r : reservations) {
            if (r.getBookingID().equals(bookingID)) {
                return true;
            }
        }
        return false;
    }

}
