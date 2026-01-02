package com.hotel.notification.service;

import com.hotel.notification.dto.BookingEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;

    private void sendEmail(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();
        

        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        log.info("Email sent successfully to {}", to);
    }

    public void sendBookingCreated(BookingEventDTO event) {
        sendEmail(
            event.getGuestEmail(),
            "Booking Created",
            String.format(
                "Hi %s,\n\nYour booking (ID: %d) at %s has been created.\n\n" +
                "Room: %s\nCheck-in: %s\nCheck-out: %s",
                event.getGuestName(),
                event.getBookingId(),
                event.getHotelName(),
                event.getRoomCategory(),
                event.getCheckInDate(),
                event.getCheckOutDate()
            )
        );
    }

    public void sendBookingConfirmed(BookingEventDTO event) {
        sendEmail(
            event.getGuestEmail(),
            "Booking Confirmed",
            String.format(
                "Hi %s,\n\nYour booking at %s is CONFIRMED.\n\n" +
                "Check-in: %s\nCheck-out: %s\n\nWe look forward to hosting you!",
                event.getGuestName(),
                event.getHotelName(),
                event.getCheckInDate(),
                event.getCheckOutDate()
            )
        );
    }

    public void sendBookingCancelled(BookingEventDTO event) {
        sendEmail(
            event.getGuestEmail(),
            "Booking Cancelled",
            String.format(
                "Hi %s,\n\nYour booking (ID: %d) at %s has been cancelled.",
                event.getGuestName(),
                event.getBookingId(),
                event.getHotelName()
            )
        );
    }

    public void sendCheckInReminder(BookingEventDTO event) {
        sendEmail(
            event.getGuestEmail(),
            "Check-in Reminder",
            String.format(
                "Hi %s,\n\nThis is a reminder that your check-in at %s is on %s.",
                event.getGuestName(),
                event.getHotelName(),
                event.getCheckInDate()
            )
        );
    }

    public void sendCheckOutReminder(BookingEventDTO event) {
        sendEmail(
            event.getGuestEmail(),
            "Check-out Reminder",
            String.format(
                "Hi %s,\n\nThis is a reminder that your check-out from %s is on %s.",
                event.getGuestName(),
                event.getHotelName(),
                event.getCheckOutDate()
            )
        );
    }
}
