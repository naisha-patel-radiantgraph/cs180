package database;

import interfaces.IDatabase;
import user.User;
import reservation.Reservation;
import movie.Movie;
import showtime.Showtime;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class Database implements IDatabase, Serializable {

    private List<User> users;
    private List<Movie> movies;
    private List<Showtime> showtimes;
    private List<Reservation> reservations;
    private final String filePath = "myDataBase.ser";


    public Database() {
        users = new ArrayList<>();
        movies = new ArrayList<>();
        showtimes = new ArrayList<>();
        reservations = new ArrayList<>();
    }


    @Override
    public synchronized List<User> getUsers() {
        return users;
    }



    @Override
    public synchronized void addUser(User u) {
        if (u != null) {
            users.add(u);
        }
    }

    @Override
    public synchronized void removeUser(String username) {
        User toBeRemoved = null;
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                toBeRemoved = u;
                break;
            }
        }
        if (toBeRemoved != null) {
            users.remove(toBeRemoved);
        }
    }

    @Override
    public synchronized User findUser(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }


    @Override
    public synchronized void addMovie(Movie m) {
        if (m != null) {
            movies.add(m);
        }
    }

    @Override
    public synchronized void removeMovie(String title) {
        Movie toBeRemoved = null;
        for (Movie m : movies) {
            if (m.getTitle().equals(title)) {
                toBeRemoved = m;
                break;
            }
        }
        if (toBeRemoved != null) {
            movies.remove(toBeRemoved);
        }
    }

    @Override
    public synchronized List<Movie> getMovies() {
        return movies;
    }


    @Override
    public synchronized void addShowtime(Showtime s) {
        if (s != null) {
            showtimes.add(s);
        }
    }

    @Override
    public synchronized Showtime findShowtime(Movie m, LocalDateTime dt) {
        for (Showtime s : showtimes) {
            if (s.getMovie().equals(m) && s.getDateTime().equals(dt)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public synchronized List<Showtime> getShowtimes() {
        return showtimes;
    }


    @Override
    public synchronized void addReservation(Reservation r) {
        if (r != null) {
            reservations.add(r);
        }
    }

    @Override
    public synchronized void removeReservation(String bookingID) {
        Reservation toBeRemoved = null;
        for (Reservation r : reservations) {
            if (r.getBookingID().equals(bookingID)) {
                toBeRemoved = r;
                break;
            }
        }
        if (toBeRemoved != null) {
            reservations.remove(toBeRemoved);
        }
    }

    @Override
    public synchronized Reservation findReservation(String bookingID) {
        for (Reservation r : reservations) {
            if (r.getBookingID().equals(bookingID)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public synchronized List<Reservation> getReservations() {
        return reservations;
    }


    @Override
    public synchronized void saveDatabase() throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(this);
        }
    }

    @Override
    public synchronized IDatabase loadDatabase() throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            return (IDatabase) in.readObject();
        }
    }


    @Override
    public synchronized void clearAll() {
        users.clear();
        movies.clear();
        showtimes.clear();
        reservations.clear();
    }
}
