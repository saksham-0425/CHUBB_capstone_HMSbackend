package com.booking.bookingservice.service.impl.report;

import com.booking.bookingservice.client.HotelServiceClient;
import com.booking.bookingservice.dto.response.HotelResponseDto;
import com.booking.bookingservice.dto.response.report.AdminMonthlyRevenueResponse;
import com.booking.bookingservice.dto.response.report.AvgRevenuePerBookingResponse;
import com.booking.bookingservice.dto.response.report.MonthlyOccupancyResponse;
import com.booking.bookingservice.dto.response.report.MonthlyRevenueReportResponse;
import com.booking.bookingservice.dto.response.report.OccupancyReportResponse;
import com.booking.bookingservice.dto.response.report.RevenueReportResponse;
import com.booking.bookingservice.exception.UnauthorizedException;
import com.booking.bookingservice.repository.report.BookingReportRepository;
import com.booking.bookingservice.service.report.ReportService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BookingReportRepository bookingReportRepository;
    private final HotelServiceClient hotelServiceClient;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<OccupancyReportResponse> getOccupancyReport(
            Long hotelId,
            LocalDate from,
            LocalDate to,
            String role,
            String userEmail
    ) {

        if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new RuntimeException("Access denied for reports");
        }
        
        validateManagerHotelAccess(role, userEmail, hotelId);

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        List<Object[]> results =
                bookingReportRepository.getOccupancyByStatus(hotelId, from, to);

        return results.stream()
                .map(r -> new OccupancyReportResponse(
                        String.valueOf(r[0]),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }
    
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(
            Long hotelId,
            LocalDate from,
            LocalDate to,
            String role,
            String userEmail
    ) {

        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new UnauthorizedException("Access denied for revenue reports");
        }
        
        validateManagerHotelAccess(role, userEmail, hotelId);

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        List<Object[]> result =
                bookingReportRepository.getRevenueByHotel(hotelId, from, to);

        if (result.isEmpty()) {
            return new RevenueReportResponse(hotelId, BigDecimal.ZERO);
        }

        Object[] row = result.get(0);

        return new RevenueReportResponse(
                (Long) row[0],
                (BigDecimal) row[1]
        );
    }
   
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<MonthlyRevenueReportResponse> getMonthlyRevenueReport(
            Long hotelId,
            int year,
            String role,
            String userEmail
    ) {

        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new UnauthorizedException("Access denied for monthly revenue report");
        }
        
        validateManagerHotelAccess(role, userEmail, hotelId);

        if (year < 2000 || year > LocalDate.now().getYear()) {
            throw new IllegalArgumentException("Invalid year");
        }

        return bookingReportRepository.getMonthlyRevenue(hotelId, year)
                .stream()
                .map(row -> {
                    int monthNumber = ((Number) row[0]).intValue();
                    BigDecimal revenue = (BigDecimal) row[1];

                    return new MonthlyRevenueReportResponse(
                            Month.of(monthNumber).name(),
                            revenue
                    );
                })
                .toList();
    }
   
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AdminMonthlyRevenueResponse> getAdminMonthlyRevenue(
            int year, String role) {

        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Admin access required");
        }

        return bookingReportRepository.getMonthlyRevenueForAllHotels(year)
                .stream()
                .map(r -> new AdminMonthlyRevenueResponse(
                        (Long) r[0],
                        Month.of(((Number) r[1]).intValue()).name(),
                        (BigDecimal) r[2]
                ))
                .toList();
    }
    
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public AvgRevenuePerBookingResponse getAverageRevenue(
            Long hotelId, String role, String userEmail) {

        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new UnauthorizedException("Access denied");
        }
        
        validateManagerHotelAccess(role, userEmail, hotelId);

        List<Object[]> result =
                bookingReportRepository.getAverageRevenuePerBooking(hotelId);

        if (result.isEmpty()) {
            return new AvgRevenuePerBookingResponse(hotelId, BigDecimal.ZERO);
        }

        Object[] row = result.get(0);

        BigDecimal avgRevenue =
                BigDecimal.valueOf(((Number) row[1]).doubleValue());

        return new AvgRevenuePerBookingResponse(
                (Long) row[0],
                avgRevenue
        );
    }
    
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<MonthlyOccupancyResponse> getMonthlyOccupancy(
            Long hotelId, int year, String role, String userEmail) {

        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new UnauthorizedException("Access denied");
        }
        
        validateManagerHotelAccess(role, userEmail, hotelId);

        return bookingReportRepository.getMonthlyOccupancy(hotelId, year)
                .stream()
                .map(r -> new MonthlyOccupancyResponse(
                        Month.of(((Number) r[0]).intValue()).name(),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }
    
    private void validateManagerHotelAccess(
            String role,
            String userEmail,
            Long requestedHotelId
    ) {
        if ("MANAGER".equals(role)) {

            HotelResponseDto hotel =
                    hotelServiceClient.getHotelByManager(userEmail, role);

            if (!hotel.getId().equals(requestedHotelId)) {
                throw new UnauthorizedException(
                    "Manager cannot access reports for another hotel"
                );
            }
        }
    }

    
}
