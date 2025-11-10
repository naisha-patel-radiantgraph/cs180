## Project Description

For our project, we selected **Option 2**, which involves building an **Movie Ticket Booking System**. The goal of this project is to design and implement a modular, object-oriented system that accurately simulates how an industrial-style movie booking platform operates.

This phase establishes the core database framework, including:
- **Movie** – stores movie details such as title, genre, runtime, and poster path.
- **Showtime** – manages screening schedules, seating arrangements, and booking states.
- **User** – represents individual customers (or admins) capable of making and canceling reservations.
- **Reservation** – handles the linkage between users, showtimes, and booked seats.
- **Database** – provides centralized, synchronized data storage with support for saving and loading the system state using serialization.

Each component has been tested through **JUnit 5** test cases to validate appropriate functionality.

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

## Tentative User Flow Diagram

The following diagram flowchart displays how a user might interact with our program:

![UML Diagram](./images/userflowdiagram1.png)

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

