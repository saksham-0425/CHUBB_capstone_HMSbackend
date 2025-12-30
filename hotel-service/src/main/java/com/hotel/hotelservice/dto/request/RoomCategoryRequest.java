package com.hotel.hotelservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String category;

    @NotNull
    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;

    @NotNull
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @NotNull
    @Min(value = 1, message = "Base price must be positive")
    private Double basePrice;
}
