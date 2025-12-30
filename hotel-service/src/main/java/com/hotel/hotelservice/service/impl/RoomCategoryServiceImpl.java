package com.hotel.hotelservice.service.impl;

import com.hotel.hotelservice.dto.request.RoomCategoryRequest;
import com.hotel.hotelservice.dto.response.RoomCategoryResponse;
import com.hotel.hotelservice.entity.Hotel;
import com.hotel.hotelservice.entity.RoomCategory;
import com.hotel.hotelservice.exception.ResourceNotFoundException;
import com.hotel.hotelservice.exception.UnauthorizedException;
import com.hotel.hotelservice.repository.HotelRepository;
import com.hotel.hotelservice.repository.RoomCategoryRepository;
import com.hotel.hotelservice.service.RoomCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomCategoryServiceImpl implements RoomCategoryService {

    private final RoomCategoryRepository roomCategoryRepository;
    private final HotelRepository hotelRepository;

    @Override
    public RoomCategoryResponse addCategory(
            Long hotelId,
            RoomCategoryRequest request,
            String role
    ) {

        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Only ADMIN can add room categories");
        }

        if (roomCategoryRepository.existsByHotelIdAndCategoryIgnoreCase(
                hotelId, request.getCategory())) {
            throw new IllegalArgumentException("Category already exists for this hotel");
        }

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        RoomCategory category = RoomCategory.builder()
                .category(request.getCategory().toUpperCase())
                .totalRooms(request.getTotalRooms())
                .capacity(request.getCapacity())
                .basePrice(request.getBasePrice())
                .hotel(hotel)
                .build();

        RoomCategory saved = roomCategoryRepository.save(category);

        return mapToResponse(saved);
    }

    @Override
    public RoomCategoryResponse updateCategory(
            Long categoryId,
            RoomCategoryRequest request,
            String userEmail,
            String role
    ) {

        RoomCategory category = roomCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Hotel hotel = category.getHotel();

        if ("MANAGER".equals(role) && !hotel.getManagerEmail().equals(userEmail)) {
            throw new UnauthorizedException("Manager not assigned to this hotel");
        }

        if (!("ADMIN".equals(role) || "MANAGER".equals(role))) {
            throw new UnauthorizedException("Access denied");
        }

        category.setTotalRooms(request.getTotalRooms());
        category.setCapacity(request.getCapacity());
        category.setBasePrice(request.getBasePrice());

        return mapToResponse(category);
    }

    @Override
    public List<RoomCategoryResponse> getCategoriesByHotel(Long hotelId) {
        return roomCategoryRepository.findByHotelId(hotelId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RoomCategoryResponse mapToResponse(RoomCategory category) {
        return RoomCategoryResponse.builder()
                .id(category.getId())
                .category(category.getCategory())
                .totalRooms(category.getTotalRooms())
                .capacity(category.getCapacity())
                .basePrice(category.getBasePrice())
                .build();
    }
}
