package com.booking.bookingservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateBookingRequest {

    @NotNull
    private Long hotelId;

    @NotNull
    private Long roomCategoryId;

    @NotNull
    @Future
    private LocalDate checkInDate;

    @NotNull
    @Future
    private LocalDate checkOutDate;
}
