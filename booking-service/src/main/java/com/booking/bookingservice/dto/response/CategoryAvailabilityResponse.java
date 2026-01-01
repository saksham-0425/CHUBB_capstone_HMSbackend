package com.booking.bookingservice.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryAvailabilityResponse {

    private Long categoryId;
    private String categoryName;
    private int availableRooms;
    private BigDecimal pricePerNight;
}
