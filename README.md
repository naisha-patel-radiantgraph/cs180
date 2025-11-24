## Project Description

For our project, we selected **Option 2**, which involves building an **Movie Ticket Booking System**. The goal of this project is to design and implement a modular, object-oriented system that accurately simulates how an industrial-style movie booking platform operates.

## Phase 1

This phase establishes the core database framework, including:
- **Movie** – stores movie details such as title, genre, runtime, and poster path.
- **Showtime** – manages screening schedules, seating arrangements, and booking states.
- **User** – represents individual customers (or admins) capable of making and canceling reservations.
- **Reservation** – handles the linkage between users, showtimes, and booked seats.
- **Database** – provides centralized, synchronized data storage with support for saving and loading the system state using serialization.

Each component has been tested through **JUnit 5** test cases to validate appropriate functionality.

---
## Testing Instructions: 
# **Compilation & Execution Guide**

This project is developed and tested using **JUnit 5** and requires **Java 17 or later**.  
Please follow all instructions below carefully to ensure successful compilation and execution.

---

## **1. Overview**

This phase of the project focuses on backend implementation and verification through **JUnit 5** test cases.  
There is **no main program** yet — all validation happens through automated unit testing.  
The guide below explains how to set up, compile, and run the tests in both an **IDE** and via the **command line**.

---

## **2. System Requirements**

Before starting, make sure the following software is installed and configured correctly:

### ✅ **Java Development Kit (JDK 17 or later)**  
To verify installation, open a terminal or command prompt and run:

java -version


# Tentative Expected End Goal

By the end of the full project, the system will evolve into an application that allows users to:
- Browse available movies and showtimes.
- Select and reserve seats in real time.
- Handle concurrent booking operations safely.
- Store user and reservation data persistently through serialization or database integration (this feature will be abstracted from the user).
- Hashed password storage.
- Include a graphical interface for enhanced interactivity.
- Provide administrative functions for adding movies, managing showtimes, and tracking sales.
- Purchase their selected seats and reeive a payment confirmation for the same.
- Schedule their bookings upto 7 days in advance.
- Receive a unique booking ID either on the purchase screen or via email.

--- 

## Tentative User Flow Diagram

The following diagram flowchart displays how a user might interact with our program:

![UML Diagram](./images/userflowdiagram1.png)

---

## Team Member Contributions

### Gaurav Mandhyan
- **Role:** Team Leader
- **Main Responsibilities:**
    - Created class descriptions in early stages.
    - Created and implemented User class, Database class.
    - Created and implemented User interface, Database interface.
    - Created and implemented User test class, Database test class.
    - Managed deadlines and distributed work.
    - Wrote associated ReadMe sections.
---

### Naisha Patel
- **Main Responsibilities:**
    - Set up GitHub repository and file structure.
    - Created and implemented Movie class, Showtime class.
    - Created and implemented Movie interface, Showtime interface.
    - Created and implemented Movie test class, Showtime test class.
    - Wrote associated ReadMe sections.
    - Wrote compilation guide for the ReadMe.

---

### Arbin Isaac De La Torre - Rodriguez
- **Main Responsibilities:**
    - Created and implemented Seat class.
    - Created and implemented Seat interface.
    - Created and implemented Seat test class.
    - Wrote associated ReadMe sections.
    - Created User flow diagram.

---

### Jakob Graham
- **Main Responsibilities:**
    - Created and implemented Reservation class.
    - Created and implemented Reservation interface.
    - Created and implemented Reservation test class.
    - Wrote associated ReadMe sections.
    - Created User flow diagram.
---

CLASS DESCRIPTIONS
---

## User Class

### **Class Overview**
The `User` class represents an individual account in the system.  
It manages user authentication, role (admin or normal), and links the user to their active and historical reservations.  
This class also ensures secure password handling through hashing.

---

### **Field Table**

| Field Name | Access Modifier | Type | Description |
|:-------------|:----------------|:------|:--------------|
| `username` | private | String | Unique identifier chosen by the user to log in. |
| `passwordHash` | private | String | MD5 hashed version of the user's password used for secure verification. |
| `email` | private | String | User’s email used for contact or verification. |
| `isAdmin` | private | boolean | Specifies whether the user has administrative privileges. |
| `reservations` | private | ArrayList<Reservation> | List of all reservations linked to this user. |

---

### **Method Table**

| Method Name | Return Type | Access Modifier | Parameters | Description | How It Was Tested |
|:--------------|:--------------|:----------------|:-------------|:---------------|:--------------------|
| `User(String username, String password, String email, boolean isAdmin)` | Constructor | public | username, password, email, isAdmin | Initializes a new user and hashes the given password. | Verified field storage and password hashing in test cases. |
| `getUsername()` | String | public | None | Returns the username of this user. | Tested through `UserTest`. |
| `getPasswordHash()` | String | public | None | Returns the stored hashed password. | Tested through `UserTest`. |
| `verifyPassword(String password)` | boolean | public | password | Hashes input password and compares with stored hash to authenticate user. | Tested with valid and invalid passwords. |
| `hashPassword(String password)` | String | public | password | Converts a plain text password into its MD5-hashed representation. Used logic from: [MD5 Hash Tutorial](https://www.youtube.com/watch?v=ef3kenC4xa0) | None Required |
| `getEmail()` | String | public | None | Returns user’s registered email. | Tested manually through constructor verification. |
| `isAdmin()` | boolean | public | None | Returns admin status. | None Required |
| `getReservations()` | ArrayList<Reservation> | public | None | Returns list of user’s reservations. | Verified in test cases involving multiple reservations. |
| `addReservation(Reservation r)` | void | public | Reservation r | Adds a new reservation to user’s list. | Verified list size increment in test cases. |
| `removeReservation(String bookingID)` | void | public | bookingID | Removes reservation matching the provided ID. | Verified by removing valid and invalid IDs. |
| `toString()` | String | public | None | Returns a formatted string representing the user’s details (username, email, admin status, and number of reservations). | None Required |

---

## Database Class

### **Class Overview**
The `Database` class serves as the centralized storage of all system data: users, movies, showtimes, and reservations.  
It also handles persistence by saving and loading serialized data to and from disk, ensuring data is maintained between program executions.  
All modification methods are synchronized to ensure thread-safe access for concurrent client operations.

---

### **Field Table**

| Field Name | Access Modifier | Type | Description |
|:-------------|:----------------|:------|:--------------|
| `users` | private | List<User> | Stores all registered users. |
| `movies` | private | List<Movie> | Stores all available movies. |
| `showtimes` | private | List<Showtime> | Stores all showtime instances. |
| `reservations` | private | List<Reservation> | Stores all active reservations. |
| `filePath` | private final | String | Path for saving serialized database file (`myDataBase.ser`). |

---

### **Method Table**

| Method Name | Return Type | Access Modifier | Parameters | Description | How It Was Tested |
|:--------------|:--------------|:----------------|:-------------|:---------------|:--------------------|
| `Database()` | Constructor | public | None | Initializes all collections for users, movies, showtimes, and reservations. | Verified empty lists on initialization. |
| `addUser(User u)` | void | public | User u | Adds a new user to the system. | Tested through `DatabaseTest`. |
| `removeUser(String username)` | void | public | username | Removes user with matching username. | Verified in `DatabaseTest`. |
| `findUser(String username)` | User | public | username | Returns the user with given username or null if not found. | Verified existing and non-existent usernames. |
| `getUsers()` | List<User> | public | None | Returns all users currently stored in the database. | Tested indirectly by verifying list size. |
| `addMovie(Movie m)` | void | public | Movie m | Adds a movie to the database. | None Required |
| `removeMovie(String title)` | void | public | title | Removes a movie based on its title. | None Required |
| `addShowtime(Showtime s)` | void | public | Showtime s | Adds a showtime instance. | None Required |
| `findShowtime(Movie m, LocalDateTime dt)` | Showtime | public | Movie m, LocalDateTime dt | Finds a showtime for a movie at a given time. | None Required |
| `addReservation(Reservation r)` | void | public | Reservation r | Adds reservation record to system. | Tested through reservation addition checks. |
| `removeReservation(String bookingID)` | void | public | bookingID | Removes reservation with specified ID. | Tested by removing and rechecking reservation count. |
| `findReservation(String bookingID)` | Reservation | public | bookingID | Finds reservation with given ID. | Verified by comparing booking IDs. |
| `getMovies()` | List<Movie> | public | None | Returns all movies currently in database. | None Required |
| `getShowtimes()` | List<Showtime> | public | None | Returns all showtimes currently in database. | None Required |
| `getReservations()` | List<Reservation> | public | None | Returns all reservations in system. | Tested via list size after multiple additions. |
| `saveDatabase()` | void | public | None | Serializes and saves data to disk at filePath. | None Required |
| `loadDatabase()` | IDatabase | public | None | Loads serialized data from disk back into memory. | None Required |
| `clearAll()` | void | public | None | Clears all stored entities (used mainly in testing). | Tested through JUnit by verifying all lists empty after call. |

---

## Reservation Class

### **Class Overview**
The `Reservation` class is a data-carrying object that represents a single, confirmed booking. It acts as a "receipt," linking a specific `User` to one or more `Seat` objects for a given `Showtime`.
It is created when a user finalizes their seat selection. The class constructor handles the task of telling the `Showtime` to mark the seats as booked, and its `cancelAllSeats` method tells the `Showtime` to free them.

---

### **Field Table**

| Field Name    | Access Modifier | Type            | Description                                            |
|:--------------|:----------------|:----------------|:-------------------------------------------------------|
| `bookingID`   | private final   | String          | A unique ID identifying this specific reservation.     |
| `user`        | private final   | User            | The user account that owns this reservation.           |
| `showtime`    | private final   | Showtime        | The specific showtime this reservation is for.         |
| `bookedSeats` | private final   | ArrayList<Seat> | The list of Seat objects associated with this booking. |
| `bookingTime` | private final   | LocalDateTime   | The timestamp of when the reservation was created.     |

---

### **Method Table**

| Method Name        | Return Type     | Access Modifier | Parameters                                          | Description                                                                                                      | How It Was Tested                                                                                                                                                                                        |
|:-------------------|:----------------|:----------------|:----------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Reservation()`    | Constructor     | public | User user, Showtime showtime, ArrayList<Seat> seats | Initializes all fields, generates a bookingID and bookingTime, and tells the Showtime to book each seat.         | Verified by checking `showtime.isSeatAvailable()` returned `false` after creation.                                                                                                                       |
| `getBookingID()`   | String          | public | None                                                | Returns this reservation's unique ID.                                                                            | Verified that the returned ID was not null or empty.                                                                                                                                                     |
| `getUser()`        | User            | public | None                                                | Returns the `User` who made this booking.                                                                        | Verified the returned `User` object was the same one passed to the constructor.                                                                                                                          |
| `getShowtime()`    | Showtime        | public | None                                                | Returns the associated `Showtime`.                                                                               | Verified the returned `Showtime` object was the same one passed to the constructor.                                                                                                                      |
| `getBookedSeats()` | ArrayList<Seat> | public | None                                                | Returns the list of seats reserved.                                                                              | Verified by checking the list size and for the presence of the specific `Seat` objects.                                                                                                                  |
| `getBookingTime()` | LocalDateTime   | public | None                                                | Returns the timestamp of reservation creation.                                                                   | Verified that the returned `LocalDateTime` was not null.                                                                                                                                                 |
| `getTotalPrice()`  | double          | public | None                                                | Calculates and returns the total cost by summing the price of each individual seat in `bookedSeats`.             | Verified by comparing the return value to a pre-calculated sum of the seats' prices.                                                                                                                     |
| `cancelAllSeats()` | void            | public | None                                                | Frees all seats in this reservation by calling `showtime.cancelSeat()` for each one, then clears the local list. | Verified the internal seat list was empty and that `showtime.isSeatAvailable()` returned `true` for the seats.                                                                                                                                                                                                         |

---

## **Movie Class**

### **Class Overview**
The `Movie` class models the core data attributes of a film shown in the system.  
It provides a clean, immutable structure for storing and retrieving movie details such as title, genre, rating, runtime, and poster path.  
This class is serializable and implements the `IMovie` interface to ensure cross-module compatibility.

---

### **Field Table**

| Field Name | Access Modifier | Type | Description |
|:------------|:----------------|:------|:--------------|
| `title` | private final | String | The name of the movie; serves as a unique identifier. |
| `genre` | private final | String | The genre or category of the movie (e.g., Action, Comedy, Drama). |
| `rating` | private final | String | The rating of the movie (e.g., PG-13, R). |
| `runtime` | private final | int | Duration of the movie in minutes; cannot be negative. |
| `posterPath` | private | String | Optional path to the movie’s poster image file. |

---

### **Method Table**

| Method Name | Return Type | Access Modifier | Parameters | Description | How It Was Tested |
|:--------------|:-------------|:----------------|:-------------|:------------------|:------------------|
| `Movie(String title, String genre, String rating, int runtime, String posterPath)` | Constructor | public | title, genre, rating, runtime, posterPath | Initializes all attributes; validates that `title` is non-null and `runtime` is non-negative. | Verified through `MovieTest` constructor tests with valid and invalid inputs. |
| `Movie(String title, int runtime)` | Constructor | public | title, runtime | Convenience constructor allowing minimal initialization. | Verified default field values through `MovieTest`. |
| `getTitle()` | String | public | None | Returns the title of the movie. | Tested via getter validation in `MovieTest`. |
| `getGenre()` | String | public | None | Returns the genre of the movie. | Tested via field verification. |
| `getRating()` | String | public | None | Returns the rating. | Tested for null and valid values. |
| `getRuntime()` | int | public | None | Returns runtime in minutes. | Tested for non-negative validation. |
| `getPosterPath()` | String | public | None | Returns the poster path, if any. | Tested by setting and retrieving paths. |
| `setPosterPath(String path)` | void | public | path | Updates the poster path dynamically. | Tested by modifying and rechecking the field. |
| `toString()` | String | public | None | Returns formatted movie details string. | Manually verified for clarity and correctness. |
| `equals(Object o)` | boolean | public | o | Compares movies based on all fields. | Tested by comparing identical and different Movie objects. |
| `hashCode()` | int | public | None | Generates hash based on all fields for use in hash-based collections. | Verified by comparing hash consistency for equal objects. |

---

## **Showtime Class**

### **Class Overview**
The `Showtime` class represents a single scheduled screening of a movie.  
It manages seat layouts, booking and cancellation operations, pricing, and synchronization for multi-threaded environments.  
Each instance links to a specific `Movie` and a specific `LocalDateTime`.  
All booking-related methods are thread-safe to handle simultaneous client actions reliably.

---

### **Field Table**

| Field Name | Access Modifier | Type | Description |
|:------------|:----------------|:------|:--------------|
| `movie` | private final | Movie | The movie associated with this showtime. |
| `dateTime` | private final | LocalDateTime | The date and time of the screening. |
| `seats` | private final | Seat[][] | 2D array representing seating layout. |
| `booked` | private final | boolean[][] | Parallel 2D array tracking seat availability (true = booked). |
| `basePrice` | private | double | The starting ticket price for the showtime. |
| `auditoriumName` | private | String | Name of the theater room or auditorium. |

---

### **Method Table**

| Method Name | Return Type | Access Modifier | Parameters | Description | How It Was Tested |
|:--------------|:-------------|:----------------|:-------------|:------------------|:------------------|
| `Showtime(Movie movie, LocalDateTime dateTime, Seat[][] seats, double basePrice, String auditoriumName)` | Constructor | public | movie, dateTime, seats, basePrice, auditoriumName | Initializes a showtime with a provided seating chart; validates all inputs. | Tested with valid and invalid arguments to verify exceptions. |
| `Showtime(Movie movie, LocalDateTime dateTime, int rows, int cols, double basePrice, String auditoriumName)` | Constructor | public | movie, dateTime, rows, cols, basePrice, auditoriumName | Creates an empty seat chart with specified dimensions. | Verified through `ShowtimeTest` for correct matrix initialization. |
| `getMovie()` | Movie | public | None | Returns the movie being shown. | Checked via equality to constructor parameter. |
| `getDateTime()` | LocalDateTime | public | None | Returns the date and time of the screening. | Verified with constructor assignment. |
| `getSeat(int row, int col)` | Seat | public | row, col | Returns the seat object at specified coordinates. | Tested by retrieving known positions. |
| `bookSeat(int row, int col)` | boolean | public | row, col | Books the specified seat if available; synchronized for thread safety. | Verified by confirming booked state transitions from false → true. |
| `cancelSeat(int row, int col)` | boolean | public | row, col | Cancels a booked seat; returns false if seat was not previously booked. | Tested through booking and then canceling same seat. |
| `isSeatAvailable(int row, int col)` | boolean | public | row, col | Checks if a seat is free to book. | Verified before and after booking. |
| `getAvailableSeatCount()` | int | public | None | Returns total number of unbooked seats. | Tested with partial booking scenarios. |
| `getBasePrice()` | double | public | None | Returns base ticket price. | Verified through getter check. |
| `setBasePrice(double price)` | void | public | price | Updates base price; ensures non-negative value. | Tested with valid and invalid price inputs. |
| `getAuditoriumName()` | String | public | None | Returns the auditorium name. | Verified against initialization value. |
| `setAuditoriumName(String name)` | void | public | name | Updates auditorium name. | Verified by setting and retrieving new name. |
| `getRowCount()` | int | public | None | Returns number of rows in seating chart. | Tested with small and large seating layouts. |
| `getColCount()` | int | public | None | Returns number of columns in seating chart. | Verified with different seating configurations. |
| `getSeats()` | Seat[][] | public | None | Returns internal 2D seat array reference. | Verified by confirming same matrix structure. |
| `toString()` | String | public | None | Returns formatted details of the showtime, including movie title and seat layout summary. | Verified visually in log outputs. |

---

## **Seat Table**

### **Class Overview**
The `seat` class represents an individual seat and its current state. 
This class manages checking the states of seats and the prices of those seats.

| Field Name | Access Modifier | Type | Description |
| :---- | :---- | :---- | :---- |
| `row` | private | int | Labels the rows from A-Z |
| `number` | private | int | Sets the seat number |
| `booked` | private | boolean | Checks whether the seat has been booked or not |
| `price` | private | double | Sets the price for a seat |

---

| Method | Return | Access | Parameters | Description | Test |
| :---- | :---- | :---- | :---- | :---- | :---- |
| `getRow()` | int | public | none | Returns the seat’s row number | Verified in testInitialValues() to ensure the constructor correctly assigned the right number for row. |
| `getNumber()` | int | public | none | Returns the seat number within the row | Checked in testInitialValues() to confirm that it received the right number for the seat. |
| `isBooked()` | boolean | public | none | Returns true if the seat is currently booked | Tested in multiple tests such as testInitialValues(), testBookSeat(), and testCancelSeat() returning true and false to test conditions. |
| `book()` | void | public | none | Marks seat as booked if not already booked | Verified in testBookSeat() and testDoubleBookingDoesNotUnbook() to confirm the status of booking |
| `cancel()` | void | public | none | Marks the seat as free for booking if it was booked previously | Validated in testCancelSeat() and testCancelWhenNotBookedDoesNothing() to make sure that booking status changed when cancelled |
| `getPrice()` | double | public | none | Returns current price of the seat | Confirmed in testInitialValues() and testPriceUpdate() to make sure price was returning the correct value |
| `setPrice` | void | public | double P | Updates seat price to the given value | Tested in testPriceUpdate() to ensure price is correctly updated to given value. |
| `getSeatLabel` | String | public | none | Returns a label for the seat based on row and letter, such as A1 or B7 | Verified in testInitialValues() and testAnotherSeatLabel() to make sure getSeatLabel is returning the correct SeatLabel |

---

## Phase 2

## Phase 2

This phase extends the system into a full client–server application, adding networked interaction on top of the existing database and domain classes. It introduces:

- **Protocol** – centralizes all command strings (e.g., `LOGIN`, `LIST_MOVIES`, `BOOK`), response types (`SUCCESS`, `ERROR`), and delimiters used in messages between client and server. 


- **Server** – starts the booking service, loads (or initializes) the shared `Database`, opens a listening `ServerSocket` on the configured port, and accepts incoming client connections. For each connection, it creates and starts a dedicated `ClientHandler`, enabling multiple clients to interact concurrently.

- **ClientHandler** – runs in its own thread per client and is responsible for interpreting commands, validating input, enforcing authentication and admin permissions, interacting with the `Database` (movies, showtimes, users, reservations), and sending structured responses back to the client.

- **Client** – provides a text-based user interface that connects to the server.

All new Phase 2 classes are covered by **JUnit 5** test cases, including constructors and all non-`run()` methods. Private method logic is exercised via reflection-based tests as suggested on Ed.

---

## How to use this program

All system requirements remain the same as Phase 1, including the use of Java 17 or later and JUnit 5 for testing. The program now runs through a client–server model and must be launched in the correct order.

### Before the First Run
- If a `myDataBase.ser` file already exists **(Before the first local run)** from a previous execution, it must be deleted before starting the server.
- This ensures the system initializes with a clean state and creates the default administrator account.

### Starting the Server
1. Run the `Server` class.
2. The server will load the database if it exists, or create a new one. (if it is the first run then it is essential that the database is created anew.)
3. On first startup, the server automatically creates the default admin account:
    - **Username:** admin
    - **Password:** admin123

### Logging in as Admin
- Start the `Client` program after the server is running.
- Use the default admin credentials to sign in.
- The admin interface allows:
    - Adding movies to the system
    - Adding showtimes for existing movies
    - Promoting another registered user to admin
    - Viewing all bookings in the system
- The database begins empty, so the admin must add movies and showtimes before any user can make bookings.

### Logging in as a Regular User

Note: Everytime you logout as a user and wish to login as another user it is imperative that the client must be refreshed. Especially if the user logging in is an admin.
- Registration must occur first through the client interface.
- Then the user can login after they've registered
- Users can:
    - Browse movies
    - View available showtimes
    - Select seats and complete a booking
    - View and cancel their own reservations

### Important Client Session Behavior
- To log in as a different user, the client program must be fully exited and restarted.
- Logging in as a guest and then as an admin within the same client session is not supported.
- If attempted, the system will log in successfully but will not display the admin menu, so a restart is required.

### Using the Application
- Menus guide the user through all available options.
- Seating displays indicate available and booked seats.
- A unique booking ID is generated for each reservation.
- Admin and guest menus are distinct.

---

## Team Member Contributions

### Gaurav Mandhyan

- **Role:** Team Leader

- **Main Responsibilities:**
    - Set up network I/O.
    - Created Server class.
    - Created Server Test.
    - Created all Phase 2 interfaces.
    - Debugged Client Handler and Client (manual integration testing).
    - User Testing.
    - Wrote Client Handler Test.
    - Wrote Associated ReadMe sections.

---

### Naisha Patel & Arbin Isaac De La Torre - Rodriguez

- **Main Responsibilities:**
    - Created Client Handler class. 
    - Wrote Client Handler Test.
    - Created Client class.
    - Wrote Client Test. 
    - Wrote Associated ReadMe sections.

---


### Jakob Graham
- **Main Responsibilities:**
    - Modified all Phase 1 classes as needed for Phase 2.
    - Tested the Same.
    - Resolved CheckStyle errors.
    - User testing.
    - Debugged Client Handler and Client (manual integration testing).
    - Wrote associated ReadMe sections.

---

## Class Descriptions   

## Server Class

### **Class Overview**
The `Server` class manages the backend networking component of the system, accepting client connections and delegating them to handler threads.
It also loads or initializes the database and ensures that a default administrator account exists when the server starts.

---

### **Field Table**

| Field Name | Access Modifier | Type | Description |
|:-----------:|:----------------:|:------:|:--------------:|
| `PORT` | private static final | int | The fixed port number used for server communication. |
| `serverSocket` | private | ServerSocket | The socket responsible for listening for incoming client connections. |
| `running` | private volatile | boolean | Flag indicating whether the server is actively running. |
| `database` | private final | Database | Stores persistent system data including users, movies, showtimes, and reservations. |

---

### **Method Table**

| Method Name | Return Type | Access Modifier | Parameters | Description | How It Was Tested |
|:--------------:|:--------------:|:----------------:|:-------------:|:------------------|:------------------|
| `Server()` | Constructor | public | None | Loads existing database if available, otherwise creates a fresh one, and initializes a default admin account. | Verified through constructor tests checking database initialization and admin creation. |
| `getDatabase()` | Database | public | None | Returns the database instance used by the server. | Tested by confirming non-null return and same instance across calls. |
| `run()` | void | public | None | Starts the server socket, accepts client connections, and launches handler threads. | None Required |
| `stop()` | void | public | None | Stops the server by setting running to false and closing the server socket. | Tested using reflection to confirm running becomes false and that stop does not throw. |
| `initializeDefaultAdmin()` | void | private | None | Ensures a built-in administrator account exists in the database. | None Required |
| `main(String[] args)` | void | public static | String[] args | Entry point that creates and launches the server in a new thread. | None Required |

---

## ClientHandler Class

### **Class Overview**
The `ClientHandler` class manages all communication between a connected client and the server. It interprets protocol commands, performs authentication, handles booking operations, and returns formatted responses back to the client. Each instance runs in its own thread and interacts directly with the shared database.

---

### **Field Table**

| Field Name | Access Modifier | Type | Description |
|:-----------:|:----------------:|:------:|:--------------:|
| `socket` | private final | Socket | The network socket associated with this client connection. |
| `server` | private final | Server | Reference to the server enabling access to shared resources. |
| `db` | private final | Database | The shared database instance storing users, movies, showtimes, and reservations. |
| `in` | private | BufferedReader | Input stream for receiving client messages. |
| `out` | private | PrintWriter | Output stream for sending protocol responses to client. |
| `currentUser` | private | User | The authenticated user associated with this connection. |
| `isAuthenticated` | private | boolean | Tracks whether the user has successfully logged in. |
| `USERNAME_PATTERN` | private static final | Pattern | Validation rule enforcing allowed username formats. |
| `EMAIL_PATTERN` | private static final | Pattern | Validation rule enforcing valid email structure. |
| `DATE_TIME_FORMATTER` | private static final | DateTimeFormatter | Formatter for parsing and formatting showtime date values. |

---

### **Method Table**

| Method Name | Return Type | Access Modifier | Parameters | Description | How It Was Tested |
|:--------------:|:--------------:|:----------------:|:-------------:|:------------------|:------------------|
| `ClientHandler(Socket socket, Server server)` | Constructor | public | socket, server | Initializes handler streams and links to server database. | Automatically used in setup of ClientHandlerTest. |
| `setupStreams()` | void | private | None | Initializes input and output streams for communication. | Tested in `testSetupStreamsInitializesInAndOut`. |
| `closeEverything()` | void | private | None | Closes streams and socket safely when client disconnects. | Tested in `testCloseEverythingClosesSocket`. |
| `send(String message)` | void | private | message | Sends raw protocol line to client. | Tested in `testSendWritesRawMessage`. |
| `sendSuccess(String message)` | void | private | message | Sends SUCCESS-prefixed protocol response. | Tested in `testSendSuccessPrefixesSuccess`. |
| `sendError(String message)` | void | private | message | Sends ERROR-prefixed protocol response. | Tested in `testSendErrorPrefixesError`. |
| `handleCommand(String input)` | void | private | input | Parses command and dispatches to appropriate handler. | Tested indirectly in `testHandleCommandInvalidCommand`. |
| `handleLogin(String[] parts)` | void | private | parts | Validates credentials and authenticates user. | Tested in `testHandleLoginSuccess` and `testHandleLoginBadPassword`. |
| `handleRegister(String[] parts)` | void | private | parts | Creates a new user after validating inputs. | Tested in `testHandleRegisterSuccess` and `testHandleRegisterBadEmail`. |
| `handleLogout()` | void | private | None | Logs out current user and clears authentication state. | Tested in `testHandleLogoutResetsAuth`. |
| `handleListMovies()` | void | private | None | Sends list of all movies to client. | Tested in `testHandleListMoviesWithOneMovie`. |
| `handleListShowtimes(String[] parts)` | void | private | parts | Sends all showtimes for a given movie. | Tested in `testHandleListShowtimesForMovie`. |
| `handleViewSeats(String[] parts)` | void | private | parts | Sends formatted seat availability for a showtime. | Tested in `testHandleViewSeatsShowsAllAvailable`. |
| `handleBookSeats(String[] parts)` | void | private | parts | Books requested seats and creates reservation. | Tested in `testHandleBookSeatsSuccess` and `testHandleBookSeatsRejectsDuplicateSelection`. |
| `handleCancelReservation(String[] parts)` | void | private | parts | Cancels an existing reservation if owned by current user. | Tested in `testHandleCancelReservationSuccess`. |
| `handleMyBookings()` | void | private | None | Sends list of bookings belonging to current user. | Tested in `testHandleMyBookingsListsReservations`. |
| `handleAdminAddMovie(String[] parts)` | void | private | parts | Allows admin to add a new movie. | Tested in `testHandleAdminAddMovieSuccess`. |
| `handleAdminAddShowtime(String[] parts)` | void | private | parts | Allows admin to add a new showtime. | Tested in `testHandleAdminAddShowtimeSuccess`. |
| `handleAdminPromoteUser(String[] parts)` | void | private | parts | Promotes a normal user to admin. | Tested in `testHandleAdminPromoteUser`. |
| `handleAdminViewAllBookings()` | void | private | None | Lists all reservations to admin. | Tested in `testHandleAdminViewAllBookings`. |
| `findShowtimeById(String showtimeId)` | Showtime | private | showtimeId | Resolves internal showtime reference by formatted ID. | Tested in `testFindShowtimeByIdViaReflection`. |

---

## **Client Table**

### **Class Overview**
The `client` class is the user-facing terminal program. 
It connects to the server, receives textual prompts, and sends user commands. 
The client stores no data locally, everything is requested from the server.

| Field Name | Access Modifier | Type | Description |
| :---- | :---- | :---- | :---- |
| `host` | Private final | String | The hostname/IP address that the client connects to. |
| `port` | Private final | int | The server port that the client uses to establish a connection. |
| `socket` | private | Socket | Socket used to connect to the server. |
| `serverIn` | private | BufferedReader | Reads inputs from the server. |
| `serverOut` | private | PrintWriter | Sends outputs to the server. |
| `userIn` | Private final | Scanner | Reads input from user |
| `isLoggedIn` | private | boolean | Tracks whether the user is currently logged in. |
| `currentUsername` | private | String | Stores the username of the currently logged in user. |
| `isAdmin` | private | boolean | Tracks whether the user who is logged in has admin privileges. |

---

| Method | Return | Access | Parameters | Description | Test |
| :---- | :---- | :---- | :---- | :---- | :---- |
| `Client` | NA | public | String host, int port | Constructor which initializes the client with the server host, port, and scanner for user input. | Indirectly tested via flow based tests such as testSuccessfulLogin() and testRegisterSuccess() |
| `start` | void | public | none | Connects to the server, displays the welcome message, and starts the main menu loop. | Not tested directly to avoid opening the real socket, but private methods that are called by start are tested individually via reflection. |
| `mainMenu` | void | private | none | Handles the main menu, with it directing users to login, register, or exit, with access to the admin menu, or guest menu being based on the login status. | Indirectly tested via login(), register(), logout(), menu private methods. |
| `login` | void | private | none | Prompts user for username and password, sends login request to the server, updates login and checks if logged in user is admin. | testSuccessfulLogin() |
| `register` | void | private | none | Prompts user for username, password, and email, sends registration request to the server. | testRegisterSuccess() |
| `logout` | void | private | none | Logs out the user, resets login and admin status, and informs the user of this change. | testSuccessfulLogin() indirectly after menu flow |
| `guestMenu` | void | private | none | Displays the guest menu for logged in users who are not admins and handles user choices. | Indirectly tested via guest actions in tests like testListMovies() |
| `adminMenu` | void | private | none | Displays the admin menu for logged in admin users and handles admin choices. | Indirectly tested via reflection tests calling addMovie, addShowtime, promoteUser |
| `listMovies` | void | private | none | Requests the list of movies from the server, displays them, and optionally show showtimes for selected movie | testListMovies() |
| `showShowtimesForMovies` | void | private | String movieId, String movieTitle | Requests showtimes for a specific movie and displays them. | testListMovies() indirectly via movie selection flow. |
| `bookSeats` | void | private | none | Handles the full seat booking workflow, including movie selection, showtime selection, seat selection, and booking confirmation | testListMovies() indirectly via the booking seats flow. |
| `viewSeatMap` | int\[\]\[\] | private | String showtimeId | Requests and displays the seat map for a given showtime, returns a 2D array representing available and booked seats. | Reflection-based tests with mock responses. |
| `viewMyBookings` | void | private | none | Requests and displays all bookings for the logged-in user | Reflection-based tests with mock responses |
| `addMovie` | void | private | none | Prompts the admin to enter new movie details and sends the add-movie request to the server | Reflection-based tests with mock responses |
| `addShowTime` | void | private | none | Prompts the admin to enter new showtime details and sends the add-showtime request to the server. | Reflection-based admin tests |
| `promoteUser` | void | private | none | Prompts for a username and sends a request to promote that user to admin. | Reflection-based admin tests |
| `main` | void | public static | String\[ \]args | Entry point of the program. Creates a Client instance connecting to localhost: 4242 and starts it. | Reflection-based admin testsI |
