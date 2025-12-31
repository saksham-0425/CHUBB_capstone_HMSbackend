package com.booking.bookingservice.dto.response;

import com.booking.bookingservice.model.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class BookingResponse {

    private Long bookingId;
    private String bookingReference;
    private ReservationStatus status;

    private Long hotelId;
    private Long roomCategoryId;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private BigDecimal totalAmount;
}
