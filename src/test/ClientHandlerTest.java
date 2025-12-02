package test;

import database.Database;
import movie.Movie;
import reservation.Reservation;
import seat.Seat;
import server.ClientHandler;
import server.Protocol;
import server.Server;
import showtime.Showtime;
import user.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ClientHandlerTest {


    static class TestDatabase extends Database {
        @Override
        public synchronized void saveDatabase() throws IOException {
            // no-op: avoid touching myDataBase.ser during tests
        }
    }


    static class TestServer extends Server {
        private final Database testDb;

        public TestServer(Database db) {
            super();
            this.testDb = db;
        }

        @Override
        public Database getDatabase() {
            return testDb;
        }
    }


    static class FakeSocket extends Socket {

        private final InputStream in;
        private final OutputStream out;
        private boolean closed = false;

        FakeSocket(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public InputStream getInputStream() {
            return in;
        }

        @Override
        public OutputStream getOutputStream() {
            return out;
        }

        @Override
        public synchronized void close() throws IOException {
            closed = true;
        }

        @Override
        public synchronized boolean isClosed() {
            return closed;
        }
    }



    private TestDatabase db;
    private TestServer server;
    private FakeSocket socket;
    private ClientHandler handler;

    private StringWriter outBuffer;
    private PrintWriter outWriter;
    private BufferedReader inReader;


    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String name, Class<T> type) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object val = f.get(target);
        return (T) val;
    }

    private Object invokeHandler(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method m = ClientHandler.class.getDeclaredMethod(methodName, paramTypes);
        m.setAccessible(true);
        return m.invoke(handler, args);
    }

    private String[] outputLines() {
        String raw = outBuffer.toString();
        if (raw.isEmpty()) return new String[0];
        return raw.split("\\R");
    }


    @BeforeEach
    public void setUp() throws Exception {
        db = new TestDatabase();
        server = new TestServer(db);

        socket = new FakeSocket(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream());


        handler = new ClientHandler(socket, server);


        outBuffer = new StringWriter();
        outWriter = new PrintWriter(outBuffer, true);
        inReader = new BufferedReader(new StringReader(""));

        setField(handler, "out", outWriter);
        setField(handler, "in", inReader);


        db.clearAll();
    }

    @AfterEach
    public void tearDown() {
        db.clearAll();
    }



    @Test
    public void testSetupStreamsInitializesInAndOut() throws Exception {

        FakeSocket fs = new FakeSocket(
                new ByteArrayInputStream(new byte[0]),
                new ByteArrayOutputStream());
        ClientHandler local = new ClientHandler(fs, server);

        Method m = ClientHandler.class.getDeclaredMethod("setupStreams");
        m.setAccessible(true);
        m.invoke(local);

        BufferedReader in = getField(local, "in", BufferedReader.class);
        PrintWriter out = getField(local, "out", PrintWriter.class);

        assertNotNull(in, "Input stream should be initialized");
        assertNotNull(out, "Output stream should be initialized");
    }

    @Test
    public void testSendWritesRawMessage() throws Exception {
        Method send = ClientHandler.class.getDeclaredMethod("send", String.class);
        send.setAccessible(true);

        send.invoke(handler, "HELLO");
        String[] lines = outputLines();
        assertEquals(1, lines.length);
        assertEquals("HELLO", lines[0]);
    }

    @Test
    public void testSendSuccessPrefixesSuccess() throws Exception {
        Method sendSuccess = ClientHandler.class.getDeclaredMethod("sendSuccess", String.class);
        sendSuccess.setAccessible(true);

        sendSuccess.invoke(handler, "ok");
        String[] lines = outputLines();
        assertEquals("SUCCESS|ok", lines[0]);
    }

    @Test
    public void testSendErrorPrefixesError() throws Exception {
        Method sendError = ClientHandler.class.getDeclaredMethod("sendError", String.class);
        sendError.setAccessible(true);

        sendError.invoke(handler, "bad");
        String[] lines = outputLines();
        assertEquals("ERROR|bad", lines[0]);
    }

    @Test
    public void testCloseEverythingClosesSocket() throws Exception {
        setField(handler, "in", new BufferedReader(new StringReader("")));
        setField(handler, "out", new PrintWriter(new StringWriter()));

        Method close = ClientHandler.class.getDeclaredMethod("closeEverything");
        close.setAccessible(true);
        close.invoke(handler);

        assertTrue(socket.isClosed(), "Socket should be closed by closeEverything()");
    }

    @Test
    public void testFindShowtimeByIdViaReflection() throws Exception {
        Movie m = new Movie("Interstellar", "Sci-Fi", "PG-13", 169, null);
        db.addMovie(m);
        Showtime st = new Showtime(m,
                LocalDateTime.of(2025, 1, 1, 20, 0),
                3, 4, 10.0, "Aud1");
        db.addShowtime(st);

        Method findSt = ClientHandler.class.getDeclaredMethod("findShowtimeById", String.class);
        findSt.setAccessible(true);

        Object resultOk = findSt.invoke(handler, "ST_0");
        Object resultBad = findSt.invoke(handler, "ST_99");

        assertNotNull(resultOk, "ST_0 should resolve to a showtime");
        assertEquals(st, resultOk);
        assertNull(resultBad, "invalid id should yield null");
    }


    @Test
    public void testHandleCommandInvalidCommand() throws Exception {
        invokeHandler("handleCommand", new Class<?>[]{String.class}, "UNKNOWN|whatever");
        String[] lines = outputLines();
        assertEquals("ERROR|" + Protocol.ERROR_INVALID_COMMAND, lines[0]);
    }

    @Test
    public void testHandleRegisterSuccess() throws Exception {
        String[] parts = {"REGISTER", "alice1", "secret123", "a@b.com"};
        invokeHandler("handleRegister", new Class<?>[]{String[].class}, (Object) parts);

        User u = db.findUser("alice1");
        assertNotNull(u);
        assertEquals("alice1", u.getUsername());

        String[] lines = outputLines();
        assertTrue(lines[0].startsWith("SUCCESS|"), "Expected SUCCESS from handleRegister");
    }

    @Test
    public void testHandleRegisterBadEmail() throws Exception {
        String[] parts = {"REGISTER", "bob1", "secret123", "not-an-email"};
        invokeHandler("handleRegister", new Class<?>[]{String[].class}, (Object) parts);

        assertNull(db.findUser("bob1"), "User should not be created with bad email");
        String[] lines = outputLines();
        assertTrue(lines[0].startsWith("ERROR|"), "Expected ERROR for bad email");
    }

    @Test
    public void testHandleLoginSuccess() throws Exception {
        User u = new User("user1", "pass123", "u@x.com", false);
        db.addUser(u);

        String[] parts = {"LOGIN", "user1", "pass123"};
        invokeHandler("handleLogin", new Class<?>[]{String[].class}, (Object) parts);

        User current = getField(handler, "currentUser", User.class);
        boolean authed = getField(handler, "isAuthenticated", boolean.class);

        assertEquals(u, current);
        assertTrue(authed);

        String[] lines = outputLines();
        assertTrue(lines[0].startsWith("SUCCESS|Welcome user1!"), "Expected welcome message");
    }

    @Test
    public void testHandleLoginBadPassword() throws Exception {
        User u = new User("user2", "right", "u2@x.com", false);
        db.addUser(u);

        String[] parts = {"LOGIN", "user2", "wrong"};
        invokeHandler("handleLogin", new Class<?>[]{String[].class}, (Object) parts);

        boolean authed = getField(handler, "isAuthenticated", boolean.class);
        assertFalse(authed);

        String[] lines = outputLines();
        assertTrue(lines[0].startsWith("ERROR|Invalid credentials"));
    }

    @Test
    public void testHandleLogoutResetsAuth() throws Exception {
        User u = new User("user3", "p", "u3@x.com", false);
        db.addUser(u);
        setField(handler, "currentUser", u);
        setField(handler, "isAuthenticated", true);

        String[] parts = {"LOGOUT"};
        invokeHandler("handleLogout", new Class<?>[]{}, new Object[]{});

        User current = getField(handler, "currentUser", User.class);
        boolean authed = getField(handler, "isAuthenticated", boolean.class);

        assertNull(current);
        assertFalse(authed);

        String[] lines = outputLines();
        assertTrue(lines[0].startsWith("SUCCESS|Logged out successfully"));
    }


    @Test
    public void testHandleListMoviesWithOneMovie() throws Exception {
        Movie m = new Movie("Dune", "Sci-Fi", "PG-13", 155, null);
        db.addMovie(m);

        String[] parts = {"LIST_MOVIES"};
        invokeHandler("handleListMovies", new Class<?>[]{}, new Object[]{});

        String[] lines = outputLines();
        assertEquals("SUCCESS|1", lines[0]);
        assertTrue(lines[1].startsWith("MOVIE|Dune|Dune"));
        assertEquals("END_LIST", lines[2]);
    }

    @Test
    public void testHandleListShowtimesForMovie() throws Exception {
        Movie m = new Movie("Avatar", "Sci-Fi", "PG-13", 160, null);
        db.addMovie(m);
        Showtime st = new Showtime(m,
                LocalDateTime.of(2025, 3, 10, 19, 30),
                2, 3, 12.5, "Aud2");
        db.addShowtime(st);

        String[] parts = {"LIST_SHOWTIMES", "Avatar"};
        invokeHandler("handleListShowtimes", new Class<?>[]{String[].class}, (Object) parts);

        String[] lines = outputLines();
        assertEquals("SUCCESS|1", lines[0]);
        assertTrue(lines[1].startsWith("SHOWTIME|ST_0|"), "Showtime id ST_0 expected");
        assertEquals("END_LIST", lines[2]);
    }


    @Test
    public void testHandleViewSeatsShowsAllAvailable() throws Exception {
        Movie m = new Movie("Matrix", "Sci-Fi", "R", 140, null);
        db.addMovie(m);
        Showtime st = new Showtime(m,
                LocalDateTime.of(2025, 4, 1, 21, 0),
                2, 3, 9.0, "Aud3");
        db.addShowtime(st);

        String[] parts = {"VIEW_SEATS", "ST_0"};
        invokeHandler("handleViewSeats", new Class<?>[]{String[].class}, (Object) parts);

        String[] lines = outputLines();
        assertEquals("SUCCESS|2|3", lines[0]); // rows=2, cols=3
        assertTrue(lines[1].startsWith("ROW|1|1|1|1"));
        assertTrue(lines[2].startsWith("ROW|2|1|1|1"));
        assertEquals("END_SEATS", lines[3]);
    }

    private void makeAuthedUser(String username) throws Exception {
        User u = new User(username, "pw12345", username + "@x.com", false);
        db.addUser(u);
        setField(handler, "currentUser", u);
        setField(handler, "isAuthenticated", true);
    }

    @Test
    public void testHandleBookSeatsSuccess() throws Exception {
        Movie m = new Movie("Oppenheimer", "Drama", "R", 180, null);
        db.addMovie(m);
        Showtime st = new Showtime(m,
                LocalDateTime.of(2025, 7, 1, 18, 0),
                3, 3, 15.0, "Aud4");
        db.addShowtime(st);

        makeAuthedUser("booker");

        String[] parts = {"BOOK", "ST_0", "2", "1:1", "1:2", "1234567891011121", "02/27", "123"};
        invokeHandler("handleBookSeats", new Class<?>[]{String[].class}, (Object) parts);

        List<Reservation> allRes = db.getReservations();
        assertEquals(1, allRes.size());
        Reservation r = allRes.get(0);

        assertFalse(st.isSeatAvailable(0, 0));
        assertFalse(st.isSeatAvailable(0, 1));

        String[] lines = outputLines();
        assertTrue(lines[0].startsWith("SUCCESS|"), "Booking should succeed");
    }

    @Test
    public void testHandleBookSeatsRejectsDuplicateSelection() throws Exception {
        Movie m = new Movie("Cars", "Family", "G", 100, null);
        db.addMovie(m);
        Showtime st = new Showtime(m,
                LocalDateTime.of(2025, 5, 1, 12, 0),
                2, 2, 8.0, "Aud5");
        db.addShowtime(st);

        makeAuthedUser("duper");

        String[] parts = {"BOOK", "ST_0", "2", "1:1", "1:1", "1234567891011121", "02/27", "123"};
        invokeHandler("handleBookSeats", new Class<?>[]{String[].class}, (Object) parts);

        assertTrue(st.isSeatAvailable(0, 0), "Seat should not be booked on duplicate error");
        assertEquals(0, db.getReservations().size(), "No reservations should be created");

        String[] lines = outputLines();
        assertTrue(lines[0].contains("Duplicate seat selection"), "Expected duplicate seat error");
    }



    private Reservation createReservationForUser(User u, Showtime st) {
        ArrayList<Seat> seats = new ArrayList<Seat>();
        seats.add(new Seat(0, 0, st.getBasePrice()));
        String cardNumber = "1234567891011121";
        String expiry = "02/27";
        String cvv = "123";
        return new Reservation(u, st, seats, cardNumber, expiry, cvv);
    }

    @Test
    public void testHandleCancelReservationSuccess() throws Exception {
        Movie m = new Movie("DeleteMe", "Drama", "PG", 110, null);
        db.addMovie(m);
        Showtime st = new Showtime(m,
                LocalDateTime.of(2025, 6, 1, 15, 0),
                2, 2, 11.0, "Aud6");
        db.addShowtime(st);

        User u = new User("owner", "p", "o@x.com", false);
        db.addUser(u);
        Reservation r = createReservationForUser(u, st);
        db.addReservation(r);
        u.addReservation(r);

        setField(handler, "currentUser", u);
        setField(handler, "isAuthenticated", true);

        String[] parts = {"CANCEL", r.getBookingID()};
        invokeHandler("handleCancelReservation", new Class<?>[]{String[].class}, (Object) parts);

        assertEquals(0, db.getReservations().size());
        assertTrue(st.isSeatAvailable(0, 0));

        String[] lines = outputLines();
        assertTrue(lines[0].startsWith("SUCCESS|Reservation cancelled"));
    }

    @Test
    public void testHandleMyBookingsListsReservations() throws Exception {
        Movie m = new Movie("ListMe", "Comedy", "PG-13", 105, null);
        db.addMovie(m);
        Showtime st = new Showtime(m,
                LocalDateTime.of(2025, 8, 1, 19, 0),
                1, 2, 7.0, "Aud7");
        db.addShowtime(st);

        User u = new User("viewer", "p", "view@x.com", false);
        db.addUser(u);
        Reservation r = createReservationForUser(u, st);
        db.addReservation(r);
        u.addReservation(r);

        setField(handler, "currentUser", u);
        setField(handler, "isAuthenticated", true);

        String[] parts = {"MY_BOOKINGS"};
        invokeHandler("handleMyBookings", new Class<?>[]{}, new Object[]{});

        String[] lines = outputLines();
        assertEquals("SUCCESS|1", lines[0]);
        assertTrue(lines[1].startsWith("BOOKING|" + r.getBookingID() + "|ListMe"), "Booking line should list movie title");
        assertEquals("END_LIST", lines[2]);
    }

    private void makeAdminUser(String username) throws Exception {
        User admin = new User(username, "pwadmin", username + "@x.com", true);
        db.addUser(admin);
        setField(handler, "currentUser", admin);
        setField(handler, "isAuthenticated", true);
    }

    @Test
    public void testHandleAdminAddMovieSuccess() throws Exception {
        makeAdminUser("root");

        String[] parts = {"ADMIN_ADD_MOVIE", "AdminMovie", "Drama", "PG", "120"};
        invokeHandler("handleAdminAddMovie", new Class<?>[]{String[].class}, (Object) parts);

        assertTrue(db.movieExists("AdminMovie"));
        String[] lines = outputLines();
        assertTrue(lines[0].startsWith("SUCCESS|Movie added"));
    }

    @Test
    public void testHandleAdminAddShowtimeSuccess() throws Exception {
        makeAdminUser("root2");

        Movie m = new Movie("ShowMovie", "Drama", "PG", 120, null);
        db.addMovie(m);

        String dt = LocalDateTime.of(2025, 9, 1, 13, 0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String[] parts = {"ADMIN_ADD_SHOWTIME", "ShowMovie", dt, "3", "4", "10.5", "MainHall"};
        invokeHandler("handleAdminAddShowtime", new Class<?>[]{String[].class}, (Object) parts);

        List<Showtime> sts = db.getShowtimes();
        assertEquals(1, sts.size());
        Showtime st = sts.get(0);
        assertEquals("ShowMovie", st.getMovie().getTitle());

        String[] lines = outputLines();
        assertTrue(lines[0].startsWith("SUCCESS|Showtime added"));
    }

    @Test
    public void testHandleAdminPromoteUser() throws Exception {
        makeAdminUser("boss");

        User normal = new User("peasant", "p", "p@x.com", false);
        db.addUser(normal);

        String[] parts = {"ADMIN_PROMOTE", "peasant"};
        invokeHandler("handleAdminPromoteUser", new Class<?>[]{String[].class}, (Object) parts);

        User promoted = db.findUser("peasant");
        assertTrue(promoted.isAdmin());

        String[] lines = outputLines();
        assertTrue(lines[0].startsWith("SUCCESS|User promoted to admin"));
    }

    @Test
    public void testHandleAdminViewAllBookings() throws Exception {
        makeAdminUser("boss2");

        Movie m = new Movie("AdminView", "Drama", "PG-13", 130, null);
        db.addMovie(m);
        Showtime st = new Showtime(m,
                LocalDateTime.of(2025, 10, 1, 20, 0),
                2, 2, 9.0, "Aud8");
        db.addShowtime(st);

        User u = new User("cust", "p", "c@x.com", false);
        db.addUser(u);
        Reservation r = createReservationForUser(u, st);
        db.addReservation(r);
        u.addReservation(r);

        String[] parts = {"ADMIN_VIEW_ALL_BOOKINGS"};
        invokeHandler("handleAdminViewAllBookings", new Class<?>[]{}, new Object[]{});

        String[] lines = outputLines();
        assertEquals("SUCCESS|1", lines[0]);
        assertTrue(lines[1].startsWith("BOOKING_DETAIL|" + r.getBookingID() + "|cust|AdminView"),
                "Booking detail should include id, username and movie");
        assertEquals("END_LIST", lines[2]);
    }
}
