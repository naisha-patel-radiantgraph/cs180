package interfaces;  //change this to match your project package

public interface ISeat {
    int getRow();              //returns number of the row of the seat (0 = first row)
    int getNumber();           //returns the seat number within the row
    boolean isBooked();        //returns true if the seat is currently booked
    void book();            //attempts to book the seat and returns true if successful
    void cancel();          //attempts to cancel a booking and will returns true if successful
    double getPrice();         //returns the current price of the seat
    void setPrice(double p);   //updates the price of the seat to the given value
    String getSeatLabel();     //returns a readable label for the seat (A6 or B5)
}
