package com.hotel.hotelservice.service;

import com.hotel.hotelservice.dto.response.RoomSuggestionResponse;

public interface RoomService {

    RoomSuggestionResponse suggestRoom(
            Long hotelId,
            Long categoryId,
            String role
    );
}
