package com.booking.bookingservice.service.report;

import com.booking.bookingservice.dto.response.report.AdminMonthlyRevenueResponse;
import com.booking.bookingservice.dto.response.report.AvgRevenuePerBookingResponse;
import com.booking.bookingservice.dto.response.report.MonthlyOccupancyResponse;
import com.booking.bookingservice.dto.response.report.MonthlyRevenueReportResponse;
import com.booking.bookingservice.dto.response.report.OccupancyReportResponse;
import com.booking.bookingservice.dto.response.report.RevenueReportResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    List<OccupancyReportResponse> getOccupancyReport(
            Long hotelId,
            LocalDate from,
            LocalDate to,
            String role,
            String userEmail
    );
    
    RevenueReportResponse getRevenueReport(
            Long hotelId,
            LocalDate from,
            LocalDate to,
            String role,
            String userEmail
    );
       
    List<MonthlyRevenueReportResponse> getMonthlyRevenueReport(
            Long hotelId,
            int year,
            String role,
            String userEmail
    );

	List<AdminMonthlyRevenueResponse> getAdminMonthlyRevenue(int year, String role);

	AvgRevenuePerBookingResponse getAverageRevenue(Long hotelId, String role, String userEmail);

	List<MonthlyOccupancyResponse> getMonthlyOccupancy(Long hotelId, int year, String role, String userEmail);

}
