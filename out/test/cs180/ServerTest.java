package test;

import org.junit.jupiter.api.*;
import server.Server;
import database.Database;
import user.User;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    private Server server;

    @BeforeEach
    void setUp() {
        server = new Server();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("Test constructor initializes database")
    void testConstructorInitializesDatabase() {
        Database db = server.getDatabase();
        assertNotNull(db, "Database should not be null after server construction");
    }

    @Test
    @DisplayName("Test default admin account exists after construction")
    void testDefaultAdminCreated() {
        Database db = server.getDatabase();
        User admin = db.findUser("admin");
        assertNotNull(admin, "Admin user should exist");
        assertTrue(admin.isAdmin(), "Admin user should have admin privileges");
    }

    @Test
    @DisplayName("Test getDatabase returns same instance")
    void testGetDatabaseReturnsSameInstance() {
        Database db1 = server.getDatabase();
        Database db2 = server.getDatabase();
        assertSame(db1, db2, "getDatabase should return the same instance each time");
    }

    @Test
    @DisplayName("Test stop() sets running field to false")
    void testStopSetsRunningFalse() throws Exception {

        Field runningField = Server.class.getDeclaredField("running");
        runningField.setAccessible(true);


        boolean runningBefore = runningField.getBoolean(server);
        assertTrue(runningBefore, "Server should initially be running");


        server.stop();


        boolean runningAfter = runningField.getBoolean(server);
        assertFalse(runningAfter, "Server stop() should set running to false");
    }

    @Test
    @DisplayName("Test stop() does not throw exceptions")
    void testStopDoesNotThrow() {
        assertDoesNotThrow(() -> server.stop(),
                "stop() should not throw any exceptions");
    }
}
