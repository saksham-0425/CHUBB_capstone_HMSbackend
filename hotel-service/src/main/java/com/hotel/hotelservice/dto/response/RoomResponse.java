package com.hotel.hotelservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomResponse {
    private Long roomId;
    private String roomNumber;
}