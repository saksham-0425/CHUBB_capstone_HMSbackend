package com.booking.bookingservice.repository;

import com.booking.bookingservice.model.Reservation;
import com.booking.bookingservice.model.ReservationStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByBookingReference(String bookingReference);
    List<Reservation> findByStatusAndCheckInDateAndCheckInReminderSentFalse(
            ReservationStatus status,
            LocalDate checkInDate
    );

    List<Reservation> findByStatusAndCheckOutDateAndCheckOutReminderSentFalse(
            ReservationStatus status,
            LocalDate checkOutDate
    );
    List<Reservation> findByUserEmailOrderByCheckInDateDesc(String userEmail);
    
    List<Reservation> findByHotelId(Long hotelId);

}
