package com.hotel.hotelservice.service;

import com.hotel.hotelservice.dto.request.CreateHotelRequest;
import com.hotel.hotelservice.dto.request.UpdateHotelRequest;
import com.hotel.hotelservice.dto.response.HotelResponse;
import com.hotel.hotelservice.dto.response.RoomResponse;

import java.util.List;

public interface HotelService {

    HotelResponse createHotel(CreateHotelRequest request, String role);

    HotelResponse updateHotel(
            Long hotelId,
            UpdateHotelRequest request,
            String userEmail,
            String role
    );

    List<HotelResponse> getAllHotels();

    List<HotelResponse> searchHotelsByCity(String city);

    HotelResponse getHotelById(Long hotelId);
    
    HotelResponse getHotelByManagerEmail(String email);
    HotelResponse getHotelByReceptionistEmail(String email);

}
