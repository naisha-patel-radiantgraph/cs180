package seat;

import interfaces.ISeat;

public class Seat implements ISeat {

    private int row;
    private int number;
    private boolean booked;
    private double price;

    public Seat(int row, int number, double price) {
        this.row = row;
        this.number = number;
        this.price = price;
        this.booked = false;
    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public boolean isBooked() {
        return booked;
    }

    @Override
    public void book() {
        if(!booked) {
            booked = true;
        }
    }

    @Override
    public void cancel() {
         if(booked) {
            booked = false;
        }
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public void setPrice(double p) {
        this.price = p;
    }

    @Override
    public String getSeatLabel() {
        char rowLetter = (char) ('A' + row);
        return rowLetter + Integer.toString(number + 1);
    }
}
