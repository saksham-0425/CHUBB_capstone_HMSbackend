package com.booking.bookingservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.Min;

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
    
    @NotBlank
    private String guestName;

    @NotNull
    @Min(1)
    private Integer numberOfGuests;

    @NotNull
    @Min(1)
    private Integer numberOfRooms;
}
