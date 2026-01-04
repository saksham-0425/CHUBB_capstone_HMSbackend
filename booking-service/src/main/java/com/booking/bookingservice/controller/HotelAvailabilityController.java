package com.booking.bookingservice.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.booking.bookingservice.dto.response.BookingResponse;
import com.booking.bookingservice.dto.response.HotelAvailabilityResponse;
import com.booking.bookingservice.service.AvailabilityService;
import com.booking.bookingservice.service.BookingService;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelAvailabilityController {

    private final AvailabilityService availabilityService;
    private final BookingService bookingService;

    // PUBLIC (GUEST)
    @GetMapping("/{hotelId}/availability")
    public ResponseEntity<HotelAvailabilityResponse> getAvailability(
            @PathVariable Long hotelId,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut
    ) {
        return ResponseEntity.ok(
                availabilityService.getHotelAvailability(
                        hotelId, checkIn, checkOut
                )
        );
    }
    
    @GetMapping("/{hotelId}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsByHotel(
            @PathVariable Long hotelId,
            @RequestHeader("X-User-Role") String role
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByHotel(hotelId, role)
        );
    }
}
