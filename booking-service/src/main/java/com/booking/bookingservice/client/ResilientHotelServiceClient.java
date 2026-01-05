package com.booking.bookingservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.booking.bookingservice.dto.request.AllocateRoomRequest;
import com.booking.bookingservice.dto.request.ReleaseRoomRequest;
import com.booking.bookingservice.dto.response.HotelResponseDto;
import com.booking.bookingservice.dto.response.RoomCategoryResponseDto;
import com.booking.bookingservice.exception.ServiceUnavailableException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResilientHotelServiceClient {

    private final HotelServiceClient hotelServiceClient;

    @CircuitBreaker(name = "hotelService", fallbackMethod = "categoryFallback")
    public RoomCategoryResponseDto getCategoryById(Long categoryId) {
        return hotelServiceClient.getCategoryById(categoryId);
    }

    @CircuitBreaker(name = "hotelService", fallbackMethod = "hotelFallback")
    public HotelResponseDto getHotelByManager(String email, String role) {
        return hotelServiceClient.getHotelByManager(email, role);
    }

    @CircuitBreaker(name = "hotelService", fallbackMethod = "voidFallback")
    public void allocateRooms(AllocateRoomRequest request) {
        hotelServiceClient.allocateRooms(request);
    }

    @CircuitBreaker(name = "hotelService", fallbackMethod = "voidFallback")
    public void releaseRooms(ReleaseRoomRequest request) {
        hotelServiceClient.releaseRooms(request);
    }


    private RoomCategoryResponseDto categoryFallback(
            Long categoryId, Throwable ex) {
        throw new ServiceUnavailableException(
            "Hotel service unavailable while fetching room category"
        );
    }

    private HotelResponseDto hotelFallback(
            String email, String role, Throwable ex) {
        throw new ServiceUnavailableException(
            "Hotel service unavailable for manager operations"
        );
    }

    private void voidFallback(Object req, Throwable ex) {
        throw new ServiceUnavailableException(
            "Hotel service temporarily unavailable"
        );
    }
}
