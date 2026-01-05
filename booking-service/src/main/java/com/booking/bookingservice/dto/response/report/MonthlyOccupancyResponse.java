package com.booking.bookingservice.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyOccupancyResponse {
    private String month;
    private Long occupiedBookings;
}
