package com.booking.bookingservice.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomCategoryResponseDto {

    private Long id;
    private String category;
    private Integer totalRooms;
    private BigDecimal basePrice;
    private boolean active;
    Integer capacity; 
}
