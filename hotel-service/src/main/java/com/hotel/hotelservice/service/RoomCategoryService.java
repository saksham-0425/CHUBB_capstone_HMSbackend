package com.hotel.hotelservice.service;

import com.hotel.hotelservice.dto.request.RoomCategoryRequest;
import com.hotel.hotelservice.dto.response.RoomCategoryResponse;

import java.util.List;

public interface RoomCategoryService {

    RoomCategoryResponse addCategory(
            Long hotelId,
            RoomCategoryRequest request,
            String role
    );

    RoomCategoryResponse updateCategory(
            Long categoryId,
            RoomCategoryRequest request,
            String userEmail,
            String role
    );

    List<RoomCategoryResponse> getCategoriesByHotel(Long hotelId);
    RoomCategoryResponse getCategoryById(Long categoryId);
}
