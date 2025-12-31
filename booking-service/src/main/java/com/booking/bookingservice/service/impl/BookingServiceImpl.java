package com.booking.bookingservice.service.impl;

import com.booking.bookingservice.client.HotelServiceClient;
import com.booking.bookingservice.dto.request.CreateBookingRequest;
import com.booking.bookingservice.dto.response.BookingResponse;
import com.booking.bookingservice.dto.response.RoomCategoryResponseDto;
import com.booking.bookingservice.exception.*;
import com.booking.bookingservice.model.Reservation;
import com.booking.bookingservice.model.ReservationStatus;
import com.booking.bookingservice.model.StayRecord;
import com.booking.bookingservice.repository.ReservationRepository;
import com.booking.bookingservice.repository.StayRecordRepository;
import com.booking.bookingservice.service.AvailabilityService;
import com.booking.bookingservice.service.BookingService;
import com.booking.bookingservice.util.BookingReferenceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final ReservationRepository reservationRepository;
    private final HotelServiceClient hotelServiceClient;
    private final AvailabilityService availabilityService;
    private final StayRecordRepository stayRecordRepository;

    @Override
    public BookingResponse createBooking(
            CreateBookingRequest request,
            String userEmail,
            String role
    ) {

        if (!"GUEST".equals(role)) {
            throw new UnauthorizedException("Only GUEST can create bookings");
        }

        if (!request.getCheckInDate().isBefore(request.getCheckOutDate())) {
            throw new IllegalArgumentException("Invalid date range");
        }

        // Validate hotel & category
        RoomCategoryResponseDto category =
                hotelServiceClient.getCategoryById(
                        request.getRoomCategoryId()
                );

        // Availability check
        boolean available = availabilityService.isAvailable(
                request.getHotelId(),
                request.getRoomCategoryId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        if (!available) {
            throw new RoomNotAvailableException("Room not available");
        }

        // Reserve first (atomic)
        availabilityService.reserve(
                request.getHotelId(),
                request.getRoomCategoryId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        try {

            long nights = ChronoUnit.DAYS.between(
                    request.getCheckInDate(),
                    request.getCheckOutDate()
            );

            BigDecimal totalAmount =
                    category.getBasePrice()
                            .multiply(BigDecimal.valueOf(nights));

            Reservation reservation = Reservation.builder()
                    .bookingReference(BookingReferenceGenerator.generate())
                    .userEmail(userEmail)
                    .hotelId(request.getHotelId())
                    .roomCategoryId(request.getRoomCategoryId())
                    .checkInDate(request.getCheckInDate())
                    .checkOutDate(request.getCheckOutDate())
                    .pricePerNight(category.getBasePrice())
                    .totalAmount(totalAmount)
                    .status(ReservationStatus.BOOKED)
                    .build();

            Reservation saved = reservationRepository.save(reservation);

            return mapToResponse(saved);

        } catch (Exception ex) {

            // rollback Redis reservation
            availabilityService.release(
                    request.getHotelId(),
                    request.getRoomCategoryId(),
                    request.getCheckInDate(),
                    request.getCheckOutDate()
            );
            throw ex;
        }
    }

    @Override
    public BookingResponse getBooking(
            Long bookingId,
            String userEmail,
            String role
    ) {

        Reservation reservation = reservationRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ReservationNotFoundException("Booking not found")
                );

        if ("GUEST".equals(role)
                && !reservation.getUserEmail().equals(userEmail)) {
            throw new UnauthorizedException("Access denied");
        }

        return mapToResponse(reservation);
    }

    @Override
    public void cancelBooking(
            Long bookingId,
            String userEmail,
            String role
    ) {

        Reservation reservation = reservationRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ReservationNotFoundException("Booking not found")
                );

        if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
            throw new InvalidReservationStateException(
                    "Cannot cancel after check-in"
            );
        }

        if ("GUEST".equals(role)
                && !reservation.getUserEmail().equals(userEmail)) {
            throw new UnauthorizedException("Access denied");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        availabilityService.release(
                reservation.getHotelId(),
                reservation.getRoomCategoryId(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );
    }

    @Override
    public BookingResponse confirmBooking(
            Long bookingId,
            String role
    ) {

        if (!("ADMIN".equals(role) || "MANAGER".equals(role))) {
            throw new UnauthorizedException("Access denied");
        }

        Reservation reservation = reservationRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ReservationNotFoundException("Booking not found")
                );

        reservation.setStatus(ReservationStatus.CONFIRMED);

        return mapToResponse(reservation);
    }

    private BookingResponse mapToResponse(Reservation r) {
        return BookingResponse.builder()
                .bookingId(r.getId())
                .bookingReference(r.getBookingReference())
                .status(r.getStatus())
                .hotelId(r.getHotelId())
                .roomCategoryId(r.getRoomCategoryId())
                .checkInDate(r.getCheckInDate())
                .checkOutDate(r.getCheckOutDate())
                .totalAmount(r.getTotalAmount())
                .build();
    }
    
    @Override
    public void checkIn(Long bookingId, String role) {

        if (!("RECEPTIONIST".equals(role)
                || "MANAGER".equals(role)
                || "ADMIN".equals(role))) {
            throw new UnauthorizedException("Access denied");
        }

        Reservation reservation = reservationRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ReservationNotFoundException("Booking not found")
                );

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStateException(
                    "Only CONFIRMED bookings can be checked in"
            );
        }

        reservation.setStatus(ReservationStatus.CHECKED_IN);

        StayRecord stayRecord = StayRecord.builder()
                .reservation(reservation)
                .checkInTime(java.time.LocalDateTime.now())
                .build();

        stayRecordRepository.save(stayRecord);
    }
    
    @Override
    public void checkOut(Long bookingId, String role) {

        if (!("RECEPTIONIST".equals(role)
                || "MANAGER".equals(role)
                || "ADMIN".equals(role))) {
            throw new UnauthorizedException("Access denied");
        }

        Reservation reservation = reservationRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ReservationNotFoundException("Booking not found")
                );

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new InvalidReservationStateException(
                    "Only CHECKED_IN bookings can be checked out"
            );
        }

        StayRecord stayRecord = stayRecordRepository
                .findByReservationId(reservation.getId())
                .orElseThrow(() ->
                        new IllegalStateException("Stay record missing")
                );

        stayRecord.setCheckOutTime(java.time.LocalDateTime.now());
        reservation.setStatus(ReservationStatus.CHECKED_OUT);
    }

}
