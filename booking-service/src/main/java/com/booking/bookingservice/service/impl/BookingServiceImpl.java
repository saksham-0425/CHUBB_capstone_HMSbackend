package com.booking.bookingservice.service.impl;

import com.booking.bookingservice.client.HotelServiceClient;
import com.booking.bookingservice.event.BookingEventDTO;
import com.booking.bookingservice.dto.request.CreateBookingRequest;
import com.booking.bookingservice.dto.response.BookingResponse;
import com.booking.bookingservice.dto.response.RoomCategoryResponseDto;
import com.booking.bookingservice.event.BookingEventPublisher;
import com.booking.bookingservice.exception.*;
import com.booking.bookingservice.model.PaymentStatus;
import com.booking.bookingservice.model.Reservation;
import com.booking.bookingservice.model.ReservationStatus;
import com.booking.bookingservice.model.StayRecord;
import com.booking.bookingservice.repository.ReservationRepository;
import com.booking.bookingservice.repository.StayRecordRepository;
import com.booking.bookingservice.service.AvailabilityService;
import com.booking.bookingservice.service.BookingService;
import com.booking.bookingservice.util.BookingReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final ReservationRepository reservationRepository;
    private final HotelServiceClient hotelServiceClient;
    private final AvailabilityService availabilityService;
    private final StayRecordRepository stayRecordRepository;
    private final BookingEventPublisher bookingEventPublisher;


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

        RoomCategoryResponseDto category =
                hotelServiceClient.getCategoryById(
                        request.getRoomCategoryId()
                );
        
        int maxAllowedGuests =
                category.getCapacity() * request.getNumberOfRooms();

        if (request.getNumberOfGuests() > maxAllowedGuests) {
            throw new InvalidGuestCountException(
                    String.format(
                            "Maximum allowed guests for %d %s room(s) is %d",
                            request.getNumberOfRooms(),
                            category.getCategory(),
                            maxAllowedGuests
                    )
            );
        }
        
        boolean available = availabilityService.isAvailable(
                request.getHotelId(),
                request.getRoomCategoryId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getNumberOfRooms()
        );

        if (!available) {
            throw new RoomNotAvailableException("Room not available");
        }

        availabilityService.reserve(
                request.getHotelId(),
                request.getRoomCategoryId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getNumberOfRooms()
        );

        try {

            long nights = ChronoUnit.DAYS.between(
                    request.getCheckInDate(),
                    request.getCheckOutDate()
            );

            BigDecimal totalAmount =
                    category.getBasePrice()
                            .multiply(BigDecimal.valueOf(nights))
                            .multiply(BigDecimal.valueOf(request.getNumberOfRooms()));

            Reservation reservation = Reservation.builder()
                    .bookingReference(BookingReferenceGenerator.generate())
                    .userEmail(userEmail)
                    .guestName(request.getGuestName())
                    .numberOfGuests(request.getNumberOfGuests())
                    .numberOfRooms(request.getNumberOfRooms())
                    .hotelId(request.getHotelId())
                    .roomCategoryId(request.getRoomCategoryId())
                    .checkInDate(request.getCheckInDate())
                    .checkOutDate(request.getCheckOutDate())
                    .pricePerNight(category.getBasePrice())
                    .totalAmount(totalAmount)
                    .status(ReservationStatus.BOOKED)
                    .paymentStatus(PaymentStatus.PENDING)
                    .checkInReminderSent(false)     
                    .checkOutReminderSent(false)
                    .build();

            Reservation saved = reservationRepository.save(reservation);


            try {
                bookingEventPublisher.publish(
                        "booking.created",
                        buildEvent("BOOKING_CREATED", saved, category)
                );
            } catch (Exception e) {
                log.error("Failed to publish BOOKING_CREATED event for bookingId={}",
                        saved.getId(), e);
            }

            return mapToResponse(saved);

        } catch (Exception ex) {

            availabilityService.release(
                    request.getHotelId(),
                    request.getRoomCategoryId(),
                    request.getCheckInDate(),
                    request.getCheckOutDate(),
                    request.getNumberOfRooms()
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
                reservation.getCheckOutDate(),
                reservation.getNumberOfRooms()
        );
        
        try {
            bookingEventPublisher.publish(
                    "booking.cancelled",
                    buildEvent("BOOKING_CANCELLED", reservation, null)
            );
        } catch (Exception e) {
            log.error(
                    "Failed to publish BOOKING_CANCELLED event for bookingId={}",
                    reservation.getId(),
                    e
            );
        }
        
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
        
        try {
            bookingEventPublisher.publish(
                    "booking.confirmed",
                    buildEvent("BOOKING_CONFIRMED", reservation, null)
            );
        } catch (Exception e) {
            log.error("Failed to publish BOOKING_CONFIRMED event for bookingId={}",
                    reservation.getId(), e);
        }


        return mapToResponse(reservation);
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
                .checkInTime(LocalDateTime.now())
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

        if (reservation.getPaymentStatus() != PaymentStatus.PAID) {
            throw new InvalidReservationStateException(
                    "Payment not completed yet"
            );
        }

        StayRecord stayRecord = stayRecordRepository
                .findByReservationId(reservation.getId())
                .orElseThrow(() ->
                        new IllegalStateException("Stay record missing")
                );

        stayRecord.setCheckOutTime(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.CHECKED_OUT);
    }

    @Override
    public void pay(Long bookingId, String userEmail, String role) {

        if (!"GUEST".equals(role)) {
            throw new UnauthorizedException("Only GUEST can make payment");
        }

        Reservation reservation = reservationRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ReservationNotFoundException("Booking not found")
                );

        if (!reservation.getUserEmail().equals(userEmail)) {
            throw new UnauthorizedException("Access denied");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStateException(
                    "Payment allowed only for CONFIRMED bookings"
            );
        }

        if (reservation.getPaymentStatus() == PaymentStatus.PAID) {
            throw new InvalidReservationStateException(
                    "Payment already completed"
            );
        }

        reservation.setPaymentStatus(PaymentStatus.PAID);
    }
   
    @Override
    public BookingResponse getBookingByReference(
            String bookingReference,
            String userEmail,
            String role
    ) {
        Reservation reservation = reservationRepository
                .findByBookingReference(bookingReference)
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
    public List<BookingResponse> getMyBookings(String userEmail, String role) {

        if (!"GUEST".equals(role)) {
            throw new UnauthorizedException("Only GUEST can view their bookings");
        }

        return reservationRepository
                .findByUserEmailOrderByCheckInDateDesc(userEmail)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    
    private BookingEventDTO buildEvent(
            String eventType,
            Reservation r,
            RoomCategoryResponseDto category
    ) {
        return BookingEventDTO.builder()
                .eventType(eventType)
                .bookingId(r.getId())
                .guestEmail(r.getUserEmail())
                .guestName(r.getGuestName())
                .hotelName("HOTEL")
                .roomCategory(
                        category != null ? category.getCategory() : "ROOM"
                )
                .checkInDate(r.getCheckInDate())
                .checkOutDate(r.getCheckOutDate())
                .eventTime(LocalDateTime.now())
                .build();
    }

    private BookingResponse mapToResponse(Reservation r) {
        return BookingResponse.builder()
                .bookingId(r.getId())
                .bookingReference(r.getBookingReference())
                .guestName(r.getGuestName())
                .numberOfGuests(r.getNumberOfGuests())
                .numberOfRooms(r.getNumberOfRooms())
                .status(r.getStatus())
                .hotelId(r.getHotelId())
                .roomCategoryId(r.getRoomCategoryId())
                .checkInDate(r.getCheckInDate())
                .checkOutDate(r.getCheckOutDate())
                .totalAmount(r.getTotalAmount())
                .build();
    }
}
