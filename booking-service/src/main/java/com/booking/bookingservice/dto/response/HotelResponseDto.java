package com.booking.bookingservice.dto.response;

import lombok.Data;

@Data
public class HotelResponseDto {

    private Long id;
    private String name;
    private String city;
    private boolean active;
}
