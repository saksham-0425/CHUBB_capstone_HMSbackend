package com.booking.bookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.booking.bookingservice.client.ResilientHotelServiceClient;
import com.booking.bookingservice.dto.request.CreateBookingRequest;
import com.booking.bookingservice.dto.response.BookingResponse;
import com.booking.bookingservice.dto.response.RoomCategoryResponseDto;
import com.booking.bookingservice.event.BookingEventPublisher;
import com.booking.bookingservice.exception.RoomNotAvailableException;
import com.booking.bookingservice.exception.UnauthorizedException;
import com.booking.bookingservice.model.PaymentStatus;
import com.booking.bookingservice.model.Reservation;
import com.booking.bookingservice.model.ReservationStatus;
import com.booking.bookingservice.model.StayRecord;
import com.booking.bookingservice.repository.ReservationRepository;
import com.booking.bookingservice.repository.StayRecordRepository;
import com.booking.bookingservice.service.impl.BookingServiceImpl;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResilientHotelServiceClient hotelServiceClient;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private StayRecordRepository stayRecordRepository;

    @Mock
    private BookingEventPublisher bookingEventPublisher;
    
    @Test
    void createBooking_asGuest_whenAvailable_shouldCreateBooking() {

        CreateBookingRequest request = new CreateBookingRequest();
        request.setHotelId(1L);
        request.setRoomCategoryId(2L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));
        request.setGuestName("John");
        request.setNumberOfGuests(2);
        request.setNumberOfRooms(1);

        RoomCategoryResponseDto category = new RoomCategoryResponseDto();
        category.setId(2L);
        category.setCategory("DELUXE");
        category.setCapacity(2);
        category.setBasePrice(BigDecimal.valueOf(3000));

        when(hotelServiceClient.getCategoryById(2L))
                .thenReturn(category);

        when(availabilityService.isAvailable(
                any(), any(), any(), any(), anyInt()))
                .thenReturn(true);

        when(reservationRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response =
                bookingService.createBooking(
                        request, "john@test.com", "GUEST");

        assertEquals(ReservationStatus.BOOKED, response.getStatus());
        verify(availabilityService).reserve(
                any(), any(), any(), any(), anyInt());
    }

    
    @Test
    void createBooking_asAdmin_shouldThrowUnauthorized() {

        CreateBookingRequest request = new CreateBookingRequest();

        assertThrows(UnauthorizedException.class, () ->
                bookingService.createBooking(
                        request, "admin@test.com", "ADMIN")
        );
    }
    
    @Test
    void createBooking_invalidDateRange_shouldThrowException() {

        CreateBookingRequest request = new CreateBookingRequest();
        request.setCheckInDate(LocalDate.now().plusDays(5));
        request.setCheckOutDate(LocalDate.now().plusDays(3));

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(
                        request, "john@test.com", "GUEST")
        );
    }
    
    @Test
    void createBooking_roomNotAvailable_shouldThrowException() {

        CreateBookingRequest request = new CreateBookingRequest();
        request.setHotelId(1L);
        request.setRoomCategoryId(2L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(2));
        request.setNumberOfRooms(1);
        request.setNumberOfGuests(1);

        RoomCategoryResponseDto category = new RoomCategoryResponseDto();
        category.setCapacity(2);
        category.setBasePrice(BigDecimal.valueOf(2000));

        when(hotelServiceClient.getCategoryById(2L))
                .thenReturn(category);

        when(availabilityService.isAvailable(
                any(), any(), any(), any(), anyInt()))
                .thenReturn(false);

        assertThrows(RoomNotAvailableException.class, () ->
                bookingService.createBooking(
                        request, "john@test.com", "GUEST")
        );
    }
    
    @Test
    void confirmBooking_asManager_shouldConfirm() {

        Reservation reservation = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.BOOKED)
                .build();

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        BookingResponse response =
                bookingService.confirmBooking(1L, "MANAGER");

        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
    }
    
    @Test
    void checkIn_whenConfirmed_shouldSucceed() {

        Reservation reservation = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.CONFIRMED)
                .hotelId(1L)
                .roomCategoryId(2L)
                .numberOfRooms(1)
                .build();

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        bookingService.checkIn(1L, "RECEPTIONIST");

        assertEquals(ReservationStatus.CHECKED_IN, reservation.getStatus());
        verify(stayRecordRepository).save(any());
        verify(hotelServiceClient).allocateRooms(any());
    }

    
    @Test
    void checkOut_whenPaid_shouldSucceed() {

        Reservation reservation = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.CHECKED_IN)
                .paymentStatus(PaymentStatus.PAID)
                .build();

        StayRecord stay = StayRecord.builder()
                .reservation(reservation)
                .build();

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(stayRecordRepository.findByReservationId(1L))
                .thenReturn(Optional.of(stay));

        bookingService.checkOut(1L, "MANAGER");

        assertEquals(ReservationStatus.CHECKED_OUT, reservation.getStatus());
        verify(hotelServiceClient).releaseRooms(any());
    }
    
    @Test
    void pay_asGuest_whenConfirmed_shouldMarkPaid() {

        Reservation reservation = Reservation.builder()
                .id(1L)
                .userEmail("john@test.com")
                .status(ReservationStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        bookingService.pay(1L, "john@test.com", "GUEST");

        assertEquals(PaymentStatus.PAID, reservation.getPaymentStatus());
    }
    
    @Test
    void getMyBookings_asGuest_shouldReturnList() {

        Reservation reservation = Reservation.builder()
                .id(1L)
                .userEmail("john@test.com")
                .build();

        when(reservationRepository
                .findByUserEmailOrderByCheckInDateDesc("john@test.com"))
                .thenReturn(List.of(reservation));

        List<BookingResponse> bookings =
                bookingService.getMyBookings("john@test.com", "GUEST");

        assertEquals(1, bookings.size());
    }
    
    
}
