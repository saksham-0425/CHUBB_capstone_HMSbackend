package com.booking.bookingservice.controller;

import com.booking.bookingservice.dto.request.CreateBookingRequest;
import com.booking.bookingservice.dto.response.BookingResponse;
import com.booking.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // GUEST → Create booking
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, email, role));
    }

    // GUEST / ADMIN → View booking
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role
    ) {
        return ResponseEntity.ok(
                bookingService.getBooking(id, email, role)
        );
    }

    // GUEST / ADMIN → Cancel booking
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role
    ) {
        bookingService.cancelBooking(id, email, role);
        return ResponseEntity.noContent().build();
    }

    // MANAGER / ADMIN → Confirm booking
    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role
    ) {
        return ResponseEntity.ok(
                bookingService.confirmBooking(id, role)
        );
    }
    
    @PutMapping("/{id}/check-in")
    public ResponseEntity<Void> checkIn(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role
    ) {
        bookingService.checkIn(id, role);
        return ResponseEntity.ok().build();
    }

    // RECEPTIONIST / MANAGER / ADMIN → Check-out
    @PutMapping("/{id}/check-out")
    public ResponseEntity<Void> checkOut(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role
    ) {
        bookingService.checkOut(id, role);
        return ResponseEntity.ok().build();
    }
}
