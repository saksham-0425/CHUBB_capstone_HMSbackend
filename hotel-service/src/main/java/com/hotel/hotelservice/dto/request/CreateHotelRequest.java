package com.hotel.hotelservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateHotelRequest {

    @NotBlank(message = "Hotel name is required")
    private String name;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Address is required")
    private String address;

    private String description;

    @NotBlank(message = "Manager email is required")
    private String managerEmail;

    @NotEmpty(message = "Amenities cannot be empty")
    private List<String> amenities;

    @NotEmpty(message = "At least one room category must be provided")
    @Valid
    private List<RoomCategoryRequest> roomCategories;
}
