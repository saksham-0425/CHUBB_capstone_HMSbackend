package com.booking.bookingservice.service;

import com.booking.bookingservice.dto.request.CreateBookingRequest;
import com.booking.bookingservice.dto.response.BookingResponse;
import java.util.List;
public interface BookingService {

    BookingResponse createBooking(
            CreateBookingRequest request,
            String userEmail,
            String role
    );

    BookingResponse getBooking(
            Long bookingId,
            String userEmail,
            String role
    );

    void cancelBooking(
            Long bookingId,
            String userEmail,
            String role
    );

    BookingResponse confirmBooking(
            Long bookingId,
            String role
    );
    
    void checkIn(Long bookingId, String role);

    void checkOut(Long bookingId, String role);
    
    void pay(Long bookingId, String userEmail, String role);
    
    BookingResponse getBookingByReference(
            String bookingReference,
            String userEmail,
            String role
    );
    
    List<BookingResponse> getMyBookings(String userEmail, String role);
}
