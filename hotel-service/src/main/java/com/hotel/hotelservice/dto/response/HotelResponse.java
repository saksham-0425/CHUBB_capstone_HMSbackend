package com.hotel.hotelservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HotelResponse {

    private Long id;
    private String name;
    private String city;
    private String address;
    private String description;
    private String managerEmail;
    private List<String> amenities;
    private List<RoomCategoryResponse> roomCategories;
}
