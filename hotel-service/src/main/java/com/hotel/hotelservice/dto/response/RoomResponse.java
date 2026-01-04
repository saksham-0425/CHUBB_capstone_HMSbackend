package com.hotel.hotelservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;


@Getter
@Builder
@AllArgsConstructor
public class RoomResponse {
    private Long roomId;
    private String roomNumber;
    private String category;
    private String status;
}