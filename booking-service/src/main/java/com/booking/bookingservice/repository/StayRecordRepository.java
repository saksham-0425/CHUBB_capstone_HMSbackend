package com.booking.bookingservice.repository;

import com.booking.bookingservice.model.StayRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StayRecordRepository
        extends JpaRepository<StayRecord, Long> {

    Optional<StayRecord> findByReservationId(Long reservationId);
}
