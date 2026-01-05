package com.hotel.hotelservice.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;

@Getter
@AllArgsConstructor
@Builder
public class RoomSuggestionResponse {

    private RoomResponse suggestedRoom;
    private List<RoomResponse> availableRooms;
}