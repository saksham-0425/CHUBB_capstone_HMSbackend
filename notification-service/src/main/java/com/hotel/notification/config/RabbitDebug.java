package com.hotel.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RabbitDebug {

    @Value("${spring.rabbitmq.username}")
    private String username;

    @PostConstruct
    public void logUser() {
        System.out.println("RabbitMQ username in runtime = " + username);
    }
}