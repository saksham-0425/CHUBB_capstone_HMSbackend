package com.booking.bookingservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AllocateRoomRequest {
    private Long bookingId;
    private Long hotelId;
    private Long categoryId;
    private int numberOfRooms;
}
