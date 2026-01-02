package com.hotel.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {


    // Constants (contract)
    public static final String BOOKING_EXCHANGE = "booking.events.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String BOOKING_ROUTING_KEY = "booking.#";

    // Exchange
    @Bean
    public TopicExchange bookingEventsExchange() {
        return new TopicExchange(BOOKING_EXCHANGE, true, false);
    }

    // Queue
    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    // Binding
    @Bean
    public Binding bookingEventsBinding(
            Queue notificationQueue,
            TopicExchange bookingEventsExchange
    ) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(bookingEventsExchange)
                .with(BOOKING_ROUTING_KEY);
    }

    // Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
