package com.hotel.hotelservice.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.hotelservice.entity.Room;
import com.hotel.hotelservice.entity.RoomAllocation;
import com.hotel.hotelservice.entity.RoomStatus;
import com.hotel.hotelservice.repository.RoomAllocationRepository;
import com.hotel.hotelservice.repository.RoomRepository;
import com.hotel.hotelservice.service.RoomAllocationService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomAllocationServiceImpl implements RoomAllocationService {

    private final RoomRepository roomRepository;
    private final RoomAllocationRepository roomAllocationRepository;

    @Override
    @Transactional
    public void allocateRooms(
            Long bookingId,
            Long hotelId,
            Long categoryId,
            int numberOfRooms
    ) {

        List<RoomAllocation> existing =
                roomAllocationRepository
                        .findAllByBookingIdAndReleasedAtIsNull(bookingId);

        if (!existing.isEmpty()) {
            throw new IllegalStateException(
                    "Rooms already allocated for booking " + bookingId
            );
        }

        List<Room> availableRooms =
                roomRepository.findByHotelIdAndCategoryIdAndStatusOrderByRoomNumberAsc(
                        hotelId,
                        categoryId,
                        RoomStatus.AVAILABLE
                );

        if (availableRooms.size() < numberOfRooms) {
            throw new IllegalStateException(
                    "Not enough available rooms. Required: "
                            + numberOfRooms
                            + ", Available: "
                            + availableRooms.size()
            );
        }

        List<Room> roomsToAllocate =
                availableRooms.subList(0, numberOfRooms);

        for (Room room : roomsToAllocate) {

            RoomAllocation allocation = RoomAllocation.builder()
                    .bookingId(bookingId)
                    .roomId(room.getId())
                    .allocatedAt(LocalDateTime.now())
                    .build();

            roomAllocationRepository.save(allocation);

            room.setStatus(RoomStatus.OCCUPIED);
            roomRepository.save(room);
        }
    }

    @Override
    @Transactional
    public void releaseRooms(Long bookingId) {

        List<RoomAllocation> allocations =
                roomAllocationRepository
                        .findAllByBookingIdAndReleasedAtIsNull(bookingId);

        if (allocations.isEmpty()) {
            throw new EntityNotFoundException(
                    "No active room allocations found for booking " + bookingId
            );
        }

        for (RoomAllocation allocation : allocations) {

            Room room = roomRepository.findById(allocation.getRoomId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Room not found for allocation"
                    ));

            allocation.setReleasedAt(LocalDateTime.now());
            roomAllocationRepository.save(allocation);

            room.setStatus(RoomStatus.CLEANING);
            roomRepository.save(room);
        }
    }
}
