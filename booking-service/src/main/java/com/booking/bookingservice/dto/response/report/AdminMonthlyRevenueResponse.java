package com.booking.bookingservice.dto.response.report;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminMonthlyRevenueResponse {
    private Long hotelId;
    private String month;
    private BigDecimal revenue;
}