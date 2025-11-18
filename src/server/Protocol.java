package server;

/**
 * Protocol constants for client-server communication
 * All messages follow the format: COMMAND|PARAM1|PARAM2|...|PARAMn
 *
 * @author Group 4, L26
 * @version Nov 18, 2025
 */
public class Protocol {

    // Commands
    public static final String LOGIN = "LOGIN";
    public static final String REGISTER = "REGISTER";
    public static final String LOGOUT = "LOGOUT";
    public static final String LIST_MOVIES = "LIST_MOVIES";
    public static final String LIST_SHOWTIMES = "LIST_SHOWTIMES";
    public static final String VIEW_SEATS = "VIEW_SEATS";
    public static final String BOOK = "BOOK";
    public static final String CANCEL = "CANCEL";
    public static final String MY_BOOKINGS = "MY_BOOKINGS";
    public static final String ADMIN_ADD_MOVIE = "ADMIN_ADD_MOVIE";
    public static final String ADMIN_ADD_SHOWTIME = "ADMIN_ADD_SHOWTIME";
    public static final String ADMIN_PROMOTE = "ADMIN_PROMOTE";
    public static final String ADMIN_VIEW_ALL_BOOKINGS = "ADMIN_VIEW_ALL_BOOKINGS";

    // Response Types
    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
    public static final String CONNECTED = "CONNECTED";
    public static final String MOVIE = "MOVIE";
    public static final String SHOWTIME = "SHOWTIME";
    public static final String BOOKING = "BOOKING";
    public static final String BOOKING_DETAIL = "BOOKING_DETAIL";
    public static final String ROW = "ROW";
    public static final String END_LIST = "END_LIST";
    public static final String END_SEATS = "END_SEATS";

    // Error Messages
    public static final String ERROR_AUTH_REQUIRED = "AUTH_REQUIRED";
    public static final String ERROR_ADMIN_REQUIRED = "ADMIN_REQUIRED";
    public static final String ERROR_INVALID_COMMAND = "INVALID_COMMAND";
    public static final String ERROR_INVALID_FORMAT = "INVALID_FORMAT";
    public static final String ERROR_DATABASE_ERROR = "DATABASE_ERROR";
    public static final String ERROR_TIMEOUT = "TIMEOUT";

    // Delimiters
    public static final String DELIMITER = "|";
    public static final String SEAT_DELIMITER = ":";
    public static final String SEAT_SEPARATOR = ",";
}
