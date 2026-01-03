package com.hotel.hotelservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AllocateRoomRequest {
    private Long bookingId;
    private Long hotelId;
    private Long categoryId;
}
