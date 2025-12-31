package com.hotel.hotelservice.service;

import com.hotel.hotelservice.dto.request.CreateReceptionistRequest;

public interface HotelStaffService {

    void addReceptionist(
            Long hotelId,
            String managerEmail,
            String role,
            CreateReceptionistRequest request
    );
}
