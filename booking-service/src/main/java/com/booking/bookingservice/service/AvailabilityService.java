package com.booking.bookingservice.service;

import java.time.LocalDate;

public interface AvailabilityService {

    boolean isAvailable(
            Long hotelId,
            Long categoryId,
            LocalDate checkIn,
            LocalDate checkOut
    );

    void reserve(
            Long hotelId,
            Long categoryId,
            LocalDate checkIn,
            LocalDate checkOut
    );

    void release(
            Long hotelId,
            Long categoryId,
            LocalDate checkIn,
            LocalDate checkOut
    );
}
