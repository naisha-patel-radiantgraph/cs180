package client;

import org.junit.jupiter.api.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

public class ClientTest {
    private Client client;
    private PrintWriter fakeServerOut;
    private BufferedReader fakeServerIn;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setup() throws Exception {
        client = new Client("localhost", 4242);
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        fakeServerOut = new PrintWriter(new ByteArrayOutputStream(), true);
        fakeServerIn = new BufferedReader(new StringReader(""));
        setPrivateField(client, "serverOut", fakeServerOut);
        setPrivateField(client, "serverIn", fakeServerIn);
        setPrivateField(client, "userIn", new Scanner(new ByteArrayInputStream("".getBytes())));
    }
    @AfterEach
    void cleanup() {
        System.setOut(System.out);
    }
    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    private Object invokePrivateMethod(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = Client.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(client, args);
    }
    @Test
    void testLoginSetsFields() throws Exception {
        fakeServerIn = new BufferedReader(new StringReader("SUCCESS|Login OK|false\n"));
        setPrivateField(client, "serverIn", fakeServerIn);
        setPrivateField(client, "userIn", new Scanner(new ByteArrayInputStream("user\npass\n".getBytes())));
        invokePrivateMethod("login", new Class<?>[]{});
        boolean isLoggedIn = (boolean) getPrivateField(client, "isLoggedIn");
        String username = (String) getPrivateField(client, "currentUsername");
        boolean isAdmin = (boolean) getPrivateField(client, "isAdmin");
        assertTrue(isLoggedIn);
        assertEquals("user", username);
        assertFalse(isAdmin);
    }
    @Test
    void testRegister() throws Exception {
        fakeServerIn = new BufferedReader(new StringReader("SUCCESS|Account created\n"));
        setPrivateField(client, "serverIn", fakeServerIn);
        setPrivateField(client, "userIn", new Scanner(new ByteArrayInputStream("Alicia\npassword\nAlicia@gmail.com\n".getBytes())));
        invokePrivateMethod("register", new Class<?>[]{});
        String output = outContent.toString();
        assertTrue(output.contains("Account created successfully."));
    }
    @Test
    void testLogoutResetsFields() throws Exception {
        setPrivateField(client, "isLoggedIn", true);
        setPrivateField(client, "currentUsername", "someone");
        setPrivateField(client, "isAdmin", true);
        invokePrivateMethod("logout", new Class<?>[]{});
        assertFalse((boolean) getPrivateField(client, "isLoggedIn"));
        assertNull(getPrivateField(client, "currentUsername"));
        assertFalse((boolean) getPrivateField(client, "isAdmin"));
        String output = outContent.toString();
        assertTrue(output.contains("Logged out successfully."));
    }
    @Test
    void testListMoviesWithNoMovies() throws Exception {
        String response = "SUCCESS|0\nEND_LIST\n";
        fakeServerIn = new BufferedReader(new StringReader(response));
        setPrivateField(client, "serverIn", fakeServerIn);
        setPrivateField(client, "userIn", new Scanner(new ByteArrayInputStream("n\n".getBytes())));
        invokePrivateMethod("listMovies", new Class<?>[]{});
        String output = outContent.toString();
        assertTrue(output.contains("No movies available."));
    }
    @Test
    void testViewSeatMapDisplaysSeats() throws Exception {
        String seatMapResponse =
                "SUCCESS|2|3\n" +
                        "ROW|1|1|0|1\n" +
                        "ROW|2|1|1|1\n" +
                        "END_SEATS\n";
        fakeServerIn = new BufferedReader(new StringReader(seatMapResponse));
        setPrivateField(client, "serverIn", fakeServerIn);
        int[][] seats = (int[][]) invokePrivateMethod("viewSeatMap", new Class<?>[]{String.class}, "ST1");
        assertNotNull(seats);
        assertEquals(2, seats.length);
        assertEquals(3, seats[0].length);
        assertEquals(0, seats[0][1]);
        assertEquals(1, seats[1][2]);
    }
    @Test
    void testBookSeatsCancelsWhenNoMovies() throws Exception {
        String response = "SUCCESS|0\nEND_LIST\n";
        fakeServerIn = new BufferedReader(new StringReader(response));
        setPrivateField(client, "serverIn", fakeServerIn);
        setPrivateField(client, "userIn", new Scanner(new ByteArrayInputStream("0\n".getBytes())));
        invokePrivateMethod("bookSeats", new Class<?>[]{});
        String output = outContent.toString();
        assertTrue(output.contains("No movies available to book."));
    }
    @Test
    void testViewMyBookingsNoBookings() throws Exception {
        String response = "SUCCESS|0\nEND_LIST\n";
        fakeServerIn = new BufferedReader(new StringReader(response));
        setPrivateField(client, "serverIn", fakeServerIn);
        invokePrivateMethod("viewMyBookings", new Class<?>[]{});
        String output = outContent.toString();
        assertTrue(output.contains("You have no bookings."));
    }
    @Test
    void testAddMovieAndAddShowtimeAndPromoteUser() throws Exception {
        fakeServerIn = new BufferedReader(new StringReader(
                "SUCCESS|Movie added\n" +
                        "SUCCESS|Showtime added\n" +
                        "SUCCESS|User promoted\n"
        ));
        setPrivateField(client, "serverIn", fakeServerIn);
        setPrivateField(client, "userIn", new Scanner(new ByteArrayInputStream(
                ("Title\nGenre\nPG-13\n120\n" +
                        "MovieID\n2025-02-14 01:00\n5\n5\n10.01\nAuditorium 1\n" +
                        "someuser\n").getBytes()
        )));
        invokePrivateMethod("addMovie", new Class<?>[]{});
        invokePrivateMethod("addShowtime", new Class<?>[]{});
        invokePrivateMethod("promoteUser", new Class<?>[]{});
        String output = outContent.toString();
        assertTrue(output.contains("Movie added successfully."));
        assertTrue(output.contains("Showtime added successfully."));
        assertTrue(output.contains("User promoted to admin."));
    }
}
