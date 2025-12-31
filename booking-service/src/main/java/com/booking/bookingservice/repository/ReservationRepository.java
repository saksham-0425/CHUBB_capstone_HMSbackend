package com.booking.bookingservice.repository;

import com.booking.bookingservice.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByBookingReference(String bookingReference);
}
