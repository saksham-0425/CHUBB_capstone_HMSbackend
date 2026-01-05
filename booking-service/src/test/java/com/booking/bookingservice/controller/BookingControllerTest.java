package com.booking.bookingservice.controller;

import com.booking.bookingservice.config.TestSecurityConfig;
import com.booking.bookingservice.dto.request.CreateBookingRequest;
import com.booking.bookingservice.dto.response.BookingResponse;
import com.booking.bookingservice.exception.UnauthorizedException;
import com.booking.bookingservice.model.ReservationStatus;
import com.booking.bookingservice.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(TestSecurityConfig.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBooking_validRequest_shouldReturn201() throws Exception {

        BookingResponse response = BookingResponse.builder()
                .bookingId(1L)
                .bookingReference("BK123")
                .status(ReservationStatus.BOOKED)
                .guestName("John Doe")
                .numberOfGuests(2)
                .numberOfRooms(1)
                .build();

        when(bookingService.createBooking(any(), eq("john@test.com"), eq("GUEST")))
                .thenReturn(response);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setHotelId(1L);
        request.setRoomCategoryId(2L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(2));
        request.setGuestName("John Doe");
        request.setNumberOfGuests(2);
        request.setNumberOfRooms(1);

        mockMvc.perform(post("/bookings")
                .header("X-User-Email", "john@test.com")
                .header("X-User-Role", "GUEST")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingReference").value("BK123"));
    }

    @Test
    void createBooking_invalidRequest_shouldReturn400() throws Exception {

        mockMvc.perform(post("/bookings")
                .header("X-User-Email", "john@test.com")
                .header("X-User-Role", "GUEST")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking_shouldReturn200() throws Exception {

        BookingResponse response = BookingResponse.builder()
                .bookingId(1L)
                .bookingReference("BK123")
                .status(ReservationStatus.BOOKED)
                .build();

        when(bookingService.getBooking(1L, "john@test.com", "GUEST"))
                .thenReturn(response);

        mockMvc.perform(get("/bookings/1")
                .header("X-User-Email", "john@test.com")
                .header("X-User-Role", "GUEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingReference").value("BK123"));
    }

    @Test
    void cancelBooking_shouldReturn204() throws Exception {

        doNothing().when(bookingService)
                .cancelBooking(1L, "john@test.com", "GUEST");

        mockMvc.perform(delete("/bookings/1")
                .header("X-User-Email", "john@test.com")
                .header("X-User-Role", "GUEST"))
                .andExpect(status().isNoContent());
    }

    @Test
    void confirmBooking_shouldReturn200() throws Exception {

        BookingResponse response = BookingResponse.builder()
                .bookingId(1L)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(bookingService.confirmBooking(1L, "MANAGER"))
                .thenReturn(response);

        mockMvc.perform(put("/bookings/1/confirm")
                .header("X-User-Role", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void checkIn_shouldReturn200() throws Exception {

        doNothing().when(bookingService).checkIn(1L, "RECEPTIONIST");

        mockMvc.perform(put("/bookings/1/check-in")
                .header("X-User-Role", "RECEPTIONIST"))
                .andExpect(status().isOk());
    }

    @Test
    void checkOut_shouldReturn200() throws Exception {

        doNothing().when(bookingService).checkOut(1L, "MANAGER");

        mockMvc.perform(put("/bookings/1/check-out")
                .header("X-User-Role", "MANAGER"))
                .andExpect(status().isOk());
    }

    @Test
    void pay_shouldReturn200() throws Exception {

        doNothing().when(bookingService)
                .pay(1L, "john@test.com", "GUEST");

        mockMvc.perform(post("/bookings/1/pay")
                .header("X-User-Email", "john@test.com")
                .header("X-User-Role", "GUEST"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingByReference_shouldReturn200() throws Exception {

        BookingResponse response = BookingResponse.builder()
                .bookingReference("BK123")
                .build();

        when(bookingService.getBookingByReference("BK123", "john@test.com", "GUEST"))
                .thenReturn(response);

        mockMvc.perform(get("/bookings/reference/BK123")
                .header("X-User-Email", "john@test.com")
                .header("X-User-Role", "GUEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingReference").value("BK123"));
    }

    @Test
    void getMyBookings_shouldReturn200() throws Exception {

        when(bookingService.getMyBookings("john@test.com", "GUEST"))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings/my")
                .header("X-User-Email", "john@test.com")
                .header("X-User-Role", "GUEST"))
                .andExpect(status().isOk());
    }

    @Test
    void getManagerBookings_asManager_shouldReturn200() throws Exception {

        when(bookingService.getBookingsForManager("manager@test.com"))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings/manager")
                .header("X-User-Email", "manager@test.com")
                .header("X-User-Role", "MANAGER"))
                .andExpect(status().isOk());
    }

    @Test
    void getManagerBookings_asNonManager_shouldReturn401() throws Exception {

        mockMvc.perform(get("/bookings/manager")
                .header("X-User-Email", "john@test.com")
                .header("X-User-Role", "GUEST"))
                .andExpect(status().isUnauthorized());
    }
}
