package com.hotel.notification.service;

import com.hotel.notification.dto.BookingEventDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EmailNotificationService emailNotificationService;

    @Override
    public void processEvent(BookingEventDTO event) {

        log.info(
            "Processing notification for eventType={}, bookingId={}",
            event.getEventType(),
            event.getBookingId()
        );

        switch (event.getEventType()) {

            case "BOOKING_CREATED":
                emailNotificationService.sendBookingCreated(event);
                break;

            case "BOOKING_CONFIRMED":
                emailNotificationService.sendBookingConfirmed(event);
                break;

            case "BOOKING_CANCELLED":
                emailNotificationService.sendBookingCancelled(event);
                break;

            case "CHECK_IN_REMINDER":
                emailNotificationService.sendCheckInReminder(event);
                break;

            case "CHECK_OUT_REMINDER":
                emailNotificationService.sendCheckOutReminder(event);
                break;

            default:
                log.warn(
                    "Unknown booking event type received: {}",
                    event.getEventType()
                );
        }
    }
}
