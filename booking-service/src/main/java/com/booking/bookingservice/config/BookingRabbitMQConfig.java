package com.booking.bookingservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BookingRabbitMQConfig {

    public static final String BOOKING_EXCHANGE = "booking.events.exchange";

    @Bean
    public TopicExchange bookingEventsExchange() {
        return new TopicExchange(BOOKING_EXCHANGE, true, false);
    }
}
