package com.booking.bookingservice.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RevenueReportResponse {

    private Long hotelId;
    private BigDecimal revenue;
}
