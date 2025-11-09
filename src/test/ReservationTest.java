package test;

import database.Database;
import user.User;
import movie.Movie;
import showtime.Showtime;
import seat.Seat;
import reservation.Reservation;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ReservationTest {

    public static void main(String[] args) {
        System.out.println("Running Reservation Tests...");
        int testsPassed = 0;
        int testsFailed = 0;

        int rows = 5;
        int cols = 5;
        double basePrice = 12.50;
        double premiumPrice = 18.00;
        Seat[][] seatingChart = new Seat[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double price = (r == 4) ? premiumPrice : basePrice;
                seatingChart[r][c] = new Seat(r, c, price);
            }
        }

        Showtime testShowtime = new Showtime(testMovie, testDateTime, seatingChart, basePrice, "Auditorium 1");

        ArrayList<Seat> seatsToBook = new ArrayList<>();
        Seat seat1 = testShowtime.getSeat(2, 3);
        Seat seat2 = testShowtime.getSeat(4, 4);
        seatsToBook.add(seat1);
        seatsToBook.add(seat2);

        double expectedPrice = basePrice + premiumPrice;

        System.out.println("\n--- Test: Reservation Creation, Getters, and Price ---");

        Reservation reservation = new Reservation(testUser, testShowtime, seatsToBook);

        // Test 1: getUser
        if (reservation.getUser() == testUser) {
            System.out.println("[PASS] getUser() returned correct user.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] getUser() returned incorrect user.");
            testsFailed++;
        }
        // Test 2: GetShowtime
        if (reservation.getBookingTime() == testShowtime) {
            System.out.println("[PASS] getShowtime() returned correct showtime.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] getShowtime() returned incorrect showtime.");
            testsFailed++;
        }
        // Test 3: GetBookedSeats
        if (reservation.getBookedSeats().size() == 2 && reservation.getBookedSeats().contains(seat1)) {
            System.out.println("[PASS] getBookedSeats() returned correct list.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] getBookedSeats() returned incorrect list.");
            testsFailed++;
        }
        // Test 4: GetBookingID
        if (reservation.getBookingID() != null && !reservation.getBookingID().isEmpty()) {
            System.out.println("[PASS] getBookingID() generated an ID.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] getBookingID() was null or empty.");
            testsFailed++;
        }
        // Test 5: GetBookingTime
        if (reservation.getBookingTime() != null) {
            System.out.println("[PASS] getBookingTime() was set.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] getBookingTime() was null.");
            testsFailed++;
        }
        // Test 6: GetTotalPrice
        if (Math.abs(reservation.getTotalPrice() - expectedPrice) < 0.001) {
            System.out.println("[PASS] getTotalPrice() calculated correct total: " + reservation.getTotalPrice());
            testsPassed++;
        } else {
            System.out.println("[FAIL] getTotalPrice() was incorrect. Expected: " + expectedPrice + ", Got: " + reservation.getTotalPrice());
            testsFailed++;
        }
        // Test 7: Verify Showtime state
        boolean seat1Booked = !testShowtime.isSeatAvailable(2, 3);
        boolean seat2Booked = !testShowtime.isSeatAvailable(4, 4);
        if (seat1Booked && seat2Booked) {
            System.out.println("[PASS] Seats are correctly marked as unavailable in Showtime.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] Seats were not marked as unavailable in Showtime.");
            testsFailed++;
        }

        System.out.println("\n--- TEST: Reservation Cancellation ---");
        reservation.cancelAllSeats();
        // Test 8: Reservation list empty
        if (reservation.getBookedSeats().isEmpty()) {
            System.out.println("[PASS] cancelAllSeats() cleared the internal list.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] cancelAllSeats() did not clear the internal list.");
            testsFailed++;
        }
        // Test 9: Verify Showtime state
        boolean seat1Freed = testShowtime.isSeatAvailable(2, 3);
        boolean seat2Freed = testShowtime.isSeatAvailable(4, 4);
        if (seat1Freed && seat2Freed) {
            System.out.println("[PASS] Seats are correctly marked as available again in Showtime.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] Seats were not freed in Showtime after cancellation.");
            testsFailed++;
        }

        System.out.println("\n--- TEST SUMMARY ---");
        System.out.println("Total Tests: " + (testsPassed + testsFailed));
        System.out.println("Passed: " + testsPassed);
        System.out.println("Failed: " + testsFailed);
    }
}