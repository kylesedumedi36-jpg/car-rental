package com.mycompany.rentalmanagementapp;

public class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}
