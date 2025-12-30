package com.hotel.hotelservice.service.impl;

import com.hotel.hotelservice.dto.request.CreateHotelRequest;
import com.hotel.hotelservice.dto.request.UpdateHotelRequest;
import com.hotel.hotelservice.dto.response.HotelResponse;
import com.hotel.hotelservice.dto.response.RoomCategoryResponse;
import com.hotel.hotelservice.entity.Hotel;
import com.hotel.hotelservice.entity.RoomCategory;
import com.hotel.hotelservice.exception.ResourceNotFoundException;
import com.hotel.hotelservice.exception.UnauthorizedException;
import com.hotel.hotelservice.repository.HotelRepository;
import com.hotel.hotelservice.repository.RoomCategoryRepository;
import com.hotel.hotelservice.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final RoomCategoryRepository roomCategoryRepository;

    @Override
    public HotelResponse createHotel(CreateHotelRequest request, String role) {

        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Only ADMIN can create hotels");
        }

        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .city(request.getCity())
                .address(request.getAddress())
                .description(request.getDescription())
                .managerEmail(request.getManagerEmail())
                .amenities(String.join(",", request.getAmenities()))
                .build();

        List<RoomCategory> categories = request.getRoomCategories()
                .stream()
                .map(rc -> RoomCategory.builder()
                        .category(rc.getCategory().toUpperCase())
                        .totalRooms(rc.getTotalRooms())
                        .capacity(rc.getCapacity())
                        .basePrice(rc.getBasePrice())
                        .hotel(hotel)
                        .build())
                .collect(Collectors.toList());

        hotel.setRoomCategories(categories);

        Hotel savedHotel = hotelRepository.save(hotel);

        return mapToHotelResponse(savedHotel);
    }

    @Override
    public HotelResponse updateHotel(
            Long hotelId,
            UpdateHotelRequest request,
            String userEmail,
            String role
    ) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if ("MANAGER".equals(role) && !hotel.getManagerEmail().equals(userEmail)) {
            throw new UnauthorizedException("Manager not assigned to this hotel");
        }

        if (!("ADMIN".equals(role) || "MANAGER".equals(role))) {
            throw new UnauthorizedException("Access denied");
        }

        if (request.getName() != null)
            hotel.setName(request.getName());

        if (request.getAddress() != null)
            hotel.setAddress(request.getAddress());

        if (request.getDescription() != null)
            hotel.setDescription(request.getDescription());

        if (request.getAmenities() != null)
            hotel.setAmenities(String.join(",", request.getAmenities()));

        return mapToHotelResponse(hotel);
    }

    @Override
    public List<HotelResponse> getAllHotels() {
        return hotelRepository.findAll()
                .stream()
                .map(this::mapToHotelResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<HotelResponse> searchHotelsByCity(String city) {
        return hotelRepository.findByCityIgnoreCase(city)
                .stream()
                .map(this::mapToHotelResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HotelResponse getHotelById(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
        return mapToHotelResponse(hotel);
    }

    private HotelResponse mapToHotelResponse(Hotel hotel) {
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .city(hotel.getCity())
                .address(hotel.getAddress())
                .description(hotel.getDescription())
                .managerEmail(hotel.getManagerEmail())
                .amenities(List.of(hotel.getAmenities().split(",")))
                .roomCategories(
                        hotel.getRoomCategories()
                                .stream()
                                .map(rc -> RoomCategoryResponse.builder()
                                        .id(rc.getId())
                                        .category(rc.getCategory())
                                        .totalRooms(rc.getTotalRooms())
                                        .capacity(rc.getCapacity())
                                        .basePrice(rc.getBasePrice())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }
}
