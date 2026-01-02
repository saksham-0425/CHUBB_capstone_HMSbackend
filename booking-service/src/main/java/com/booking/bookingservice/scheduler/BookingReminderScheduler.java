package com.booking.bookingservice.scheduler;

import com.booking.bookingservice.event.BookingEventDTO;
import com.booking.bookingservice.event.BookingEventPublisher;
import com.booking.bookingservice.model.Reservation;
import com.booking.bookingservice.model.ReservationStatus;
import com.booking.bookingservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingReminderScheduler {

    private final ReservationRepository reservationRepository;
    private final BookingEventPublisher bookingEventPublisher;

    
    @Scheduled(cron = "0 41 20 * * *", zone = "Asia/Kolkata") 
    public void sendCheckInReminders() {

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Reservation> bookings =
                reservationRepository
                        .findByStatusAndCheckInDateAndCheckInReminderSentFalse(
                                ReservationStatus.CONFIRMED,
                                tomorrow
                        );

        for (Reservation r : bookings) {

            bookingEventPublisher.publish(
                    "booking.checkin.reminder",
                    BookingEventDTO.builder()
                            .eventType("CHECK_IN_REMINDER")
                            .bookingId(r.getId())
                            .guestEmail(r.getUserEmail())
                            .guestName(r.getUserEmail())
                            .hotelName("HOTEL")      
                            .roomCategory("ROOM")
                            .checkInDate(r.getCheckInDate())
                            .checkOutDate(r.getCheckOutDate())
                            .eventTime(LocalDateTime.now())
                            .build()
            );

            r.setCheckInReminderSent(true);
        }

        reservationRepository.saveAll(bookings);
    }

 
    @Scheduled(cron = "0 55 14 * * *", zone = "Asia/Kolkata")
    public void sendCheckOutReminders() {

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Reservation> bookings =
                reservationRepository
                        .findByStatusAndCheckOutDateAndCheckOutReminderSentFalse(
                                ReservationStatus.CONFIRMED,
                                tomorrow
                        );

        for (Reservation r : bookings) {

            bookingEventPublisher.publish(
                    "booking.checkout.reminder",
                    BookingEventDTO.builder()
                            .eventType("CHECK_OUT_REMINDER")
                            .bookingId(r.getId())
                            .guestEmail(r.getUserEmail())
                            .guestName(r.getUserEmail())
                            .hotelName("HOTEL")
                            .roomCategory("ROOM")
                            .checkInDate(r.getCheckInDate())
                            .checkOutDate(r.getCheckOutDate())
                            .eventTime(LocalDateTime.now())
                            .build()
            );

            r.setCheckOutReminderSent(true);
        }

        reservationRepository.saveAll(bookings);
    }
}
