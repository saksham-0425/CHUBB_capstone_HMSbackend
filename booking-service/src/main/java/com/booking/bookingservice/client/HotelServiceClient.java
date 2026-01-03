package com.booking.bookingservice.client;

import com.booking.bookingservice.config.FeignConfig;
import com.booking.bookingservice.dto.request.AllocateRoomRequest;
import com.booking.bookingservice.dto.request.ReleaseRoomRequest;
import com.booking.bookingservice.dto.response.HotelResponseDto;
import com.booking.bookingservice.dto.response.RoomCategoryResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "hotel-service",
    configuration = FeignConfig.class
)
public interface HotelServiceClient {

    // Validate hotel exists
    @GetMapping("/hotels/{hotelId}")
    HotelResponseDto getHotelById(
            @PathVariable Long hotelId
    );

    // Fetch categories for availability search
    @GetMapping("/hotels/{hotelId}/categories")
    List<RoomCategoryResponseDto> getCategoriesByHotel(
            @PathVariable Long hotelId
    );

    // INTERNAL → Used during booking creation
    @GetMapping("/internal/hotels/categories/{categoryId}")
    RoomCategoryResponseDto getCategoryById(
            @PathVariable Long categoryId
    );
    
    @PostMapping("/internal/room-allocations")
    void allocateRoom(@RequestBody AllocateRoomRequest request);

    @PostMapping("/internal/room-allocations/release")
    void releaseRoom(@RequestBody ReleaseRoomRequest request);
}
