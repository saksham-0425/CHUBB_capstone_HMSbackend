package com.hotel.notification.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEventDTO {

    private String eventType;        // BOOKING_CREATED, BOOKING_CONFIRMED, etc.
    private Long bookingId;

    private String guestEmail;
    private String guestName;

    private String hotelName;
    private String roomCategory;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private LocalDateTime eventTime;
}
