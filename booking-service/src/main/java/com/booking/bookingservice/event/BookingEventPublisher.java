package com.booking.bookingservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.booking.bookingservice.event.BookingEventDTO;

import static com.booking.bookingservice.config.BookingRabbitMQConfig.BOOKING_EXCHANGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(String routingKey, BookingEventDTO event) {

        rabbitTemplate.convertAndSend(
            BOOKING_EXCHANGE,
            routingKey,
            event
        );

        log.info(
            "Published booking event: type={}, bookingId={}",
            event.getEventType(),
            event.getBookingId()
        );
    }
}
