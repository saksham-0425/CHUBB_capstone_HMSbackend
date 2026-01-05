package com.booking.bookingservice.controller.report;

import com.booking.bookingservice.dto.response.report.AdminMonthlyRevenueResponse;
import com.booking.bookingservice.dto.response.report.AvgRevenuePerBookingResponse;
import com.booking.bookingservice.dto.response.report.MonthlyOccupancyResponse;
import com.booking.bookingservice.dto.response.report.MonthlyRevenueReportResponse;
import com.booking.bookingservice.dto.response.report.OccupancyReportResponse;
import com.booking.bookingservice.dto.response.report.RevenueReportResponse;
import com.booking.bookingservice.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/occupancy")
    public ResponseEntity<List<OccupancyReportResponse>> getOccupancyReport(
            @RequestParam Long hotelId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email
    ) {
    	
    	return ResponseEntity.ok(
                reportService.getOccupancyReport(
                        hotelId, from, to, role, email
                )
        );
    }
    
    @GetMapping("/revenue")
    public ResponseEntity<RevenueReportResponse> getRevenueReport(
            @RequestParam Long hotelId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email
    ) {
        return ResponseEntity.ok(
                reportService.getRevenueReport(
                        hotelId, from, to, role, email
                )
        );
    }
    
    @GetMapping("/revenue/monthly")
    public ResponseEntity<List<MonthlyRevenueReportResponse>> getMonthlyRevenue(
            @RequestParam Long hotelId,
            @RequestParam int year,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email
    ) {
        return ResponseEntity.ok(
                reportService.getMonthlyRevenueReport(
                        hotelId, year, role, email
                )
        );
    }
    
    @GetMapping("/revenue/monthly/all-hotels")
    public ResponseEntity<List<AdminMonthlyRevenueResponse>> getAdminMonthlyRevenue(
            @RequestParam int year,
            @RequestHeader("X-User-Role") String role
    ) {
        return ResponseEntity.ok(
                reportService.getAdminMonthlyRevenue(year, role)
        );
    }
    
    @GetMapping("/revenue/average")
    public ResponseEntity<AvgRevenuePerBookingResponse> getAverageRevenue(
            @RequestParam Long hotelId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email
    ) {
        return ResponseEntity.ok(
                reportService.getAverageRevenue(hotelId, role, email)
        );
    }
    
    @GetMapping("/occupancy/monthly")
    public ResponseEntity<List<MonthlyOccupancyResponse>> getMonthlyOccupancy(
            @RequestParam Long hotelId,
            @RequestParam int year,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email
    ) {
        return ResponseEntity.ok(
                reportService.getMonthlyOccupancy(hotelId, year, role, email)
        );
    }
}
