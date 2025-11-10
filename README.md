## Project Description

For our project, we selected **Option 2**, which involves building an **Movie Ticket Booking System**. The goal of this project is to design and implement a modular, object-oriented system that accurately simulates how an industrial-style movie booking platform operates.

This phase establishes the core database framework, including:
- **Movie** – stores movie details such as title, genre, runtime, and poster path.
- **Showtime** – manages screening schedules, seating arrangements, and booking states.
- **User** – represents individual customers (or admins) capable of making and canceling reservations.
- **Reservation** – handles the linkage between users, showtimes, and booked seats.
- **Database** – provides centralized, synchronized data storage with support for saving and loading the system state using serialization.

Each component has been tested through **JUnit 5** test cases to validate appropriate functionality.

---

## Tentative Expected End Goal

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

### Arbin
- **Main Responsibilities:**
    - Created and implemented Seat class.
    - Created and implemented Seat interface.
    - Created and implemented Seat test class.
    - Wrote associated ReadMe sections.
    - Created User flow diagram.

---

### Jakob
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