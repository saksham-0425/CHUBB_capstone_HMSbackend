package com.booking.bookingservice.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingEventDTO {

    private String eventType;
    private Long bookingId;

    private String guestEmail;
    private String guestName;

    private String hotelName;
    private String roomCategory;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private LocalDateTime eventTime;
}
