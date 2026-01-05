package com.booking.bookingservice.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OccupancyReportResponse {

    private String status;
    private Long count;
}
