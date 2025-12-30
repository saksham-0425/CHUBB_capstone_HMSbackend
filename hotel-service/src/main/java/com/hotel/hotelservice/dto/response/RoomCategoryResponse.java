package com.hotel.hotelservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomCategoryResponse {

    private Long id;
    private String category;
    private Integer totalRooms;
    private Integer capacity;
    private Double basePrice;
}
