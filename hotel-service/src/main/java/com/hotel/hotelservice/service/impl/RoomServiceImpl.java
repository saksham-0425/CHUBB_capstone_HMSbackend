package com.hotel.hotelservice.service.impl;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.hotel.hotelservice.dto.response.RoomResponse;
import com.hotel.hotelservice.dto.response.RoomSuggestionResponse;
import com.hotel.hotelservice.entity.Room;
import com.hotel.hotelservice.entity.RoomStatus;
import com.hotel.hotelservice.repository.RoomRepository;
import com.hotel.hotelservice.service.RoomService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

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
                .map(r -> new RoomResponse(r.getId(), r.getRoomNumber()))
                .toList();

        return new RoomSuggestionResponse(
                responses.get(0),   // auto-suggested
                responses           // full list
        );
    }
}
