package com.hotel.hotelservice.service.impl;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.hotelservice.dto.request.BulkCreateRoomRequest;
import com.hotel.hotelservice.dto.request.CreateRoomRequest;
import com.hotel.hotelservice.dto.response.RoomResponse;
import com.hotel.hotelservice.dto.response.RoomSuggestionResponse;
import com.hotel.hotelservice.entity.Hotel;
import com.hotel.hotelservice.entity.Room;
import com.hotel.hotelservice.entity.RoomStatus;
import com.hotel.hotelservice.exception.ResourceNotFoundException;
import com.hotel.hotelservice.exception.UnauthorizedException;
import com.hotel.hotelservice.repository.HotelRepository;
import com.hotel.hotelservice.repository.RoomCategoryRepository;
import com.hotel.hotelservice.repository.RoomRepository;
import com.hotel.hotelservice.service.RoomService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomCategoryRepository roomCategoryRepository;

    @Override
    public RoomSuggestionResponse suggestRoom(
            Long hotelId,
            Long categoryId,
            String role
    ) {

        if (!role.equals("RECEPTIONIST") && !role.equals("MANAGER")) {
            throw new AccessDeniedException("Unauthorized");
        }

        List<Room> rooms = roomRepository
                .findByHotelIdAndCategoryIdAndStatusOrderByRoomNumberAsc(
                        hotelId,
                        categoryId,
                        RoomStatus.AVAILABLE
                );

        if (rooms.isEmpty()) {
            throw new IllegalStateException("No available rooms");
        }

        List<RoomResponse> responses = rooms.stream()
                .map(r -> RoomResponse.builder()
                        .roomId(r.getId())          
                        .roomNumber(r.getRoomNumber())
                        .build()
                )
                .collect(java.util.stream.Collectors.toList());

        return new RoomSuggestionResponse(
                responses.get(0),   // auto-suggested
                responses           // full list
        );
    }

    @Override
    @Transactional
    public void updateRoomStatus(Long roomId, String status, String role) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        RoomStatus newStatus = RoomStatus.valueOf(status);

        switch (newStatus) {

            case AVAILABLE -> {
                if (!role.equals("RECEPTIONIST") && !role.equals("MANAGER")) {
                    throw new AccessDeniedException("Unauthorized");
                }
                if (room.getStatus() != RoomStatus.CLEANING) {
                    throw new IllegalStateException(
                            "Room must be CLEANING before marking AVAILABLE"
                    );
                }
            }

            case MAINTENANCE -> {
                if (!role.equals("MANAGER") && !role.equals("ADMIN")) {
                    throw new AccessDeniedException("Unauthorized");
                }
                if (room.getStatus() == RoomStatus.OCCUPIED) {
                    throw new IllegalStateException(
                            "Cannot put OCCUPIED room into maintenance"
                    );
                }
            }

            case OUT_OF_SERVICE -> {
                if (!role.equals("ADMIN")) {
                    throw new AccessDeniedException("Only ADMIN allowed");
                }
            }

            default -> throw new IllegalArgumentException(
                    "Direct transition not allowed"
            );
        }

        room.setStatus(newStatus);
        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void createRoom(
            Long hotelId,
            CreateRoomRequest request,
            String role
    ) {

        if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new AccessDeniedException("Unauthorized");
        }

        if (roomRepository.existsByHotelIdAndRoomNumber(
                hotelId, request.getRoomNumber())) {
            throw new IllegalStateException("Room already exists");
        }

        Room room = Room.builder()
                .hotelId(hotelId)
                .categoryId(request.getCategoryId())
                .roomNumber(request.getRoomNumber())
                .status(RoomStatus.AVAILABLE)
                .build();

        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void bulkCreateRooms(
            Long hotelId,
            BulkCreateRoomRequest request,
            String role
    ) {

        if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new AccessDeniedException("Unauthorized");
        }

        if (request.getStart() > request.getEnd()) {
            throw new IllegalArgumentException("Invalid room range");
        }

        for (int i = request.getStart(); i <= request.getEnd(); i++) {

            String roomNumber = String.valueOf(i);

            if (roomRepository.existsByHotelIdAndRoomNumber(
                    hotelId, roomNumber)) {
                continue;
            }

            Room room = Room.builder()
                    .hotelId(hotelId)
                    .categoryId(request.getCategoryId())
                    .roomNumber(roomNumber)
                    .status(RoomStatus.AVAILABLE)
                    .build();

            roomRepository.save(room);
        }
    }

    @Override
    public List<RoomResponse> getRoomsByHotel(
            Long hotelId,
            String role,
            String email
    ) {

        if (!("ADMIN".equals(role) || "MANAGER".equals(role) || "RECEPTIONIST".equals(role))) {
            throw new UnauthorizedException("Access denied");
        }

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if ("MANAGER".equals(role) && !hotel.getManagerEmail().equals(email)) {
            throw new UnauthorizedException("Not your hotel");
        }

        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(room -> {

                    String categoryName =
                            roomCategoryRepository.findById(room.getCategoryId())
                                    .map(c -> c.getCategory())
                                    .orElse("UNKNOWN");

                    return RoomResponse.builder()
                            .roomId(room.getId())
                            .roomNumber(room.getRoomNumber())
                            .category(categoryName)
                            .status(room.getStatus().name())
                            .build();
                })
                .toList();
    }

}
