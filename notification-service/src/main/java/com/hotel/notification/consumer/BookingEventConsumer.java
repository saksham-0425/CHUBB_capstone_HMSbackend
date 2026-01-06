package com.hotel.notification.consumer;

import com.hotel.notification.dto.BookingEventDTO;
import com.hotel.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class BookingEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(
    	    queues = "notification.queue",
    	    containerFactory = "rabbitListenerContainerFactory"
    	)
    public void handleBookingEvent(BookingEventDTO event) {

        log.info(
            "Received booking event: type={}, bookingId={}",
            event.getEventType(),
            event.getBookingId()
        );

        try {
            notificationService.processEvent(event);
        } catch (Exception ex) {
            log.error(
                "Failed to process booking event. bookingId={}, eventType={}",
                event.getBookingId(),
                event.getEventType(),
                ex
            );

            throw ex;
        }
    }
}
