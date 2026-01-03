package com.hotel.hotelservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.hotelservice.entity.RoomAllocation;

public interface RoomAllocationRepository
extends JpaRepository<RoomAllocation, Long> {

Optional<RoomAllocation> findByRoomIdAndReleasedAtIsNull(Long roomId);

Optional<RoomAllocation> findByBookingIdAndReleasedAtIsNull(Long bookingId);
}
