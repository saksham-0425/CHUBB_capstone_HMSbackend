package com.booking.bookingservice.controller;

import com.booking.bookingservice.config.TestSecurityConfig;
import com.booking.bookingservice.dto.response.BookingResponse;
import com.booking.bookingservice.dto.response.CategoryAvailabilityResponse;
import com.booking.bookingservice.dto.response.HotelAvailabilityResponse;
import com.booking.bookingservice.model.ReservationStatus;
import com.booking.bookingservice.service.AvailabilityService;
import com.booking.bookingservice.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HotelAvailabilityController.class)
@Import(TestSecurityConfig.class)
class HotelAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AvailabilityService availabilityService;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAvailability_shouldReturn200() throws Exception {

        CategoryAvailabilityResponse category =
                new CategoryAvailabilityResponse(
                        1L,
                        "DELUXE",
                        5,
                        BigDecimal.valueOf(3500)
                );

        HotelAvailabilityResponse response =
                new HotelAvailabilityResponse(
                        1L,
                        LocalDate.of(2026, 1, 10),
                        LocalDate.of(2026, 1, 12),
                        List.of(category)
                );

        when(availabilityService.getHotelAvailability(
                1L,
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 12)
        )).thenReturn(response);

        mockMvc.perform(get("/hotels/1/availability")
                .param("checkIn", "2026-01-10")
                .param("checkOut", "2026-01-12")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotelId").value(1))
                .andExpect(jsonPath("$.categories[0].categoryName")
                        .value("DELUXE"));
    }

    @Test
    void getBookingsByHotel_asManager_shouldReturn200() throws Exception {

        BookingResponse booking =
                BookingResponse.builder()
                        .bookingId(1L)
                        .bookingReference("BK123")
                        .status(ReservationStatus.BOOKED)
                        .guestName("John Doe")
                        .numberOfRooms(1)
                        .build();

        when(bookingService.getBookingsByHotel(1L, "MANAGER"))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/hotels/1/bookings")
                .header("X-User-Role", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingReference")
                        .value("BK123"));
    }

    @Test
    void getBookingsByHotel_asAdmin_shouldReturn200() throws Exception {

        when(bookingService.getBookingsByHotel(1L, "ADMIN"))
                .thenReturn(List.of());

        mockMvc.perform(get("/hotels/1/bookings")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }
}
