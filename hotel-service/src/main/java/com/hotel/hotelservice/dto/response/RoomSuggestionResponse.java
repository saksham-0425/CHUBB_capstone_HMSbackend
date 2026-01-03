package com.hotel.hotelservice.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomSuggestionResponse {

    private RoomResponse suggestedRoom;
    private List<RoomResponse> availableRooms;
}