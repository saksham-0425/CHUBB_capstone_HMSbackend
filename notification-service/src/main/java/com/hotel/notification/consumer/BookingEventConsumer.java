package com.hotel.notification.consumer;

import com.hotel.notification.dto.BookingEventDTO;
import com.hotel.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "notification.queue")
    public void handleBookingEvent(BookingEventDTO event) {

        log.info(
            "Received booking event: type={}, bookingId={}",
            event.getEventType(),
            event.getBookingId()
        );

        try {
            notificationService.processEvent(event);
        } catch (Exception ex) {
            // IMPORTANT: Do NOT throw exception unless you want requeue
            log.error(
                "Failed to process booking event. bookingId={}, eventType={}",
                event.getBookingId(),
                event.getEventType(),
                ex
            );

            // Let RabbitMQ retry based on configuration
            throw ex;
        }
    }
}
