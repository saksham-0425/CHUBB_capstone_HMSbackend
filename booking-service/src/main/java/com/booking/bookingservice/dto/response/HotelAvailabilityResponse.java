package com.booking.bookingservice.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HotelAvailabilityResponse {

    private Long hotelId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private List<CategoryAvailabilityResponse> categories;
}
