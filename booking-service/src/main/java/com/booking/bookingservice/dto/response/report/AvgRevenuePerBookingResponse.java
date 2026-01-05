package com.booking.bookingservice.dto.response.report;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvgRevenuePerBookingResponse {
    private Long hotelId;
    private BigDecimal averageRevenue;
}
