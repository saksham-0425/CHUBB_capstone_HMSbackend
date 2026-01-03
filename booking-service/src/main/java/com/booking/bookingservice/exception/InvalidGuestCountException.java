package com.booking.bookingservice.exception;

public class InvalidGuestCountException extends RuntimeException {
    public InvalidGuestCountException(String message) {
        super(message);
    }
}
