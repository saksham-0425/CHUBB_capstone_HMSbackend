package com.hotel.hotelservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoomRequest {
    private Long categoryId;
    private String roomNumber;
}
