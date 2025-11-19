package server;
import server.Protocol;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;
    private final Scanner userIn;
    private boolean authenticated = false;
    private boolean isAdmin = false;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        this.userIn = new Scanner(System.in);
    }
    public void start() {
        try {
            socket = new Socket(host, port);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOut = new PrintWriter(socket.getOutputStream(), true);
            System.out.println(serverIn.readLine());
            mainMenu();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
// Start of mainMenu for Cinema Booking System. Shows the options before redirecting user based off their choice.
    private void mainMenu() throws IOException {
        while (true) {
            System.out.println("\n---------------------");
            System.out.println("CINEMA BOOKING SYSTEM");
            System.out.println("---------------------");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            String choice = userIn.nextLine().trim();
            switch (choice) {
                case "1" -> loginMenu();
                case "2" -> registerMenu();
                case "3" -> {
                    System.out.println("Goodbye!");
                    socket.close();
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
// Start of registerMenu looking for user input to assign username, password, and email to user.
    private void registerMenu() throws IOException {
        System.out.println("\n--------");
        System.out.println("Register");
        System.out.println("--------");
        System.out.print("Username: ");
        String username = userIn.nextLine().trim();
        System.out.print("Password: ");
        String password = userIn.nextLine().trim();
        System.out.print("Email: ");
        String email = userIn.nextLine().trim();
        String command = Protocol.REGISTER + Protocol.DELIMITER
                + username + Protocol.DELIMITER
                + password + Protocol.DELIMITER
                + email;
        serverOut.println(command);
        handleResponse();
    }
// Start of loginMenu looking for user input to confirm username and password exist and whether they go together.
    private void loginMenu() throws IOException {
        System.out.println("\n-----");
        System.out.println("Login");
        System.out.println("-----");
        System.out.print("Username: ");
        String username = userIn.nextLine().trim();
        System.out.print("Password: ");
        String password = userIn.nextLine().trim();
        String command = Protocol.LOGIN + Protocol.DELIMITER
                + username + Protocol.DELIMITER
                + password;
        serverOut.println(command);
        String line = serverIn.readLine();
        if (line == null) return;
        if (line.startsWith(Protocol.SUCCESS)) {
            authenticated = true;
            String[] parts = line.split("\\|");
            isAdmin = Boolean.parseBoolean(parts[2]);
            System.out.println(parts[1]);
            if (isAdmin) adminMenu();
            else userMenu();
        } else {
            System.out.println(line);
        }
    }
// Start of userMenu which lists the different options before redirecting user based off choice.
    private void userMenu() throws IOException {
        while (authenticated) {
            System.out.println("\n---------");
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. List Movies");
            System.out.println("2. List Showtimes");
            System.out.println("3. View Seats");
            System.out.println("4. Book Seats");
            System.out.println("5. Cancel Booking");
            System.out.println("6. My Bookings");
            System.out.println("7. Logout");
            System.out.print("Choose: ");
            String choice = userIn.nextLine().trim();
            switch (choice) {
                case "1" -> sendSimple(Protocol.LIST_MOVIES);
                case "2" -> sendListShowtimes();
                case "3" -> sendViewSeats();
                case "4" -> sendBook();
                case "5" -> sendCancel();
                case "6" -> sendSimple(Protocol.MY_BOOKINGS);
                case "7" -> logout();
                default -> System.out.println("Invalid choice.");
            }
        }
    }
// Start of adminMenu which shows when user is admin and shows the different admin options.
    private void adminMenu() throws IOException {
        while (authenticated) {
            System.out.println("\n----------");
            System.out.println("ADMIN MENU");
            System.out.println("----------");
            System.out.println("1. Add Movie");
            System.out.println("2. Add Showtime");
            System.out.println("3. Promote User");
            System.out.println("4. View All Bookings");
            System.out.println("5. Logout");
            System.out.print("Choose: ");
            String choice = userIn.nextLine().trim();
            switch (choice) {
                case "1" -> sendAddMovie();
                case "2" -> sendAddShowtime();
                case "3" -> sendPromoteUser();
                case "4" -> sendSimple(Protocol.ADMIN_VIEW_ALL_BOOKINGS);
                case "5" -> logout();
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void sendSimple(String command) throws IOException {
        serverOut.println(command);
        handleResponse();
    }

    private void sendListShowtimes() throws IOException {
        System.out.print("Movie title: ");
        String title = userIn.nextLine().trim();
        String cmd = Protocol.LIST_SHOWTIMES + Protocol.DELIMITER + title;
        serverOut.println(cmd);
        handleResponse();
    }

    private void sendViewSeats() throws IOException {
        System.out.print("Showtime ID: ");
        String id = userIn.nextLine().trim();
        serverOut.println(Protocol.VIEW_SEATS + Protocol.DELIMITER + id);
        handleResponse();
    }

    private void sendBook() throws IOException {
        System.out.print("Showtime ID: ");
        String id = userIn.nextLine().trim();
        System.out.print("Seats (e.g., A1,A2,A3): ");
        String seats = userIn.nextLine().trim();
        String cmd = Protocol.BOOK + Protocol.DELIMITER + id + Protocol.DELIMITER + seats;
        serverOut.println(cmd);
        handleResponse();
    }

    private void sendCancel() throws IOException {
        System.out.print("Booking ID: ");
        String id = userIn.nextLine().trim();
        serverOut.println(Protocol.CANCEL + Protocol.DELIMITER + id);
        handleResponse();
    }

    private void sendAddMovie() throws IOException {
        System.out.print("Movie title: ");
        String title = userIn.nextLine().trim();
        serverOut.println(Protocol.ADMIN_ADD_MOVIE + Protocol.DELIMITER + title);
        handleResponse();
    }

    private void sendAddShowtime() throws IOException {
        System.out.print("Movie title: ");
        String title = userIn.nextLine().trim();
        System.out.print("Date & time (YYYY-MM-DD HH:MM): ");
        String datetime = userIn.nextLine().trim();
        serverOut.println(Protocol.ADMIN_ADD_SHOWTIME + Protocol.DELIMITER + title + Protocol.DELIMITER + datetime);
        handleResponse();
    }

    private void sendPromoteUser() throws IOException {
        System.out.print("Username to promote: ");
        String user = userIn.nextLine().trim();
        serverOut.println(Protocol.ADMIN_PROMOTE + Protocol.DELIMITER + user);
        handleResponse();
    }

    private void logout() throws IOException {
        serverOut.println(Protocol.LOGOUT);
        authenticated = false;
        System.out.println("Logged out.");
    }

    private void handleResponse() throws IOException {
        String line;
        while ((line = serverIn.readLine()) != null) {
            if (line.equals(Protocol.END_LIST) || line.equals(Protocol.END_SEATS))
                break;
            System.out.println(line);
            if (!line.startsWith(Protocol.MOVIE) &&
                    !line.startsWith(Protocol.SHOWTIME) &&
                    !line.startsWith(Protocol.BOOKING) &&
                    !line.startsWith(Protocol.ROW))
                break;
        }
    }

    public static void main(String[] args) {
        Client c = new Client("localhost", 4242);
        c.start();
    }
}






