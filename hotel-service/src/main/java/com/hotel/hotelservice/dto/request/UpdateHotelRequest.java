package com.hotel.hotelservice.dto.request;

import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class UpdateHotelRequest {

    private String name;
    private String address;
    private String description;
    private List<String> amenities;
}
