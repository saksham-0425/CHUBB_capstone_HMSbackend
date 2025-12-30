package com.hotel.hotelservice.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateHotelRequest {

    private String name;
    private String address;
    private String description;
    private List<String> amenities;
}
