package com.booking.bookingservice.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.booking.bookingservice.dto.response.HotelAvailabilityResponse;
import com.booking.bookingservice.service.AvailabilityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelAvailabilityController {

    private final AvailabilityService availabilityService;

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
}
