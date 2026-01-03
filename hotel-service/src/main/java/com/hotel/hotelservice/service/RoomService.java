package com.hotel.hotelservice.service;

import com.hotel.hotelservice.dto.request.BulkCreateRoomRequest;
import com.hotel.hotelservice.dto.request.CreateRoomRequest;
import com.hotel.hotelservice.dto.response.RoomSuggestionResponse;

public interface RoomService {

    RoomSuggestionResponse suggestRoom(
            Long hotelId,
            Long categoryId,
            String role
    );
    
    void updateRoomStatus(Long roomId, String status, String role);
    
    void createRoom(Long hotelId, CreateRoomRequest request, String role);

    void bulkCreateRooms(Long hotelId, BulkCreateRoomRequest request, String role);

}
